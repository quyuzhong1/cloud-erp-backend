package com.erp.server.fms.controller.feign;

import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.fms.service.AssetLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * FMS异步导入Feign控制器
 * @author wuht
 * @date 2025/10/13
 */
@Slf4j
@RestController
@RequestMapping("/feign/import")
public class ImportFmsFeignController {
    
    @Resource
    private AssetLocationService assetLocationService;
    
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @PostMapping("/assetLocation")
    public void importAssetLocation(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            assetLocationService.importAssetLocation(dto);
        } catch (Exception e) {
            log.error("导入资产位置失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }
}

