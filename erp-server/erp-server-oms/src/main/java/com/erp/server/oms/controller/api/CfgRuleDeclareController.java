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
import com.erp.model.oms.dto.CfgRuleDeclareDTO;
import com.erp.server.oms.service.CfgRuleDeclareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 申报规则表
 *
 * @author zdy
 * @since 2024-05-08
 */
@Slf4j
@RestController
@LogSystemModule("申报规则表")
@RequestMapping("/cfgRuleDeclare")
public class CfgRuleDeclareController extends BaseController {

    @Resource
    private CfgRuleDeclareService cfgRuleDeclareService;

    /**
    * 新增
    * @author zdy
    * @date:  2024-05-08
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "申报规则表新增")
    public ApiResult<String> add(@RequestBody @Validated CfgRuleDeclareDTO.AddDTO dto) {
        return success(cfgRuleDeclareService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-05-08
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "申报规则表修改")
    public ApiResult<Object> update(@RequestBody @Validated CfgRuleDeclareDTO.UpdateDTO dto) {
        cfgRuleDeclareService.update(dto);
        return success();
    }

    /**
     * 申报规则分页查询
     *
     * @param dto
     * @return ApiResult<String>
     * @author zdy
     * @date: 2024-05-08
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<CfgRuleDeclareDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<CfgRuleDeclareDTO.PagingParamDTO> dto) {
        PagingVO<CfgRuleDeclareDTO.PagingViewDTO> pagingVO = cfgRuleDeclareService.paging(dto);
        return success(pagingVO);
    }
    /**
     * 申报规则详情
     *
     * @param id
     * @return ApiResult
     * @author Lambda
     * @date: 2023-08-28
     */
    @GetMapping("/view")
    public ApiResult<CfgRuleDeclareDTO.ViewDTO> view(@RequestParam("id") String id) {
        CfgRuleDeclareDTO.ViewDTO viewDTO = cfgRuleDeclareService.view(id);
        return success(viewDTO);
    }

    /**
     * 申报规则更改启用禁用状态
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-08-30 14:13
     */
    @PostMapping("/updateStatus")
    public ApiResult<Object> updateStatus(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean result = cfgRuleDeclareService.updateStatus(dto);
        return Boolean.TRUE.equals(result) ? success() : failure();
    }
}
