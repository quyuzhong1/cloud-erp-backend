package com.erp.server.tms.controller.api;


import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.server.tms.service.TransferDeclareDetailService;
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
import com.erp.server.tms.service.TransferDeclareProductService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TransferDeclareProductDTO;

import java.util.Collections;
import java.util.List;

/**
 * 中转报关产品
 *
 * @author zdy
 * @since 2024-01-27
 */
@Slf4j
@RestController
@LogSystemModule("中转报关产品")
@RequestMapping("/transferDeclareProduct")
public class TransferDeclareProductController extends BaseController {

    @Resource
    private TransferDeclareProductService transferDeclareProductService;
    @Resource
    private TransferDeclareDetailService transferDeclareDetailService;

    /**
    * 新增
    * @author zdy
    * @date:  2024-01-27
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中转报关产品新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TransferDeclareProductDTO.AddDTO dto) {
        return success(transferDeclareProductService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-01-27
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "中转报关产品修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:transferDeclareProduct:update",
        serviceClass = TransferDeclareProductService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated TransferDeclareProductDTO.UpdateDTO dto) {
        transferDeclareProductService.update(dto);
        return success();
    }

}
