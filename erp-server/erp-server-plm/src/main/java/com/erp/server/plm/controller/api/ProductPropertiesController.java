package com.erp.server.plm.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.plm.service.ProductPropertiesService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.ProductPropertiesDTO;

/**
 * sku与配置字段关系表
 *
 * @author Lambda
 * @since 2023-11-08
 */
@Slf4j
@RestController
@LogSystemModule("sku与配置字段关系表")
@RequestMapping("/productProperties")
public class ProductPropertiesController extends BaseController {

    @Autowired
    private ProductPropertiesService productPropertiesService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-11-08
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "sku与配置字段关系表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ProductPropertiesDTO.AddDTO dto) {
        return success(productPropertiesService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2023-11-08
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "sku与配置字段关系表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:productProperties:update",
        serviceClass = ProductPropertiesService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated ProductPropertiesDTO.UpdateDTO dto) {
        productPropertiesService.update(dto);
        return success();
    }



}
