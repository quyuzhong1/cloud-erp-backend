package com.erp.server.plm.controller.feign;

import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.enums.SkuStdCostImportTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.service.ProductDetailImagesService;
import com.erp.server.plm.service.SkuStdCostDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/feign/import")
public class ImportPlmFeignController {

    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private ProductDetailImagesService productDetailImagesService;
    @Resource
    private SkuStdCostDetailService skuStdCostDetailService;

    private void updateTask(String taskId, Exception e) {
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
        importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @PostMapping("/productDetailImages")
    public void productDetailImages(@RequestBody ProductDetailDTO.ProductImagesZipDTO dto) {
        try {
            productDetailImagesService.importProductDetailImages(dto);
        } catch (Exception e) {
            log.error("导入产品详情图片失败", e);
            updateTask(dto.getTaskId(), e);
        }
    }

    @PostMapping("/skuStdCostDetail")
    public void skuStdCostDetail(@RequestBody BaseDTO.ImportTypeDTO dto) {
        try {
            SkuStdCostImportTypeEnum importTypeEnum = SkuStdCostImportTypeEnum.getByCode(dto.getImportType());
            switch (importTypeEnum) {
                case CHANGE:
                    skuStdCostDetailService.importChangeSkuStdCostDetail(dto);
                case UPDATE:
                    skuStdCostDetailService.importUpdateSkuStdCostDetail(dto);
                default:
                    throw new ServiceException("输入导入的类型有误");
            }
        } catch (Exception e) {
            log.error("【SKU标准成本导入】失败", e);
            updateTask(dto.getTaskId(), e);
        }
    }


}
