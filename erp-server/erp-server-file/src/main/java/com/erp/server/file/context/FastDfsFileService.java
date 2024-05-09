package com.erp.server.file.context;

import com.common.core.utils.FastDFSClientUtil;
import org.springframework.stereotype.Service;

@Service
public class FastDfsFileService implements FileService {

    @Override
    public void deleteFile(String fileUrl) {
        FastDFSClientUtil.deleteFile(fileUrl);
    }

    @Override
    public boolean exist(String fileUrl) {
        return FastDFSClientUtil.exist(fileUrl);
    }
}
