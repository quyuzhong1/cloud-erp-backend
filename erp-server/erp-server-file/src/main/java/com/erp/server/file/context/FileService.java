package com.erp.server.file.context;

public interface FileService {
    void deleteFile(String fileUrl);

    boolean exist(String fileUrl);
}
