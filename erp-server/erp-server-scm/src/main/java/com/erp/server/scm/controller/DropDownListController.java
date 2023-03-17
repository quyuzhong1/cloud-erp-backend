package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.enums.InvalidStatusEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 下拉列表
 *
 * @Author Cloud
 * @Date 2022/12/19 11:26
 **/

@RestController
@RequestMapping("plm/drop/down")
public class DropDownListController extends BaseController {

    /**
     * 审核状态下拉列表
     *
     * @return
     */
    @GetMapping("/approveStatus/list")
    public ApiResult<List<BaseDropDownDTO>> listApproveStatusDropDown() {
        List<BaseDropDownDTO> result = Arrays.stream(ApproveStatusEnum.values())
                .map(x -> new BaseDropDownDTO(x.getStatus(),x.getName(),""))
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
                .map(x -> new BaseDropDownDTO(x.getStatus(),x.getName(),""))
                .collect(Collectors.toList());
        return success(result);
    }
}
