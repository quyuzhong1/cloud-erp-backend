package com.erp.server.wms.controller.api;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.validator.ValidList;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.CfgRulePickingStagingDTO;
import com.erp.model.wms.entity.CfgRulePickingStagingEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.server.wms.service.CfgRulePickingStagingService;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 拣货暂存规则 前端控制器
 * </p>
 *
 * @author liaohui
 * @since 2024-06-07
 */
@RestController
@RequestMapping("/pickingStaging")
public class CfgRulePickingStagingController extends BaseController {

    @Resource
    private CfgRulePickingStagingService cfgRulePickingStagingService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    /**
     * 保存默认暂存库位
     */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody List<CfgRulePickingStagingEntity> entity){
        cfgRulePickingStagingService.saveOrUpdateBatch(entity);
        return success();
    }

    /**
     * 保存默认暂存库位
     */
    @PostMapping("/delete")
    public ApiResult<String> delete(@RequestBody List<String> ids){
        cfgRulePickingStagingService.removeByIds(ids);
        return success();
    }

    /**
     * 查看暂存仓位列表
     * @return
     */
    @GetMapping("/viewStaging")
    public ApiResult<List<CfgRulePickingStagingDTO.StagingDTO>> viewStaging(){
        return success(cfgRulePickingStagingService.viewStaging());
    }

    /**
     * 保存暂存仓位配置
     */
    @PostMapping("/saveStaging")
    public ApiResult<List<BatchResultDTO>> saveStaging(@RequestBody ValidList<CfgRulePickingStagingDTO.StagingDTO> dtoList){
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dtoList.size());
        List<String> warehouseIds = dtoList.stream().map(CfgRulePickingStagingDTO.StagingDTO::getWarehouseId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        //删除已有配置
        cfgRulePickingStagingService.removeOtherWarehouse(null);
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIds);
        Map<String, String> warehouseMap = warehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));
        List<String> warehouseLocationIdList = dtoList.stream()
                .flatMap(obj -> Stream.of(obj.getB2bWarehouseLocationId(), obj.getFbaWarehouseLocationId(), obj.getThirdWarehouseLocationId()).filter(CharSequenceUtil::isNotBlank))
                .filter(value -> value != null && !value.isEmpty())
                .collect(Collectors.toList());
        List<WarehouseLocationEntity> locationEntityList = warehouseLocationService.listByIds(warehouseLocationIdList);
        Map<String, WarehouseLocationEntity> locationMap = locationEntityList.stream().collect(Collectors.toMap(WarehouseLocationEntity::getId, Function.identity()));
        //检查是否存在重复仓库id配置
        int size = (int) dtoList.stream().map(CfgRulePickingStagingDTO.StagingDTO::getWarehouseId).distinct().count();
        if (dtoList.size() != size){
            return failure("仓库存在重复配置，请检查！");
        }
        for (CfgRulePickingStagingDTO.StagingDTO dto : dtoList){
            String warehouseName = warehouseMap.get(dto.getWarehouseId());
            try {
                resultDTOS.add(cfgRulePickingStagingService.saveStaging(dto,warehouseName,locationMap));
            }catch (Exception e){
                resultDTOS.add(BatchResultDTO.fail(dto.getWarehouseId(), warehouseName, e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

}
