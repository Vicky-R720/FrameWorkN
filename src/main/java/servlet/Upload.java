package servlet;

import java.io.File;
import java.io.InputStream;

/**
 * Représente un fichier uploadé via multipart/form-data
 */
public class Upload {
    private String filename;
    private String contentType;
    private long size;
    private byte[] content;
    private String savedPath;

    public Upload(String filename, String contentType, long size, byte[] content) {
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getSavedPath() {
        return savedPath;
    }

    public void setSavedPath(String savedPath) {
        this.savedPath = savedPath;
    }

    @Override
    public String toString() {
        return "Upload{" +
                "filename='" + filename + '\'' +
                ", contentType='" + contentType + '\'' +
                ", size=" + size +
                ", savedPath='" + savedPath + '\'' +
                '}';
    }
}
