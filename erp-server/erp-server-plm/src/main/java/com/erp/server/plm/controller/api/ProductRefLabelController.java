package com.erp.server.plm.controller.api;


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

/**
 * 产品便签关系表
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@RequestMapping("/productRefLabel")
public class ProductRefLabelController extends BaseController {

    @Resource
    private ProductRefLabelService productRefLabelService;

    /**
    * 新增
    * @author Lambda
    * @date:  2023-09-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated ProductRefLabelDTO.AddDTO dto) {
        return success(productRefLabelService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2023-09-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:productRefLabel:update",
        serviceClass = ProductRefLabelService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated ProductRefLabelDTO.UpdateDTO dto) {
        productRefLabelService.update(dto);
        return success();
    }



}
