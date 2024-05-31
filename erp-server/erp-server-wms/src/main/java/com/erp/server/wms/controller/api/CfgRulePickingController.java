package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.server.wms.service.CfgRulePickingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 拣货规则表 前端控制器
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
@RestController
@RequestMapping("/cfg-rule-picking")
public class CfgRulePickingController extends BaseController {

    @Resource
    private CfgRulePickingService cfgRulePickingService;

    /**
     * 分页查询
     *
     * @param dto 分页查询条件
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<CfgRulePickingDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<CfgRulePickingDTO.PagingParam> dto) {
        PagingVO<CfgRulePickingDTO.PagingView> pagingVO = cfgRulePickingService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 新增
     *
     * @param dto 新增参数
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增拣货规则")
    @PostMapping("/add")
    public ApiResult<Void> add(@RequestBody @Validated CfgRulePickingDTO.Add dto) {
        cfgRulePickingService.add(dto);
        return success();
    }

    /**
     * 修改
     *
     * @param dto 编辑参数
     **/
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改拣货规则")
    @PostMapping("/update")
    public ApiResult<String> update(@RequestBody @Validated CfgRulePickingDTO.Add dto, @RequestParam(value = "id") String id) {
        cfgRulePickingService.update(dto, id);
        return success();
    }

    /**
     * 查询详情
     *
     * @param id id
     **/
    @LogViewService
    @GetMapping("/view")
    public ApiResult<CfgRulePickingDTO.View> view(@RequestParam("id") String id) {
        CfgRulePickingDTO.View dto = cfgRulePickingService.view(id);
        return success(dto);
    }

    /**
     * 批量删除
     *
     * @param idsDTO idsDTO
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除拣货规则")
    @PostMapping("/delete")
    public ApiResult<String> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        cfgRulePickingService.delete(idsDTO.getIds());
        return success();
    }

    /**
     * 启用/禁用
     *
     * @param dto dto
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "启用拣货规则:id={id},状态值={state}(true=禁用,false=启用)")
    @PostMapping("/updateStatus")
    public ApiResult<String> updateStatus(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO dto) {
        cfgRulePickingService.updateStatus(dto);
        return success();
    }
}
