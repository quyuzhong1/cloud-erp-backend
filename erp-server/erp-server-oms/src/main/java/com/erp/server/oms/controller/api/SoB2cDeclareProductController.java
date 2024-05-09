package com.erp.server.oms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.SoB2cDeclareProductService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.SoB2cDeclareProductDTO;

/**
 * B2C销售订单申报产品信息表
 *
 * @author zdy
 * @since 2024-05-09
 */
@Slf4j
@RestController
@LogSystemModule("B2C销售订单申报产品信息表")
@RequestMapping("/soB2cDeclareProduct")
public class SoB2cDeclareProductController extends BaseController {

    @Resource
    private SoB2cDeclareProductService soB2cDeclareProductService;

    /**
    * 新增
    * @author zdy
    * @date:  2024-05-09
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "B2C销售订单申报产品信息表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SoB2cDeclareProductDTO.AddDTO dto) {
        return success(soB2cDeclareProductService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-05-09
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "B2C销售订单申报产品信息表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:soB2cDeclareProduct:update",
        serviceClass = SoB2cDeclareProductService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SoB2cDeclareProductDTO.UpdateDTO dto) {
        soB2cDeclareProductService.update(dto);
        return success();
    }



}
