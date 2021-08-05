package org.coodex.file.down;

public interface DownloadHandler<P> extends ResourceHandler<P> {
    ResourceInfo getResource(P queryParam);
}
