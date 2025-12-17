package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.RuleLogisticsDTO;
import com.erp.server.oms.service.RuleLogisticsService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单规则
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@RestController
@RequestMapping("/ruleLogistics")
@LogSystemModule("物流规则表")
public class RuleLogisticsController extends BaseController {

    @Resource
    private RuleLogisticsService ruleLogisticsService;


    /**
     * 物流规则分页查询
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-08-28
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<RuleLogisticsDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<RuleLogisticsDTO.PagingParamDTO> dto) {
        PagingVO<RuleLogisticsDTO.PagingViewDTO> pagingVO = ruleLogisticsService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 物流规则新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-08-28
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流规则新增")
    public ApiResult<String> add(@RequestBody @Validated RuleLogisticsDTO.AddDTO dto) {
        return success(ruleLogisticsService.add(dto));
    }

    /**
     * 物流规则修改
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2023-08-28
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流规则修改")
    public ApiResult update(@RequestBody @Validated RuleLogisticsDTO.UpdateDTO dto) {
        Boolean result = ruleLogisticsService.update(dto);
        return result?success():failure();
    }

    /**
     * 物流规则详情
     *
     * @param id
     * @return ApiResult
     * @author Lambda
     * @date: 2023-08-28
     */
    @GetMapping("/view")
    public ApiResult<RuleLogisticsDTO.ViewDTO> view(@RequestParam("id") String id) {
        RuleLogisticsDTO.ViewDTO viewDTO = ruleLogisticsService.view(id);
        return success(viewDTO);
    }

    /**
     * 物流规则更改启用禁用状态
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-08-30 14:13
     */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "物流规则更改启用禁用状态 ids={id},状态值={state}(true=禁用,false=启用)")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean result = ruleLogisticsService.updateStatus(dto);
        return result ? success() : failure();
    }

}
