package com.erp.server.plm.controller.api;


import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.CfgProductCategoryFieldDTO;
import com.erp.server.plm.service.CfgProductCategoryFieldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品分类字段配置
 *
 * @author Lambda
 * @since 2023-11-06
 */
@Slf4j
@RestController
@LogSystemModule("产品分类字段配置")
@RequestMapping("/cfgProductCategoryField")
public class CfgProductCategoryFieldController extends BaseController {

    @Autowired
    private CfgProductCategoryFieldService cfgProductCategoryFieldService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-11-06
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "产品分类字段配置表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgProductCategoryFieldDTO.AddDTO dto) {
        return success(cfgProductCategoryFieldService.add(dto));
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2023-11-06
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "产品分类字段配置表修改")
    public ApiResult<Object> update(@RequestBody @Validated CfgProductCategoryFieldDTO.UpdateDTO dto) {
        cfgProductCategoryFieldService.update(dto);
        return success();
    }


    @LogAction(value = LogActionEnum.DELETE, desc = "产品分类字段配置表删除")
    @PostMapping("/remove")
    public ApiResult<Object> remove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = cfgProductCategoryFieldService.removeByIds(dto.getIds());
        return flag == true ? success() : failure();
    }


}
