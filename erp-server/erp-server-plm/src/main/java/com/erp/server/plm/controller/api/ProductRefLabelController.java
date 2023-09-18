package com.erp.server.plm.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.BasicLabelDTO;
import com.erp.server.plm.service.BasicLabelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.plm.service.ProductRefLabelService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.ProductRefLabelDTO;

import java.util.List;

/**
 * 产品便签关系表
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@LogSystemModule("产品便签关系")
@RequestMapping("/productRefLabel")
public class ProductRefLabelController extends BaseController {

    @Resource
    private ProductRefLabelService productRefLabelService;

    /**
     * 新增产品标签关系
     *
     * @param dtos
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-09-13
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增产品标签关系")
    public ApiResult<String> add(@RequestBody @Validated ProductRefLabelDTO.BatchAddDTO dtos) {
        productRefLabelService.batchAdd(dtos);
        return success();
    }

    /**
     * 删除产品标签关系
     */
    @PostMapping("/remove")
    public ApiResult delete(@RequestBody @Validated ProductRefLabelDTO.RemoveDTO dto) {
        productRefLabelService.removeProductRef(dto);
        return success();
    }

}
