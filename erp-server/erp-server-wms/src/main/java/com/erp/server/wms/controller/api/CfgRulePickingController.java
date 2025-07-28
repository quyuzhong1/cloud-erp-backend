package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
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
@LogSystemModule("拣货规则")
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
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增参数")
    public ApiResult<Void> add(@RequestBody @Validated CfgRulePickingDTO.Add dto) {
        cfgRulePickingService.add(dto);
        return success();
    }

    /**
     * 修改
     *
     * @param dto 编辑参数
     **/
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "编辑参数")
    public ApiResult<String> update(@RequestBody @Validated CfgRulePickingDTO.Update dto) {
        cfgRulePickingService.update(dto);
        return success();
    }

    /**
     * 查询详情
     *
     * @param id id
     **/
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
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量启用/禁用 ids={ids},状态值={disabled}(true=禁用,false=启用)")
    public ApiResult<String> updateStatus(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO dto) {
        cfgRulePickingService.updateStatus(dto);
        return success();
    }
}
