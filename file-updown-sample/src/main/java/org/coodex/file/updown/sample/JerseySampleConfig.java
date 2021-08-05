package org.coodex.file.updown.sample;

import org.coodex.file.rest.jaxrs.FileDownloadResource;
import org.coodex.file.rest.jaxrs.FileExportResource;
import org.coodex.file.rest.jaxrs.FileImportResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("jaxrs")
public class JerseySampleConfig  extends ResourceConfig {
    private static Logger log = LoggerFactory.getLogger(JerseySampleConfig.class);

    public JerseySampleConfig() {
        log.debug("load jaxrs config");
        register(FileImportResource.class);
        register(FileExportResource.class);
        register(FileDownloadResource.class);
    }
}
