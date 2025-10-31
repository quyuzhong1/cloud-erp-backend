package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WarehouseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * WMS 服务 Feign 接口
 * @author wuhaotian
 * @since 2025-09-24
 */
@FeignClient(name = "erp-wms", contextId = "wmsFeign", configuration = {FeignErrorDecoder.class})
public interface WmsFeign {

    /**
     * 仓库列表
     */
    @GetMapping("/feign/warehouse/list")
    ApiResult<List<WarehouseDTO.ListDTO>> warehouseList(@RequestParam(required = false) Boolean showByAuth);

    /**
     * 字典列表
     */
    @GetMapping("/feign/dict/list")
    ApiResult<List<BaseDropDownDTO.CommonDTO>> dictList(@RequestParam("key") String key);

    /**
     * 审批状态下拉列表
     */
    @GetMapping("/feign/dropDown/approveStatus/list")
    ApiResult<List<BaseDropDownDTO.CommonDTO>> approveStatusList();
}
