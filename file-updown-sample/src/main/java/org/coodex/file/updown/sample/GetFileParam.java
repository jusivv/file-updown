package org.coodex.file.updown.sample;

import org.coodex.file.helper.FileParameter;

public class GetFileParam {
    @FileParameter("fileId")
    private String fileId;
    @FileParameter("token")
    private String[] token;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String[] getToken() {
        return token;
    }

    public void setToken(String[] token) {
        this.token = token;
    }
}
