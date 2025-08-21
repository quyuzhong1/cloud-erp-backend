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
import com.erp.server.oms.service.ExhibitionOrderDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.ExhibitionOrderDetailDTO;

/**
 * 展会订单详情
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@RestController
@LogSystemModule("展会订单详情")
@RequestMapping("/exhibitionOrderDetail")
public class ExhibitionOrderDetailController extends BaseController {

    @Resource
    private ExhibitionOrderDetailService exhibitionOrderDetailService;

    /**
    * 新增
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "展会订单详情新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ExhibitionOrderDetailDTO.AddDTO dto) {
        return success(exhibitionOrderDetailService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "展会订单详情修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:exhibitionOrderDetail:update",
        serviceClass = ExhibitionOrderDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ExhibitionOrderDetailDTO.UpdateDTO dto) {
        exhibitionOrderDetailService.update(dto);
        return success();
    }



}
