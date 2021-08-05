package org.coodex.file.rest.jaxrs;

import org.coodex.file.down.ExportStream;
import org.coodex.file.helper.ParameterParser;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
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
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Path("file/export")
public class FileExportResource {

    private static Logger log = LoggerFactory.getLogger(FileExportResource.class);

    private ApplicationContext context;

    @Inject
    public FileExportResource(ApplicationContext context) {
        this.context = context;
    }

    @Path("/{bizName}")
    @GET
    public void export(
            @Context final HttpServletRequest request,
            @Suspended final AsyncResponse asyncResponse,
            @PathParam("bizName") final String bizName) {
        Thread downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    asyncResponse.resume(download(bizName, request));
                } catch (BeansException e) {
                    log.error(e.getLocalizedMessage(), e);
                    asyncResponse.resume(e);
                } catch (UnsupportedEncodingException e) {
                    log.error(e.getLocalizedMessage(), e);
                    asyncResponse.resume(e);
                } catch (RuntimeException e) {
                    log.error(e.getLocalizedMessage(), e);
                    asyncResponse.resume(e);
                }
            }
        });
        downloadThread.start();
    }

    private Response download(String bizName, HttpServletRequest request) throws UnsupportedEncodingException {
        final ExportStream exportStream = context.getBean(bizName, ExportStream.class);

        final Object param = exportStream.createParameter();
        ParameterParser.parseParameterMap(request.getParameterMap(), param);

        String contentType = exportStream.getContentType(param);
        String fileName = exportStream.getFileName(param);
        Response.ResponseBuilder builder = Response.ok()
                .header("Content-Type", Common.isBlank(contentType) ? "application/octet-stream" : contentType);
        builder.header("Content-Disposition",
                "attachment; fileName=\""
                        + URLEncoder.encode(!Common.isBlank(fileName) ? fileName :
                        String.valueOf(System.currentTimeMillis()), "UTF-8")
                        + "\"");

        StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                exportStream.write(output, param);
            }
        };

        return builder.entity(output).build();
    }
}
