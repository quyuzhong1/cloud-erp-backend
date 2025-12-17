package com.erp.server.sys.controller.api;


import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.CfgQueryConditionDTO;
import com.erp.server.sys.service.CfgQueryConditionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 查询条件配置表
 * @author lrp
 * @since 2024-01-03
 */
@Slf4j
@RestController
@LogSystemModule("查询条件配置表")
@RequestMapping("/cfgQueryCondition")
public class CfgQueryConditionController extends BaseController {

    @Resource
    private CfgQueryConditionService cfgQueryConditionService;

    /**
     * menuCode分页查询
     */
    @PostMapping("/menuPaging")
    public ApiResult<PagingVO<CfgQueryConditionDTO.MenuDTO>> menuPaging(@RequestBody PagingDTO<CfgQueryConditionDTO.MenuSearchParamDTO> menuSearchParamDTOPagingDTO) {
        return success(cfgQueryConditionService.menuPaging(menuSearchParamDTOPagingDTO));
    }

    /**
     * 分页查询
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<CfgQueryConditionDTO.ListDTO>> paging(@RequestBody PagingDTO<CfgQueryConditionDTO.SearchParamDTO> searchParamDTOPagingDTO) {
        return success(cfgQueryConditionService.paging(searchParamDTOPagingDTO));
    }

    /**
     * 新增
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<Boolean> add(@RequestBody @Validated CfgQueryConditionDTO.AddDTO addDTO) {
        return success(cfgQueryConditionService.add(addDTO));
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新")
    public ApiResult<Boolean> update(@RequestBody @Validated CfgQueryConditionDTO.UpdateDTO updateDTO) {
        return success(cfgQueryConditionService.update(updateDTO));
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.UPDATE, desc = "删除")
    public ApiResult<Boolean> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(cfgQueryConditionService.delete(dto));
    }
    /**
     * 获取查询条件配置
     */
    @GetMapping("/getQueryCondition")
    public ApiResult<List<CfgQueryConditionDTO.ViewDTO>> getQueryCondition(@RequestParam(name = "code") String code) {
        if(StringUtils.isBlank(code)){
            return failure("页面code不能为空");
        }
        return success(cfgQueryConditionService.getQueryCondition(code));
    }
}
