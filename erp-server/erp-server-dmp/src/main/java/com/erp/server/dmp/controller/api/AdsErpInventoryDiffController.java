package com.erp.server.dmp.controller.api;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.doris.AdsErpInventoryDiffEntity;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.server.dmp.query.AdsErpAdsErpInventoryDiffQueryHandler;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.ThirdMappingService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.AdsErpInventoryDiffService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.AdsErpInventoryDiffDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 平台库存差异
 *
 * @author Jim
 * @since 2025-11-13
 */
@Slf4j
@RestController
@LogSystemModule("平台库存差异")
@RequestMapping("/adsErpInventoryDiff")
public class AdsErpInventoryDiffController extends BaseController {

    @Resource
    private AdsErpInventoryDiffService adsErpInventoryDiffService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private ThirdMappingService thirdMappingService;

    /**
     * 获取统计
     */
    @PostMapping("/statistics")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpInventoryDiff:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery
    public ApiResult<AdsErpInventoryDiffDTO.StatisticsDTO> statistics(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffDTO.PagingParamDTO> dto) {
        return success(adsErpInventoryDiffService.statistics(dto));
    }

    /**
     * 列表查询
     *
     * @param dto
     * @return ApiResult<PagingVO<AdsErpInventoryDiffDTO.ListDTO>>
     * @author Jim
     * @date: 2025-11-13
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpInventoryDiff:paging",
            tableAlias = "aeid"
    )
    @WebAdvanceQuery(handler = AdsErpAdsErpInventoryDiffQueryHandler.class)
    public ApiResult<PagingVO<AdsErpInventoryDiffDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffDTO.PagingParamDTO> dto) {
        return success(adsErpInventoryDiffService.paging(dto));
    }


    /**
     * 详情
     *
     * @param id
     * @return ApiResult<AdsErpInventoryDiffDTO.ViewDTO>>
     * @author Jim
     * @date: 2025-11-13
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpInventoryDiff:view",
            serviceClass = AdsErpInventoryDiffService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AdsErpInventoryDiffDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(adsErpInventoryDiffService.view(id));
    }

    /**
     * 导出Excel数据
     *
     * @param dto
     * @param response
     * @return
     * @author Jim
     * @date: 2025-11-13
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpInventoryDiff:export",
            tableAlias = "aeid"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "平台库存差异导出Excel数据")
    public ApiResult<String> exportList(@RequestBody @Validated AdsErpInventoryDiffDTO.ExportDTO dto, HttpServletResponse response) {
        boolean result = adsErpInventoryDiffService.exportList(dto, response);
        return result ? ApiResult.success() : ApiResult.error("导出失败");
    }

    /**
     * 编辑备注
     * @author Jim
     * @date: 2025-11-13
     */
    @PostMapping("/updateRemark")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "编辑备注")
    public ApiResult<List<BatchResultDTO>> updateRemark(@RequestBody @Validated BaseIdsDTO.BlankRemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = adsErpInventoryDiffService.updateRemark(id,dto.getRemark());
            }catch (Exception e){
                log.error("平台库存差异编辑备注",e);
                AdsErpInventoryDiffEntity entity = adsErpInventoryDiffService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "平台库存差异不存在,平台库存差异备注失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 仓库配置新增/修改
     * @author Jim
     * @date: 2025-11-13
     */
    @PostMapping("/cfgSetting")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "仓库配置新增/修改")
    public ApiResult<Boolean> cfgSetting(@RequestBody @Validated AdsErpInventoryDiffDTO.CfgSettingDTO dto) {
        CfgSettingEntity entity = cfgSettingService.lambdaQuery()
                .eq(CfgSettingEntity::getKey, SettingEnum.ADS_ERP_INVENTORY_DIFF_WAREHOUSE_LIST)
                .last("limit 1")
                .one();
        String ids = "";
        if (CollectionUtil.isNotEmpty(dto.getWarehouseIdList())) {
            ids = String.join(",", dto.getWarehouseIdList());
        }
        boolean result;
        if (null != entity) {
            entity.setValue(ids);
            result = cfgSettingService.updateById(entity);
        } else {
            CfgSettingEntity newEntity = new CfgSettingEntity()
                    .setKey(SettingEnum.ADS_ERP_INVENTORY_DIFF_WAREHOUSE_LIST)
                    .setType(SettingEnum.ADS_ERP_INVENTORY_DIFF_WAREHOUSE_LIST.getType())
                    .setValue(ids).setRemark(SettingEnum.ADS_ERP_INVENTORY_DIFF_WAREHOUSE_LIST.getValue());
            result = cfgSettingService.save(newEntity);
        }
        return ApiResult.success(result);
    }

    /**
     * 重新生成
     * @author Jim
     * @date: 2025-11-13
     */
    @PostMapping("/generateDiff")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "重新生成")
    public ApiResult<Boolean> generateDiff(@RequestBody @Validated AdsErpInventoryDiffDTO.GenerateDiffDTO dto) {
        // TODO 请求restCloud
        return ApiResult.success(true);
    }


    /**
     * 可配置仓库列表
     */
    @GetMapping("/warehouseList")
    public ApiResult<List<AdsErpInventoryDiffDTO.WarehouseListDTO>> warehouseList() {
        List<AdsErpInventoryDiffDTO.WarehouseListDTO> list = adsErpInventoryDiffService.getCanDiffWarehouseList();
        return success(list);
    }
}
