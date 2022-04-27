package com.relationalai;

public class TransactionAsyncFile extends Entity {
    String name;
    byte[] data;
    String filename;
    String contentType;

    public TransactionAsyncFile(String name, byte[] data, String filename, String contentType) {
        this.name = name;
        this.data = data;
        this.filename = filename;
        this.contentType = contentType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
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
}
