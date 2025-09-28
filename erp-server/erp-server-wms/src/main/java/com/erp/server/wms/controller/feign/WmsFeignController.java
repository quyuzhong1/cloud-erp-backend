package com.erp.server.wms.controller.feign;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.server.wms.service.WarehouseService;
import com.erp.server.wms.service.DictBasicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WMS 服务 Feign 控制器
 * @author wuhaotian
 * @since 2025-09-24
 */
@RestController
@RequestMapping("/feign")
public class WmsFeignController extends BaseController {

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private DictBasicService dictBasicService;

    /**
     * 仓库列表
     */
    @GetMapping("/warehouse/list")
    public ApiResult<List<WarehouseDTO.ListDTO>> warehouseList(@RequestParam(required = false) Boolean showByAuth) {
        List<WarehouseDTO.ListDTO> warehouseList = warehouseService.listApproveWarehouse(showByAuth != null ? showByAuth : Boolean.TRUE);
        return success(warehouseList);
    }

    /**
     * 字典列表
     */
    @GetMapping("/dict/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> dictList(@RequestParam("key") String key) {
        List<DictBasicDTO.ListDTO> dictList = dictBasicService.getByKey(key);
        return success(dictList.stream()
                .map(dict -> new BaseDropDownDTO.CommonDTO(dict.getValue(), dict.getName()))
                .collect(Collectors.toList()));
    }

    /**
     * 审批状态下拉列表
     */
    @GetMapping("/dropDown/approveStatus/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> approveStatusList() {
        List<BaseDropDownDTO.CommonDTO> result = Arrays.stream(ApproveStatusEnum.values())
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getStatus(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }
}
