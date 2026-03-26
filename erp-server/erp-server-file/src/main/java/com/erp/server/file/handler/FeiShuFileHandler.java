package com.erp.server.file.handler;

import com.erp.model.sys.dto.SysCommonDTO;
import com.lark.oapi.Client;

/**
 * @author zdy
 * @ClassName FileHandler
 * @description: TODO
 * @date 2026年03月26日
 * @version: 1.0
 */
@FunctionalInterface
public interface FeiShuFileHandler {
    SysCommonDTO.AttachmentDTO handle(String fileToken, String sheet, String view, Client client) throws Exception;
}
