package com.erp.server.plm.controller.feign;

import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.anno.LogAction;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.enums.SkuStdCostImportTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.service.*;
import com.erp.model.plm.dto.RefProductImgAttachmentDTO;
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
    @Resource
    private MoldInfoService moldInfoService;
    @Resource
    private MoldRefSkuService moldRefSkuService;
    @Resource
    private CfgMoldReturnAlertRuleService cfgMoldReturnAlertRuleService;
    @Resource
    private CfgMoldAlertRuleService cfgMoldAlertRuleService;
    
    @Resource
    private RefProductImgAttachmentService refProductImgAttachmentService;

    @Resource
    private ProductChangeService productChangeService;

    @Resource
    private SkuStdRetailPriceService skuStdRetailPriceService;

    private void updateTask(String taskId, Exception e) {
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
        importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @PostMapping("/productDetailImages")
    @LogAction(value = LogActionEnum.IMPORT, desc = "产品详情图片导入")
    public void productDetailImages(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            productDetailImagesService.importProductDetailImages(dto);
        } catch (Exception e) {
            log.error("导入产品详情图片失败", e);
            updateTask(dto.getTaskId(), e);
        }
    }

    @PostMapping("/skuStdCostDetail")
    @LogAction(value = LogActionEnum.IMPORT, desc = "SKU标准成本明细导入")
    public void skuStdCostDetail(@RequestBody BaseDTO.ImportTypeDTO dto) {
        try {
            SkuStdCostImportTypeEnum importTypeEnum = SkuStdCostImportTypeEnum.getByCode(dto.getImportType());
            switch (importTypeEnum) {
                case CHANGE:
                    skuStdCostDetailService.importChangeSkuStdCostDetail(dto);
                    break;
                case UPDATE:
                    skuStdCostDetailService.importUpdateSkuStdCostDetail(dto);
                    break;
                default:
                    throw new ServiceException("输入导入的类型有误");
            }
        } catch (Exception e) {
            log.error("【SKU标准成本导入】失败", e);
            updateTask(dto.getTaskId(), e);
        }
    }

    @PostMapping("/importMoldInfo")
    @LogAction(value = LogActionEnum.IMPORT, desc = "模具档案导入")
    public void importMoldInfo(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            moldInfoService.importMoldInfo(dto);
        } catch (Exception e) {
            log.error("导入模具档案失败", e);
            updateTask(dto.getTaskId(), e);
        }
    }

    @PostMapping("/importMoldRefSku")
    @LogAction(value = LogActionEnum.IMPORT, desc = "模具关联SKU导入")
    public void importMoldRefSku(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            moldRefSkuService.importMoldRefSku(dto);
        } catch (Exception e) {
            log.error("导入模具档案失败", e);
            updateTask(dto.getTaskId(), e);
        }
    }

    @PostMapping("/importCfgMoldReturn")
    @LogAction(value = LogActionEnum.IMPORT, desc = "模具返还策略导入")
    public void importCfgMoldReturn(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            cfgMoldReturnAlertRuleService.importCfgMoldReturn(dto);
        } catch (Exception e) {
            log.error("导入模具返还策略失败", e);
            updateTask(dto.getTaskId(), e);
        }
    }

    @PostMapping("/importCfgMoldAlert")
    @LogAction(value = LogActionEnum.IMPORT, desc = "模具预警策略导入")
    public void importCfgMoldAlert(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            cfgMoldAlertRuleService.importCfgMoldAlert(dto);
        } catch (Exception e) {
            log.error("导入模具返还策略失败", e);
            updateTask(dto.getTaskId(), e);
        }
    }

    @PostMapping("/importBatchUpload")
    @LogAction(value = LogActionEnum.IMPORT, desc = "产品图片批量上传导入")
    public void importBatchUpload(@RequestBody RefProductImgAttachmentDTO.BatchUploadDTO dto) {
        try {
            // FileTaskContext 现在直接传递 metaInfo JSON 字符串，parseParamVarArgs 会根据参数类型反序列化
            // 所以 dto 中已经包含了完整的 BatchUploadDTO 数据（包括 categoryId 和 taskId）
            refProductImgAttachmentService.batchUpload(dto);
        } catch (Exception e) {
            log.error("批量上传图片失败", e);
            updateTask(dto.getTaskId(), e);
        }
    }
    @PostMapping("/importProductChange")
    @LogAction(value = LogActionEnum.IMPORT, desc = "产品信息变更导入")
    public void importProductChange(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            productChangeService.importProductChange(dto);
        } catch (Exception e) {
            log.error("导入产品信息变更失败", e);
            updateTask(dto.getTaskId(), e);
        }
    }


    @PostMapping("/importSkuStdRetailPrice")
    @LogAction(value = LogActionEnum.IMPORT, desc = "SKU标准零售价导入")
    public void importSkuStdRetailPrice(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            skuStdRetailPriceService.importSkuStdRetailPrice(dto);
        } catch (Exception e) {
            log.error("导入sku标准零售价失败", e);
            updateTask(dto.getTaskId(), e);
        }
    }

}
