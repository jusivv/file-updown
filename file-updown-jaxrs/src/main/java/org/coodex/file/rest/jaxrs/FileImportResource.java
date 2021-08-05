package org.coodex.file.rest.jaxrs;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.coodex.file.up.ImportStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("file/import")
public class FileImportResource {

    private static Logger log = LoggerFactory.getLogger(FileImportResource.class);

    private ApplicationContext context;

    @Inject
    public FileImportResource(ApplicationContext context) {
        this.context = context;
    }

    @Path("/{bizName}")
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
    @Produces(MediaType.APPLICATION_JSON)
    public void importByForm(@Context final HttpServletRequest request, @Suspended final AsyncResponse asyncResponse,
                             @PathParam("bizName") final String bizName) {
        Thread uploadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, ISessionGuard> sessionGuardMap = context.getBeansOfType(ISessionGuard.class);
                    if (sessionGuardMap.size() == 1) {
                        ISessionGuard sessionGuard = sessionGuardMap.values().iterator().next();
                        if (!sessionGuard.pass(request)) {
                            throw new RuntimeException("Invalid session.");
                        }
                    } else {
                        log.warn("session guard not found.");
                    }
                    ServletFileUpload fileUpload = new ServletFileUpload(new DiskFileItemFactory());
                    List<FileItem> items = fileUpload.parseRequest(request);
                    List<Object> results = new ArrayList<Object>();
                    ImportStream<?> importStream = context.getBean(bizName, ImportStream.class);
                    for (FileItem item : items) {
                        if (!item.isFormField()) {
                            try {
                                results.add(importStream.read(item.getInputStream(), item.getName(), item.getSize(),
                                        item.getContentType()));
                            } catch (IOException e) {
                                log.error(e.getLocalizedMessage(), e);
                                asyncResponse.resume(e);
                            }
                        }
                    }
                    if (results.size() == 1) {
                        asyncResponse.resume(results.get(0));
                    } else {
                        asyncResponse.resume(results.toArray());
                    }
                } catch (FileUploadException e) {
                    log.error(e.getLocalizedMessage(), e);
                    asyncResponse.resume(e);
                } catch (RuntimeException e) {
                    log.error(e.getLocalizedMessage(), e);
                    asyncResponse.resume(e);
                }
            }
        });

        uploadThread.start();
    }
}
