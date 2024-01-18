package com.erp.server.dmp.controller.api;


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
import com.erp.server.dmp.service.DmpSkuCostCustomService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpSkuCostCustomDTO;

/**
 * sku自定义成本表
 *
 * @author will
 * @since 2024-01-04
 */
@Slf4j
@RestController
@LogSystemModule("sku自定义成本表")
@RequestMapping("/dmpSkuCostCustom")
public class DmpSkuCostCustomController extends BaseController {

    @Resource
    private DmpSkuCostCustomService dmpSkuCostCustomService;

    /**
    * 新增
    * @author will
    * @date:  2024-01-04
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "sku自定义成本表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpSkuCostCustomDTO.AddDTO dto) {
        return success(dmpSkuCostCustomService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-01-04
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "sku自定义成本表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpSkuCostCustom:update",
        serviceClass = DmpSkuCostCustomService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpSkuCostCustomDTO.UpdateDTO dto) {
        dmpSkuCostCustomService.update(dto);
        return success();
    }



}
