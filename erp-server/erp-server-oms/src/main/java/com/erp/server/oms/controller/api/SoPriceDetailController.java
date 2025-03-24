package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoPriceDetailDTO;
import com.erp.model.oms.entity.SoPriceDetailEntity;
import com.erp.model.oms.entity.SoPriceEntity;
import com.erp.model.scm.dto.ExcelImportDTO;
import com.erp.server.oms.service.SoPriceDetailService;
import com.erp.server.oms.service.SoPriceHistoryService;
import com.erp.server.oms.service.SoPriceService;
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
 * 销售价目表明细
 *
 * @author will
 * @since 2025-03-24
 */
@Slf4j
@RestController
@LogSystemModule("销售价目表明细")
@RequestMapping("/soPriceDetail")
public class SoPriceDetailController extends BaseController {

    @Resource
    private SoPriceDetailService soPriceDetailService;
    @Resource
    private SoPriceHistoryService soPriceHistoryService;
    @Resource
    private SoPriceService soPriceService;


    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板销售价目明细")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        soPriceDetailService.downloadTemplate(response);
        return success();
    }


    /**
     * 导入数据
     *
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入数据销售价目明细")
    @PostMapping("/importFile")
    public ApiResult<SoPriceDetailDTO.ImportDTO> importFile(@ModelAttribute @Validated ExcelImportDTO.CommonDTO excelImportDTO, HttpServletResponse response) {
        SoPriceDetailDTO.ImportDTO result = soPriceDetailService.importFile(excelImportDTO.getExcelFile(), excelImportDTO.getSkuIds(), response);
        return success(result);
    }


    /**
     * 批量禁用
     * @author Will
     * @date: 2024/1/15 14:22
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量禁用销售价目状态", keyIdName = "ids")
    @PostMapping("/disabled")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "oms:so:price:detail:disabled",
            serviceClass = SoPriceDetailService.class,
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
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量启用销售价目状态", keyIdName = "ids")
    @PostMapping("/enable")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "oms:so:price:detail:enable",
            serviceClass = SoPriceDetailService.class,
            keyIdName = "ids")
    public ApiResult<?> enable(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = processPricingStatusUpdate(dto.getIds(), true);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 获取历史数据
     */
    @PostMapping("/history")
    public ApiResult<List<SoPriceDetailDTO.HistoryDTO>> getHistory(@RequestBody @Valid BaseIdDTO dto) {
        List<SoPriceDetailDTO.HistoryDTO> historyList = soPriceHistoryService.getHistory(dto.getId());
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
    public ApiResult<List<SoPriceDetailDTO.SoTaxPriceViewDTO>> getTaxPrice(@RequestBody @Validated SoPriceDetailDTO.SoTaxPriceSearchDTO dto) {
        List<SoPriceDetailDTO.SoTaxPriceViewDTO> list = soPriceDetailService.getTaxPrice(dto);
        return success(list);
    }

    /**
     * 批量查询含税单价
     *
     * @param list
     * @return ApiResult<List < SoTaxPriceBatchViewDTO>>
     * @author Will
     * @date: 2023/9/14 14:09
     */
    @PostMapping("/batchGetTaxPrice")
    public ApiResult<List<SoPriceDetailDTO.SoTaxPriceBatchViewDTO>> batchGetTaxPrice(@RequestBody @Validated List<SoPriceDetailDTO.SoTaxPriceSearchDTO> list) {
        List<SoPriceDetailDTO.SoTaxPriceBatchViewDTO> resultList = soPriceDetailService.batchGetTaxPrice(list);
        return success(resultList);
    }


    /**
     * 批量启动或禁用
     */
    private List<BatchResultDTO> processPricingStatusUpdate(List<String> ids, boolean enable) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SoPriceDetailEntity> list = soPriceDetailService.listByIds(ids);
        Map<String, SoPriceDetailEntity> entityMap = list.stream().collect(Collectors.toMap(BaseEntity::getId, e -> e));
        List<String> mainIds = list.stream().map(SoPriceDetailEntity::getMainId).distinct().collect(Collectors.toList());
        Map<String, SoPriceEntity> mainMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(mainIds)) {
            mainMap = soPriceService.mapByIds(mainIds);
        }

        for (String id : ids) {
            SoPriceDetailEntity entity = entityMap.get(id);
            if (Objects.isNull(entity)) {
                resultDTOS.add(BatchResultDTO.fail(id, id, "销售价目明细不存在"));
                continue;
            }
            SoPriceEntity mainEntity = mainMap.get(entity.getMainId());
            if (Objects.isNull(mainEntity)) {
                resultDTOS.add(BatchResultDTO.fail(id, entity.getMainId(), "销售价目不存在"));
                continue;
            }
            try {
                BaseIdsDTO.IdsDTO idsDTO = new BaseIdsDTO.IdsDTO();
                idsDTO.setIds(Collections.singletonList(id));
                Boolean result = enable ? soPriceDetailService.enable(idsDTO) : soPriceDetailService.disabled(idsDTO);

                if (result) {
                    resultDTOS.add(BatchResultDTO.success(id, mainEntity.getCode(), (enable ? "启用" : "禁用") + "销售价目状态"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, mainEntity.getCode(), (enable ? "启用" : "禁用") + "销售价目状态"));
                }
            } catch (Exception e) {
                log.error((enable ? "启用" : "禁用") + "销售价目状态失败", e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), mainEntity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS;
    }



}
