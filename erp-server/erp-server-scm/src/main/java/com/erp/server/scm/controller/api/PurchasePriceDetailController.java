package com.erp.server.scm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.LogActionEnum;
import com.erp.model.scm.dto.ExcelImportDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.PurchasePriceHistoryService;
import com.erp.server.scm.service.PurchasePriceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采购价目管理
 *
 * @author admin
 * @since 2023-03-15
 */
@Slf4j
@RestController
@LogSystemModule("采购价目表")
@RequestMapping("/purchase/price/detail")
public class PurchasePriceDetailController extends BaseController {

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;
    @Resource
    private PurchasePriceHistoryService purchasePriceHistoryService;
    @Resource
    private PurchasePriceService purchasePriceService;


    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板采购价目明细")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        purchasePriceDetailService.downloadTemplate(response);
        return success();
    }


    /**
     * 导入数据
     *
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入数据采购价目明细")
    @PostMapping("/importFile")
    public ApiResult<PurchasePriceDetailDTO.ImportDTO> importFile(@ModelAttribute @Validated ExcelImportDTO.CommonDTO excelImportDTO, HttpServletResponse response) {
        PurchasePriceDetailDTO.ImportDTO result = purchasePriceDetailService.importFile(excelImportDTO.getExcelFile(), excelImportDTO.getSkuIds(), response);
        return success(result);
    }


    /**
     * 批量禁用
     * @author Will
     * @date: 2024/1/15 14:22
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量禁用采购价目状态", keyIdName = "ids")
    @PostMapping("/disabled")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:detail:disabled",
            serviceClass = PurchasePriceDetailService.class,
            keyIdName = "ids")
    public ApiResult<?> disabled(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = processPricingStatusUpdate(dto.getIds(), false);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

   /**
    * 批量启用
    * @author Will
    * @date: 2024/1/15 15:01
    * @param dto
    * @return ApiResult
    */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量启用采购价目状态", keyIdName = "ids")
    @PostMapping("/enable")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:detail:enable",
            serviceClass = PurchasePriceDetailService.class,
            keyIdName = "ids")
    public ApiResult<?> enable(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = processPricingStatusUpdate(dto.getIds(), true);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 获取历史数据
     */
    @PostMapping("/history")
    public ApiResult<List<PurchasePriceDetailDTO.HistoryDTO>> getHistory(@RequestBody @Valid BaseIdDTO dto) {
        List<PurchasePriceDetailDTO.HistoryDTO> historyList = purchasePriceHistoryService.getHistory(dto.getId());
        return success(historyList);
    }

    /**
     * 查询含税单价
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/3/27 9:22
     */
    @PostMapping("/getTaxPrice")
    public ApiResult<List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO>> getTaxPrice(@RequestBody @Validated PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto) {
        List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> list = purchasePriceDetailService.getTaxPrice(dto);
        return success(list);
    }

    /**
     * 批量查询含税单价
     *
     * @param list
     * @return ApiResult<List < PurchaseTaxPriceBatchViewDTO>>
     * @author Will
     * @date: 2023/9/14 14:09
     */
    @PostMapping("/batchGetTaxPrice")
    public ApiResult<List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO>> batchGetTaxPrice(@RequestBody @Validated List<PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO> list) {
        List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> resultList = purchasePriceDetailService.batchGetTaxPrice(list);
        return success(resultList);
    }


    /**
     * 批量启动或禁用
     */
    private List<BatchResultDTO> processPricingStatusUpdate(List<String> ids, boolean enable) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<PurchasePriceDetailEntity> list = purchasePriceDetailService.listByIds(ids);
        Map<String, PurchasePriceDetailEntity> entityMap = list.stream().collect(Collectors.toMap(BaseEntity::getId, e -> e));
        List<String> mainIds = list.stream().map(PurchasePriceDetailEntity::getPurchasePriceId).distinct().collect(Collectors.toList());
        Map<String, PurchasePriceEntity> mainMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(mainIds)) {
            mainMap = purchasePriceService.mapByIds(mainIds);
        }

        for (String id : ids) {
            PurchasePriceDetailEntity entity = entityMap.get(id);
            if (Objects.isNull(entity)) {
                resultDTOS.add(BatchResultDTO.fail(id, id, "采购价目明细不存在"));
                continue;
            }
            PurchasePriceEntity mainEntity = mainMap.get(entity.getPurchasePriceId());
            if (Objects.isNull(mainEntity)) {
                resultDTOS.add(BatchResultDTO.fail(id, entity.getPurchasePriceId(), "采购价目不存在"));
                continue;
            }
            try {
                BaseIdsDTO.IdsDTO idsDTO = new BaseIdsDTO.IdsDTO();
                idsDTO.setIds(Collections.singletonList(id));
                Boolean result = enable ? purchasePriceDetailService.enable(idsDTO) : purchasePriceDetailService.disabled(idsDTO);

                if (result) {
                    resultDTOS.add(BatchResultDTO.success(id, mainEntity.getCode(), (enable ? "启用" : "禁用") + "采购价目状态"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, mainEntity.getCode(), (enable ? "启用" : "禁用") + "采购价目状态"));
                }
            } catch (Exception e) {
                log.error((enable ? "启用" : "禁用") + "采购价目状态失败", e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), mainEntity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS;
    }


    /**
     * 批量查询含税单价
     * @param
     * @return
     * @date: 2025-07-22
     * @author: jack
     */
    @PostMapping("/listTaxPrice")
    public List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> listTaxPrice(@RequestBody @Validated List<PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO> list){
        return purchasePriceDetailService.listTaxPrice(list);
    }

}
