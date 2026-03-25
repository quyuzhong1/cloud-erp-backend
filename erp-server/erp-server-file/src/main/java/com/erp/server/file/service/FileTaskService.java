package com.erp.server.file.service;

import com.erp.model.file.dto.FileDTO;
import com.erp.model.sys.dto.SysCommonDTO;

/**
 * @author zdy
 * @ClassName FileTaskService
 * @description: TODO
 * @date 2026年03月20日
 * @version: 1.0
 */
public interface FileTaskService {
    SysCommonDTO.AttachmentDTO getFeiShuFile(FileDTO.UploadDTO uploadDTO) throws Exception;
}
