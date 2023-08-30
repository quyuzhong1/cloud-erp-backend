package com.erp.server.oms.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.RefundOrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.RuleOrderApprovalService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.RuleOrderApprovalDTO;

/**
 * 订单审核规则
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@RestController
@RequestMapping("/ruleOrderApproval")
public class RuleOrderApprovalController extends BaseController {

    @Autowired
    private RuleOrderApprovalService ruleOrderApprovalService;


    /**
     * 分页查询
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-08-28
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<RuleOrderApprovalDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<RuleOrderApprovalDTO.PagingParamDTO> dto) {
        PagingVO<RuleOrderApprovalDTO.PagingViewDTO> pagingVO = ruleOrderApprovalService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-08-28
     */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated RuleOrderApprovalDTO.AddDTO dto) {
        return success(ruleOrderApprovalService.add(dto));
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2023-08-28
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated RuleOrderApprovalDTO.UpdateDTO dto) {
        ruleOrderApprovalService.update(dto);
        return success();
    }

    /**
     * 更改启用禁用状态
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-08-30 14:13
     */
    @PostMapping("/updateStatus")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean result = ruleOrderApprovalService.updateStatus(dto);
        return result?success():failure();
    }


}
