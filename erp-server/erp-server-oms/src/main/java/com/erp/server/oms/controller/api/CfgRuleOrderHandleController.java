package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.CfgRuleOrderHandleDTO;
import com.erp.server.oms.service.CfgRuleOrderHandleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 订单处理规则表
 *
 * @author will
 * @since 2024-05-09
 */
@Slf4j
@RestController
@LogSystemModule("订单处理规则表")
@RequestMapping("/cfgRuleOrderHandle")
public class CfgRuleOrderHandleController extends BaseController {

    @Resource
    private CfgRuleOrderHandleService cfgRuleOrderHandleService;

    /**
    * 新增
    * @author will
    * @date:  2024-05-09
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "订单处理规则表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgRuleOrderHandleDTO.AddDTO dto) {
        return success(cfgRuleOrderHandleService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-05-09
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "订单处理规则表修改")
    public ApiResult<Object> update(@RequestBody @Validated CfgRuleOrderHandleDTO.UpdateDTO dto) {
        cfgRuleOrderHandleService.update(dto);
        return success();
    }

    /**
     * 分页查询
     * @author Will
     * @date: 2024/5/9 11:33
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<CfgRuleOrderHandleDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<CfgRuleOrderHandleDTO.PagingParamDTO> dto) {
        PagingVO<CfgRuleOrderHandleDTO.ListDTO> pagingVO = cfgRuleOrderHandleService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 查询详情
     * @author Will
     * @date: 2024/5/9 11:43
     * @param id
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    public ApiResult<CfgRuleOrderHandleDTO.ViewDTO> view(@RequestParam("id") String id) {
        CfgRuleOrderHandleDTO.ViewDTO viewDTO = cfgRuleOrderHandleService.view(id);
        return success(viewDTO);
    }

    /**
     * 更新状态
     * @author Will
     * @date: 2024/5/9 11:52
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/updateStatus")
    public ApiResult<Object> updateStatus(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean result = cfgRuleOrderHandleService.updateStatus(dto);
        return Boolean.TRUE.equals(result) ? success() : failure();
    }
}
