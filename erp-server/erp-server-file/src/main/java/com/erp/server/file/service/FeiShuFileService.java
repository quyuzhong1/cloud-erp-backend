package com.erp.server.file.service;

import com.erp.model.file.dto.FileDTO;
import com.erp.model.sys.dto.SysCommonDTO;

/**
 * @author zdy
 * @ClassName FeiShuFileService
 * @description: TODO
 * @date 2026年03月20日
 * @version: 1.0
 */
public interface FeiShuFileService {
    SysCommonDTO.AttachmentDTO getFeiShuFile(FileDTO.UploadDTO uploadDTO);
}
