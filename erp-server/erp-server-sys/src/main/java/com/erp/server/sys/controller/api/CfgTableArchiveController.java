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
import com.erp.model.sys.dto.CfgTableArchiveDTO;
import com.erp.server.sys.service.CfgTableArchiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 归档配置表
 */
@Slf4j
@RestController
@LogSystemModule("归档配置表")
@RequestMapping("/cfgTableArchive")
public class CfgTableArchiveController extends BaseController {

    @Resource
    private CfgTableArchiveService cfgTableArchiveService;

    /**
     * 分页查询
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<CfgTableArchiveDTO.ListDTO>> paging(@RequestBody PagingDTO<CfgTableArchiveDTO.SearchParamDTO> pagingDTO) {
        return success(cfgTableArchiveService.paging(pagingDTO));
    }

    /**
     * 新增
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<Boolean> add(@RequestBody @Validated CfgTableArchiveDTO.AddDTO addDTO) {
        return success(cfgTableArchiveService.add(addDTO));
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新")
    public ApiResult<Boolean> update(@RequestBody @Validated CfgTableArchiveDTO.UpdateDTO updateDTO) {
        return success(cfgTableArchiveService.update(updateDTO));
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.UPDATE, desc = "删除")
    public ApiResult<Boolean> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(cfgTableArchiveService.delete(dto));
    }

    /**
     * 手动触发单条归档
     */
    @PostMapping("/execute")
    @LogAction(value = LogActionEnum.UPDATE, desc = "手动执行归档")
    public ApiResult<Integer> execute(@RequestBody @Validated BaseIdDTO dto) {
        return success(cfgTableArchiveService.executeArchive(dto.getId()));
    }

    /**
     * 手动触发全部归档
     */
    @PostMapping("/executeAll")
    @LogAction(value = LogActionEnum.UPDATE, desc = "手动执行全部归档")
    public ApiResult<Integer> executeAll() {
        return success(cfgTableArchiveService.executeAllArchive());
    }
}
