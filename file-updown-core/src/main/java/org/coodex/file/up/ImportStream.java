package org.coodex.file.up;

import java.io.InputStream;

public interface ImportStream<T> {
    T read(InputStream inputStream, String fileName, long fileSize, String contentType);
}
