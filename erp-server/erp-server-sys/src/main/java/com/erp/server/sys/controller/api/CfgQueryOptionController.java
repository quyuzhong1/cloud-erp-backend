package com.erp.server.sys.controller.api;


import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.CfgQueryOptionDTO;
import com.erp.server.sys.service.CfgQueryOptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 查询option配置表
 *
 * @author lrp
 * @since 2024-01-04
 */
@Slf4j
@RestController
@LogSystemModule("查询option配置表")
@RequestMapping("/cfgQueryOption")
public class CfgQueryOptionController extends BaseController {

    @Resource
    private CfgQueryOptionService cfgQueryOptionService;

    /**
     * 分页
     * @author lrp
     * @date:  2024-01-04
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/paging")
    public PagingVO<CfgQueryOptionDTO.ListDTO> paging(@RequestBody @Validated PagingDTO<CfgQueryOptionDTO.ParamDTO> dto) {
        return cfgQueryOptionService.paging(dto);
    }

    /**
    * 新增
    * @author lrp
    * @date:  2024-01-04
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "查询option配置表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgQueryOptionDTO.AddDTO dto) {
        return success(cfgQueryOptionService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2024-01-04
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "查询option配置表修改")
    public ApiResult<?> update(@RequestBody @Validated CfgQueryOptionDTO.UpdateDTO dto) {
        cfgQueryOptionService.update(dto);
        return success();
    }

    /**
     * 删除
     * @author lrp
     * @date:  2024-01-04
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "查询option配置表删除")
    public ApiResult<?> delete(@RequestBody @Validated CfgQueryOptionDTO.UpdateDTO dto) {
        cfgQueryOptionService.delete(dto);
        return success();
    }
}
