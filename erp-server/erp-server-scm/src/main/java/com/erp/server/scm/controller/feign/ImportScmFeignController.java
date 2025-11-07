package com.erp.server.scm.controller.feign;

import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.enums.SkuStdCostImportTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.scm.service.AssetNoticeService;
import com.erp.server.scm.service.AssetPurchaseOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/feign/import")
public class ImportScmFeignController {

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private AssetNoticeService assetNoticeService;

    @Resource
    private AssetPurchaseOrderService assetPurchaseOrderService;

    private void updateTask(String taskId, Exception e) {
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
        importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @PostMapping("/importAssetNotice")
    public void importAssetNotice(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            assetNoticeService.importAssetNotice(dto);
        } catch (Exception e) {
            log.error("导入开模通知单失败", e);
            updateTask(dto.getTaskId(), e);
        }
    }

    @PostMapping("/importAssetPurchaseOrder")
    public void importAssetPurchaseOrder(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            assetPurchaseOrderService.importAssetPurchaseOrder(dto);
        } catch (Exception e) {
            log.error("导入模具采购单失败", e);
            updateTask(dto.getTaskId(), e);
        }
    }

}
