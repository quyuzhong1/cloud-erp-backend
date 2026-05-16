package com.erp.server.sys.controller.api;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.CfgMaskFieldDTO;
import com.erp.server.sys.service.CfgMaskFieldService;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * 字段脱敏配置 控制器（运维侧）
 *
 * @author cloud-erp
 */
@Slf4j
@RestController
@LogSystemModule("字段脱敏配置")
@RequestMapping("/cfgMaskField")
public class CfgMaskFieldController extends BaseController {

    @Resource
    private CfgMaskFieldService cfgMaskFieldService;

    @PostMapping("/paging")
    public ApiResult<PagingVO<CfgMaskFieldDTO.ListDTO>> paging(
            @RequestBody PagingDTO<CfgMaskFieldDTO.SearchParamDTO> dto) {
        return success(cfgMaskFieldService.paging(dto));
    }

    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增字段脱敏配置")
    public ApiResult<Boolean> add(@RequestBody @Validated CfgMaskFieldDTO.AddDTO dto) {
        return success(cfgMaskFieldService.add(dto));
    }

    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改字段脱敏配置")
    public ApiResult<Boolean> update(@RequestBody @Validated CfgMaskFieldDTO.UpdateDTO dto) {
        return success(cfgMaskFieldService.update(dto));
    }

    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.UPDATE, desc = "删除字段脱敏配置")
    public ApiResult<Boolean> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(cfgMaskFieldService.delete(dto));
    }

    /**
     * 强制刷新各节点本地缓存：按当前表数据重建 FullCache 写入 Bucket 并 publish
     */
    @PostMapping("/refresh")
    @LogAction(value = LogActionEnum.UPDATE, desc = "刷新字段脱敏配置缓存")
    public ApiResult<Boolean> refresh() {
        return success(cfgMaskFieldService.publishFullCache());
    }
}
