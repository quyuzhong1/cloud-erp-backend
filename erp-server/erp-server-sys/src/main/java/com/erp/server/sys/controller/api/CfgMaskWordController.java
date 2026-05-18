package com.erp.server.sys.controller.api;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.CfgMaskWordDTO;
import com.erp.server.sys.service.CfgMaskWordService;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * 脱敏词典 控制器（运维侧）
 *
 * @author cloud-erp
 */
@Slf4j
@RestController
@LogSystemModule("脱敏词典")
@RequestMapping("/cfgMaskWord")
public class CfgMaskWordController extends BaseController {

    @Resource
    private CfgMaskWordService cfgMaskWordService;

    @PostMapping("/paging")
    public ApiResult<PagingVO<CfgMaskWordDTO.ListDTO>> paging(
            @RequestBody PagingDTO<CfgMaskWordDTO.SearchParamDTO> dto) {
        return success(cfgMaskWordService.paging(dto));
    }

    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增脱敏词典")
    public ApiResult<Boolean> add(@RequestBody @Validated CfgMaskWordDTO.AddDTO dto) {
        return success(cfgMaskWordService.add(dto));
    }

    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改脱敏词典")
    public ApiResult<Boolean> update(@RequestBody @Validated CfgMaskWordDTO.UpdateDTO dto) {
        return success(cfgMaskWordService.update(dto));
    }

    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.UPDATE, desc = "删除脱敏词典")
    public ApiResult<Boolean> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(cfgMaskWordService.delete(dto));
    }

    /**
     * 强制刷新 Redis 缓存：按当前表数据重建 FullCache 写入 Redis
     */
    @PostMapping("/refresh")
    @LogAction(value = LogActionEnum.UPDATE, desc = "刷新脱敏词典缓存")
    public ApiResult<Boolean> refresh() {
        return success(cfgMaskWordService.publishFullCache());
    }
}
