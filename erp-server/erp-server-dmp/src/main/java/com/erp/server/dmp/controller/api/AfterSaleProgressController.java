package com.erp.server.dmp.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.server.dmp.service.AfterSaleProgressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 售后进度记录表
 *
 * @author jack
 * @since 2025-04-03
 */
@Slf4j
@RestController
@LogSystemModule("售后进度记录表")
@RequestMapping("/afterSaleProgress")
public class AfterSaleProgressController extends BaseController {

    @Resource
    private AfterSaleProgressService afterSaleProgressService;

    /**
    * 新增
    * @author jack
    * @date:  2025-04-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "售后进度记录表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AfterSaleProgressDTO.AddDTO dto) {
        return success(afterSaleProgressService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-04-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "售后进度记录表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:afterSaleProgress:update",
        serviceClass = AfterSaleProgressService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AfterSaleProgressDTO.UpdateDTO dto) {
        afterSaleProgressService.update(dto);
        return success();
    }



}
