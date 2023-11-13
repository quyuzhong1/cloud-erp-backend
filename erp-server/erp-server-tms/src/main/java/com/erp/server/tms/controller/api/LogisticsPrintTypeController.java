package com.erp.server.tms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.LogisticsPrintTypeDTO;
import com.erp.server.tms.service.LogisticsPrintTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 面板打印设置表
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@RestController
@LogSystemModule("面板打印设置表")
@RequestMapping("/logisticsPrintType")
public class LogisticsPrintTypeController extends BaseController {

    @Autowired
    private LogisticsPrintTypeService logisticsPrintTypeService;


    /**
    * 修改
    * @author Lambda
    * @date:  2023-11-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsPrintType:update",
        serviceClass = LogisticsPrintTypeService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated LogisticsPrintTypeDTO.UpdateDTO dto) {
        logisticsPrintTypeService.update(dto);
        return success();
    }



}
