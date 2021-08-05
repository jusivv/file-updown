package org.coodex.file.updown.sample;

import org.coodex.file.down.ExportStream;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class FileUpDownSampleBoot {
    private static Logger log = LoggerFactory.getLogger(FileUpDownSampleBoot.class);

    public static void main(String[] args) {
        SpringApplication.run(FileUpDownSampleBoot.class, args);
    }

    @Bean
    public ResourceConfig getJaxRsConfig() {
        return new JerseySampleConfig();
    }

    @Bean("exportTest")
    public ExportStream<GetFileParam> exportStream() {
        return new ExportStream<GetFileParam>() {

            @Override
            public GetFileParam createParameter() {
                return new GetFileParam();
            }

            @Override
            public void write(OutputStream outputStream, GetFileParam queryParam) throws FileNotFoundException {
                String content = queryParam.getFileId() + "|" + queryParam.getToken();
                try {
                    outputStream.write(content.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                    FileNotFoundException fileNotFoundException = new FileNotFoundException();
                    fileNotFoundException.initCause(e);
                    throw fileNotFoundException;
                }
            }

            @Override
            public String getFileName(GetFileParam queryParam) {
                return "test.txt";
            }

            @Override
            public String getContentType(GetFileParam queryParam) {
                return "application/octet-stream";
            }
        };
    }
}
