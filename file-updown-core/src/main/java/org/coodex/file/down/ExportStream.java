package org.coodex.file.down;

import java.io.FileNotFoundException;
import java.io.OutputStream;

public interface ExportStream<T> extends ResourceHandler<T> {
    void write(OutputStream outputStream, T queryParam) throws FileNotFoundException;

    String getFileName(T queryParam);

    String getContentType(T queryParam);
}
