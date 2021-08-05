package org.coodex.file.down;

/**
 * 资源信息
 */
public class ResourceInfo {
    /**
     * 资源在服务器上的路径
     */
    private String resourcePath;
    /**
     * 资源的Content-Type
     */
    private String contentType;
    /**
     * 资源文件名
     */
    private String resourceName;
    /**
     * 资源大小
     */
    private long contentLength;

    public static ResourceInfo build(String resourcePath, String resourceName, String contentType, long contentLength) {
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setResourcePath(resourcePath);
        resourceInfo.setResourceName(resourceName);
        resourceInfo.setContentType(contentType);
        resourceInfo.setContentLength(contentLength);
        return resourceInfo;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }
}
