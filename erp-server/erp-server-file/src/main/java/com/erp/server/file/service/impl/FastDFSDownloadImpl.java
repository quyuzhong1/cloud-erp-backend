package com.erp.server.file.service.impl;

import com.common.business.annotation.FileServiceType;
import com.common.business.enums.FileServiceTypeEnum;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.server.file.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author zdy
 * @ClassName FastDFSDownloadImpl
 * @description: TODO
 * @date 2025年06月19日
 * @version: 1.0
 */
@Slf4j
@Service
@FileServiceType(FileServiceTypeEnum.FAST_DFS)
public class FastDFSDownloadImpl implements FileService {
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
    public String uploadFile(byte[] buff, String fileName, Map<String, String> metaList) {
        return FastDFSClientUtil.uploadFile(buff, fileName, metaList);
    }

    @Override
    public byte[] downloadFile(String fileId) {
        return FastDFSClientUtil.getFileByte(fileId);
    }

    @Override
    public InputStream getInputStream(String fileId) {
        return FastDFSClientUtil.getInputStream(fileId);
    }

    @Override
    public ResponseEntity<byte[]> downloadByte(String fileId, String fileName, String contentType, boolean bPreview) {
        return FastDFSClientUtil.downloadByte(fileId, fileName, contentType, bPreview);
    }
}
