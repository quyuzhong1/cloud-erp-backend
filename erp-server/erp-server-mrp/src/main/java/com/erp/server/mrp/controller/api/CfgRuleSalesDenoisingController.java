package com.erp.server.mrp.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.CfgRuleSalesDenoisingDTO;
import com.erp.server.mrp.service.CfgRuleSalesDenoisingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 销量去噪信息
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@RestController
@LogSystemModule("销量去噪信息")
@RequestMapping("/cfgRuleSalesDenoising")
public class CfgRuleSalesDenoisingController extends BaseController {

    @Resource
    private CfgRuleSalesDenoisingService cfgRuleSalesDenoisingService;

    /**
    * 新增
    * @author will
    * @date:  2024-08-23
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "销量去噪信息新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgRuleSalesDenoisingDTO.AddDTO dto) {
        return success(cfgRuleSalesDenoisingService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-08-23
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "销量去噪信息修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "mrp:cfgRuleSalesDenoising:update",
        serviceClass = CfgRuleSalesDenoisingService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgRuleSalesDenoisingDTO.UpdateDTO dto) {
        cfgRuleSalesDenoisingService.update(dto);
        return success();
    }



}
