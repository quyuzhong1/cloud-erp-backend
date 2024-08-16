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
import com.erp.server.dmp.service.DmpShopInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpShopInfoDTO;

/**
 * 中台店铺表
 *
 * @author Luo_WG
 * @since 2024-08-07
 */
@Slf4j
@RestController
@LogSystemModule("中台店铺表")
@RequestMapping("/dmpShopInfo")
public class DmpShopInfoController extends BaseController {

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2024-08-07
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中台店铺表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpShopInfoDTO.AddDTO dto) {
        return success(dmpShopInfoService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2024-08-07
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "中台店铺表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpShopInfo:update",
        serviceClass = DmpShopInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpShopInfoDTO.UpdateDTO dto) {
        dmpShopInfoService.update(dto);
        return success();
    }



}
