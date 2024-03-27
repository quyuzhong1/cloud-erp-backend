package com.erp.server.tms.controller.api;


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
import com.erp.server.tms.service.TmsDeclareBillService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TmsDeclareBillDTO;

/**
 * 报关单
 *
 * @author lrp
 * @since 2024-03-27
 */
@Slf4j
@RestController
@LogSystemModule("报关单")
@RequestMapping("/tmsDeclareBill")
public class TmsDeclareBillController extends BaseController {

    @Resource
    private TmsDeclareBillService tmsDeclareBillService;

    /**
    * 新增
    * @author lrp
    * @date:  2024-03-27
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "报关单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TmsDeclareBillDTO.AddDTO dto) {
        return success(tmsDeclareBillService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2024-03-27
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "报关单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:tmsDeclareBill:update",
        serviceClass = TmsDeclareBillService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated TmsDeclareBillDTO.UpdateDTO dto) {
        tmsDeclareBillService.update(dto);
        return success();
    }



}
