package org.coodex.file.rest.jaxrs;

import org.coodex.file.down.DownloadHandler;
import org.coodex.file.down.ResourceInfo;
import org.coodex.file.helper.ParameterParser;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.net.URLEncoder;

@Path("file/download")
public class FileDownloadResource {
    private static Logger log = LoggerFactory.getLogger(FileDownloadResource.class);

    private ApplicationContext context;

    @Inject
    public FileDownloadResource(ApplicationContext context) {
        this.context = context;
    }

    @Path("/{bizName}/{supportRange}")
    @GET
    public void download(@Context final HttpServletRequest request,
                         @Suspended final AsyncResponse asyncResponse,
                         @PathParam("bizName") final String bizName,
                         @PathParam("supportRange") final boolean supportRange) {

        Thread downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    asyncResponse.resume(handle(bizName, supportRange, request));
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage(), e);
                    asyncResponse.resume(e);
                }
            }
        });
        downloadThread.start();
    }

    private Response handle(String bizName, boolean supportRange, HttpServletRequest request)
            throws UnsupportedEncodingException {
        final DownloadHandler downloadHandler = context.getBean(bizName, DownloadHandler.class);
        final Object param = downloadHandler.createParameter();
        ParameterParser.parseParameterMap(request.getParameterMap(), param);
        ResourceInfo resourceInfo = downloadHandler.getResource(param);
        log.debug("download file [{}], file size: {}", resourceInfo.getResourcePath(), resourceInfo.getContentLength());
        final File file = new File(resourceInfo.getResourcePath());
        if (!file.exists()) {
            throw new RuntimeException("找不到资源");
        }
        Response.ResponseBuilder builder = Response.ok()
                .header("Content-Type",
                        Common.isBlank(resourceInfo.getContentType()) ? "application/octet-stream" : resourceInfo.getContentType());

        builder.header("Content-Disposition",
                "attachment; fileName=\""
                        + URLEncoder.encode(!Common.isBlank(resourceInfo.getResourceName()) ? resourceInfo.getResourceName() :
                        String.valueOf(System.currentTimeMillis()), "UTF-8")
                        + "\"");
        builder.header("Content-Length", file.length());
        Object entity = file;
        if (supportRange) {
            // support range
            builder.header("Accept-Ranges", "bytes");
            String range = request.getHeader("RANGE");
            if (!Common.isBlank(range)) {
                log.debug("download RANGE: {}", range);
                int pos = range.indexOf('=');
                if (pos != -1) {
                    range = range.substring(pos + 1);
                }
                pos = range.indexOf('-');
                if (pos != -1) {
                    range = range.substring(0, pos);
                }
                long start = Math.max(0L, Long.parseLong(range.trim())), fileSize = file.length();
                start = Math.min(start, fileSize - 1);
                if (start > 0L) {
                    final long skipBytes = start;
                    StreamingOutput output = new StreamingOutput() {
                        @Override
                        public void write(OutputStream output) throws IOException, WebApplicationException {
                            log.debug("write output stream skip {} bytes", skipBytes);
                            FileInputStream fis = null;
                            try {
                                fis = new FileInputStream(file);
                                fis.skip(skipBytes);
                                byte[] buff = new byte[1024 * 4];
                                int len = -1;
                                while ((len = fis.read(buff)) != -1) {
                                    output.write(buff, 0, len);
                                }
                                output.flush();
                            } finally {
                                if (fis != null) {
                                    fis.close();
                                }
                            }
                        }
                    };
                    entity = output;
                    builder.header("Content-Range", "bytes " + start + "-" + (fileSize - 1) + "/" + fileSize);
                    builder.status(Response.Status.PARTIAL_CONTENT);
                }
            }
        }
        return builder.entity(entity).build();
    }
}
