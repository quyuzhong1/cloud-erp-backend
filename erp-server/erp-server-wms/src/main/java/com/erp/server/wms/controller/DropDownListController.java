package com.erp.server.wms.controller;


import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.entity.QcRuleEntity;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.server.wms.service.QcRuleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                .map(x -> new BaseDropDownDTO.DisabledDTO(x.getType(), x.getName(),qcTypes.contains(x.getType())))
                .collect(Collectors.toList());
        return success(result);
    }

}
