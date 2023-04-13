package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.dto.SupplierContactDTO;
import com.erp.model.scm.entity.SupplierContactEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.*;
import com.erp.server.scm.service.SupplierService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private SupplierService supplierService;
    @Resource
    private com.erp.server.scm.service.SupplierContactService SupplierContactService;


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
     * 作废状态下拉列表
     *
     * @return
     */
    @GetMapping("/invalidStatus/list")
    public ApiResult<List<BaseDropDownDTO.CommonBooleanDTO>> listInvalidStatusDropDown() {
        List<BaseDropDownDTO.CommonBooleanDTO> result = Arrays.stream(InvalidStatusEnum.values())
                .map(x -> new BaseDropDownDTO.CommonBooleanDTO(x.getStatus(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 已审核供应商下拉列表
     *
     * @return
     */
    @GetMapping("/supplier/list")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> listSupplierDropDown() {
        List<Map<String, Object>> mapList = supplierService.listApproveSupplier();
        if (CollectionUtils.isEmpty(mapList)) {
            return success(new ArrayList<>());
        }
        List<BaseDropDownDTO.DisabledDTO> result = mapList.stream()
                .map(x -> new BaseDropDownDTO.DisabledDTO(x.get("id").toString(), x.get("name").toString(),(Boolean)x.get("disabled")))
                .collect(Collectors.toList());
        return success(result);
    }


    /**
     * 所有供应商下拉列表
     *
     * @return
     */
    @GetMapping("/supplier/allList")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> listALLSupplierDropDown() {
        List<SupplierEntity> mapList = supplierService.list();
        if (CollectionUtils.isEmpty(mapList)) {
            return success(new ArrayList<>());
        }
        List<BaseDropDownDTO.DisabledDTO> result = mapList.stream()
                .map(x -> new BaseDropDownDTO.DisabledDTO(x.getId(), x.getName(),x.getDisabled()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 获取供应商阶段列表
     *
     * @return
     */
    @GetMapping("/supplier/phase/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listSupplierPhase() {
        SupplierPhaseEnum[] phaseList = SupplierPhaseEnum.values();
        List<SupplierPhaseEnum> list = Arrays.asList(phaseList);
        List<BaseDropDownDTO.CommonDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getPhase(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 供应商拜访类型下拉
     *
     * @return
     */
    @GetMapping("/supplier/visitType/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listVisitType() {
        List<SupplierVisitEnum> list = Arrays.asList(SupplierVisitEnum.values());
        List<BaseDropDownDTO.CommonDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getType(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }


    /**
     * 供应商拜访结果下拉
     *
     * @return
     */
    @GetMapping("/supplier/visitResult/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listVisitResult() {
        List<SupplierVisitResultEnum> list = Arrays.asList(SupplierVisitResultEnum.values());
        List<BaseDropDownDTO.CommonDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getCode(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }


    /**
     * 订单生成状态列表
     *
     * @return
     */
    @GetMapping("/createPoType/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listCreatePoType() {
        List<CreatePoTypeEnum> list = Arrays.asList(CreatePoTypeEnum.values());
        List<BaseDropDownDTO.CommonDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getStatus(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 到货状态列表
     *
     * @return
     */
    @GetMapping("/arrivalStatus/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listArrivalStatus() {
        List<ArrivalStatusEnum> list = Arrays.asList(ArrivalStatusEnum.values());
        List<BaseDropDownDTO.CommonDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getCode(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 供应商联系人
     * @author Will
     * @date: 2023/3/29 9:29
     * @return ApiResult<List<DropDownDTO>>
     */
    @GetMapping("/supplierContact/list")
    public ApiResult<List<SupplierContactDTO.DropDownDTO>> listSupplierContactDropDown(@RequestParam("supplierId") String supplierId) {
        List<SupplierContactEntity> resultList = SupplierContactService
                .lambdaQuery()
                .eq(SupplierContactEntity::getSupplierId,supplierId)
                .list();
        if (CollectionUtils.isEmpty(resultList)) {
            return success(new ArrayList<>());
        }
        List<SupplierContactDTO.DropDownDTO> list = BeanMapperUtils.copyList(SupplierContactDTO.DropDownDTO.class, resultList);
        return success(list);
    }

}
