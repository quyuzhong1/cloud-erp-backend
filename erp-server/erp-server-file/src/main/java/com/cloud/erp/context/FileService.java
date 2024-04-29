package com.cloud.erp.context;

public interface FileService {
    void deleteFile(String fileUrl);

    boolean exist(String fileUrl);
}
