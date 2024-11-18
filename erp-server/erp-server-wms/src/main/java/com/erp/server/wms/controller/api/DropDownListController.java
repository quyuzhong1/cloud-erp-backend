package com.erp.server.wms.controller.api;


import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.entity.QcRuleEntity;
import com.erp.model.wms.enums.*;
import com.erp.server.wms.pull.service.ProductInfoService;
import com.erp.server.wms.service.QcRuleService;
import com.erp.server.wms.service.WarehouseLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 下拉管理
 *
 * @Author will
 * @Date 2023/03/19 11:26
 **/

@RestController
@RequestMapping("/drop/down")
public class DropDownListController extends BaseController {

    @Resource
    private QcRuleService qcRuleService;

    @Resource
    private ProductInfoService productInfoService;
    @Resource
    private WarehouseLocationService warehouseLocationService;


    /**
     * 审核状态下拉列表
     *
     * @return
     */
    @GetMapping("/approveStatus/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listApproveStatusDropDown() {
        List<BaseDropDownDTO.CommonDTO> result = Arrays.stream(ApproveStatusEnum.values())
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getStatus(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }


    /**
     * 质检类型下拉列表
     *
     * @return
     */
    @GetMapping("/qcType/list")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> listQcTypeDropDown() {
        List<QcRuleEntity> list= qcRuleService.list();
        List<String> qcTypes=list.stream().map(x -> x.getQcType().getCode()).collect(Collectors.toList());
        List<BaseDropDownDTO.DisabledDTO> result = Arrays.stream(QcTypeEnum.values())
                .map(x -> new BaseDropDownDTO.DisabledDTO(x.getCode(), x.getName(),qcTypes.contains(x.getCode())))
                .collect(Collectors.toList());
        return success(result);
    }


    /**
     * 质检状态下拉列表
     *
     * @return
     */
    @GetMapping("/qcStatus/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listQcStatusDropDown() {
        List<BaseDropDownDTO.CommonDTO> result = Arrays.stream(QcBillStatusEnum.values())
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getCode(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 质检结果下拉列表
     *
     * @return
     */
    @GetMapping("/qcResult/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listQcResultDropDown() {
        List<BaseDropDownDTO.CommonDTO> result = Arrays.stream(QcResultEnum.values())
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getCode(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }


    /**
     * 质检信息 质检类型下拉列表
     *
     * @return
     */
    @GetMapping("/qcInfo/qcType/list")
    public ApiResult<List<BaseDropDownDTO.QcTypeDTO>> listQcInfoQcTypeDropDown() {
        List<BaseDropDownDTO.QcTypeDTO> result = Arrays.stream(QcTypeEnum.values())
                .map(x -> new BaseDropDownDTO.QcTypeDTO(x.getCode(), x.getName(),x.getIsInside()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * spu no下拉
     *
     * @return
     */
    @GetMapping("/product/spuNo/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listSpuNoDropDown() {
        return success(productInfoService.getNotEmptySpuNos());
    }

    /**
     * 单据类型下拉
     *
     * @return
     */
    @GetMapping("/billType/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listBillTypeDropDown(@RequestParam(value = "type",required = true)String type) {
        List<BillTypeEnum> billTypeList=BillTypeEnum.listByType(type);
        List<BaseDropDownDTO.CommonDTO> result = billTypeList.stream()
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getCode(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    @GetMapping("/warehouseArea/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> ListWarehouseArea(@RequestParam(value = "warehouseId",required = false) String warehouseId){
        // 0 返回库区
        return success(warehouseLocationService.getWarehouseArea(warehouseId,  WarehouseLocationTypeEnum.AREA, ""));
    }

    @GetMapping("/warehouseLocation/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> ListWarehouseLocation(@RequestParam(value = "warehouseId",required = false) String warehouseId,
                                                                            @RequestParam(value = "areaCode",required = false) String areaId){
        // 1 返回库位
        return success(warehouseLocationService.getWarehouseArea(warehouseId, WarehouseLocationTypeEnum.LOCATION, areaId));
    }


}
