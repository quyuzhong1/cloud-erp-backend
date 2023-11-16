package com.erp.server.wms.controller.api;


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
import com.erp.server.wms.service.FirstMileCartonBillService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.FirstMileCartonBillDTO;

/**
 * 发货单箱子信息明细表
 *
 * @author Luo_wg
 * @since 2023-11-16
 */
@Slf4j
@RestController
@LogSystemModule("发货单箱子信息明细表")
@RequestMapping("/firstMileCartonBill")
public class FirstMileCartonBillController extends BaseController {

    @Resource
    private FirstMileCartonBillService firstMileCartonBillService;

    /**
    * 新增
    * @author Luo_wg
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "发货单箱子信息明细表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated FirstMileCartonBillDTO.AddDTO dto) {
        return success(firstMileCartonBillService.add(dto));
    }

    /**
    * 修改
    * @author Luo_wg
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "发货单箱子信息明细表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:firstMileCartonBill:update",
        serviceClass = FirstMileCartonBillService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated FirstMileCartonBillDTO.UpdateDTO dto) {
        firstMileCartonBillService.update(dto);
        return success();
    }



}
