package com.erp.server.plm.controller;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.enums.SalesPlatformEnum;
import com.erp.model.plm.dto.SelectShowDTO;
import com.erp.model.plm.enums.RelatedSkuTypeEnum;
import com.erp.model.plm.enums.TaskRelationshipEnum;
import com.erp.model.plm.vo.RelationshipVO;
import com.common.business.constant.IsConstant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BI 下拉列表
 *
 * @Author Cloud
 * @Date 2022/12/19 11:26
 **/

@RestController
@RequestMapping("drop/down")
public class PlmDropDownListController extends BaseController {

    /**
     * 平台下拉列表
     *
     * @return
     */
    @GetMapping("/platform/list")
    public ApiResult<List<SelectShowDTO>> listPlatformDropDown() {
        List<SelectShowDTO> result = Arrays.stream(SalesPlatformEnum.values())
                .map(x -> new SelectShowDTO(IsConstant.NO, x.getName(), x.getDesc()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 关联类型下拉列表
     *
     * @return
     */
    @GetMapping("/relatedSkuType/list")
    public ApiResult<List<SelectShowDTO>> listRelatedSkuType() {
        List<SelectShowDTO> result = Arrays.stream(RelatedSkuTypeEnum.values())
                .map(x -> new SelectShowDTO(IsConstant.NO, x.getCode(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 依赖关系枚举下拉
     * @return
     */
    @GetMapping("/relationship/list")
    public ApiResult<List<RelationshipVO>> listRelationshipDropDown() {
        List<RelationshipVO> result = Arrays.stream(TaskRelationshipEnum.values())
                .map(x -> new RelationshipVO(x.getCode(),x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

}
