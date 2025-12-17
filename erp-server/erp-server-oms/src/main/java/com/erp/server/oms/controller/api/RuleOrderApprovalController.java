package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.RuleOrderApprovalDTO;
import com.erp.server.oms.service.RuleOrderApprovalService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单规则
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@RestController
@RequestMapping("/ruleOrderApproval")
@LogSystemModule("订单审核规则")
public class RuleOrderApprovalController extends BaseController {

    @Resource
    private RuleOrderApprovalService ruleOrderApprovalService;


    /**
     * 订单审核规则分页查询
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-08-28
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:ruleOrderApproval:paging",
            tableAlias = "roa"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<RuleOrderApprovalDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<RuleOrderApprovalDTO.PagingParamDTO> dto) {
        PagingVO<RuleOrderApprovalDTO.PagingViewDTO> pagingVO = ruleOrderApprovalService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 订单审核规则新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-08-28
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "订单审核规则新增")
    public ApiResult<String> add(@RequestBody @Validated RuleOrderApprovalDTO.AddDTO dto) {
        return success(ruleOrderApprovalService.add(dto));
    }


    /**
     * 订单审核规则详情
     *
     * @param id
     * @return ApiResult
     * @author Lambda
     * @date: 2023-08-28
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:ruleOrderApproval:view",
            serviceClass = RuleOrderApprovalService.class,
            keyIdName = "id")
    public ApiResult<RuleOrderApprovalDTO.ViewDTO> view(@RequestParam("id") String id) {
        RuleOrderApprovalDTO.ViewDTO viewDTO = ruleOrderApprovalService.view(id);
        return success(viewDTO);
    }

    /**
     * 订单审核规则修改
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2023-08-28
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "订单审核规则修改")
    public ApiResult update(@RequestBody @Validated RuleOrderApprovalDTO.UpdateDTO dto) {
        Boolean result = ruleOrderApprovalService.update(dto);
        return result ? success() : failure();
    }

    /**
     * 订单审核规则更改启用禁用状态
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-08-30 14:13
     */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "订单审核规则更改启用禁用状态 ids={id},状态值={state}(true=禁用,false=启用)")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean result = ruleOrderApprovalService.updateStatus(dto);
        return result ? success() : failure();
    }

    @PostMapping("/test")
    public ApiResult test(@RequestBody Map<String,Object> obj) {
        RuleOrderApprovalDTO.RuleMatchDTO ruleMatchDTO = ruleOrderApprovalService.getRuleOrderMatchResult(obj);
        return success(ruleMatchDTO);
    }


}
