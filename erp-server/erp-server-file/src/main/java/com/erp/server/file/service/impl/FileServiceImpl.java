package com.erp.server.file.service.impl;

import com.common.core.utils.FastDFSClientUtil;
import com.erp.server.file.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * @author zdy
 * @ClassName FileServiceImpl
 * @description: TODO
 * @date 2025年06月19日
 * @version: 1.0
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {
    @Override
    public String uploadFile(MultipartFile file) {
        return FastDFSClientUtil.uploadFile(file);
    }

    @Override
    public int deleteFile(String url) {
        return FastDFSClientUtil.deleteFile(url);
    }

    @Override
    public boolean exist(String fileUrl) {
        return false;
    }

    @Override
    public void deleteBatchFile(List<String> urlList) {
        FastDFSClientUtil.deleteBatchFile(urlList);
    }

    @Override
    public String uploadFile(File file, String fileName) {
        return FastDFSClientUtil.uploadFile(file, fileName);
    }

    @Override
    public byte[] downloadFile(String fileId) {
        return FastDFSClientUtil.getFileByte(fileId);
    }
}
