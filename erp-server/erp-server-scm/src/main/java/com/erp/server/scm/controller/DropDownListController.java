package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.SupplierPhaseEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.service.SupplierService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 下拉列表
 *
 * @Author will
 * @Date 2023/03/19 11:26
 **/

@RestController
@RequestMapping("/drop/down")
public class DropDownListController extends BaseController {

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SupplierService supplierService;

    /**
     * 审核状态下拉列表
     *
     * @return
     */
    @GetMapping("/approveStatus/list")
    public ApiResult<List<BaseDropDownDTO>> listApproveStatusDropDown() {
        List<BaseDropDownDTO> result = Arrays.stream(ApproveStatusEnum.values())
                .map(x -> new BaseDropDownDTO(x.getStatus(), x.getName(), ""))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 作废状态下拉列表
     *
     * @return
     */
    @GetMapping("/invalidStatus/list")
    public ApiResult<List<BaseDropDownDTO>> listInvalidStatusDropDown() {
        List<BaseDropDownDTO> result = Arrays.stream(InvalidStatusEnum.values())
                .map(x -> new BaseDropDownDTO(x.getStatus(), x.getName(), ""))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 已审核启用仓库下拉列表
     *
     * @return
     */
    @GetMapping("/warehouse/list")
    public ApiResult<List<BaseDropDownDTO>> listWarehouseDropDown() {
        List<WarehouseDTO.UpdateDTO> list = wmsTaskFeign.listApproveWarehouse();
        if (CollectionUtils.isEmpty(list)) {
            return success(new ArrayList<>());
        }
        List<BaseDropDownDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO(x.getId(), x.getName(), ""))
                .collect(Collectors.toList());
        return success(result);
    }


    /**
     * 已审核供应商下拉列表
     *
     * @return
     */
    @GetMapping("/supplier/list")
    public ApiResult<List<BaseDropDownDTO>> listSupplierDropDown() {
        List<Map<String, Object>> mapList = supplierService.listApproveSupplier();
        if (CollectionUtils.isEmpty(mapList)) {
            return success(new ArrayList<>());
        }
        List<BaseDropDownDTO> result = mapList.stream()
                .map(x -> new BaseDropDownDTO(x.get("id").toString(), x.get("name").toString(), ""))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 获取供应商阶段列表
     *
     * @return
     */
    @GetMapping("/supplier/phase/list")
    public ApiResult<List<BaseDropDownDTO>> listSupplierPhase() {
        SupplierPhaseEnum[] phaseList = SupplierPhaseEnum.values();
        List<SupplierPhaseEnum> list = Arrays.asList(phaseList);
        List<BaseDropDownDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO(x.getPhase(), x.getName(), ""))
                .collect(Collectors.toList());
        return success(result);
    }

}
