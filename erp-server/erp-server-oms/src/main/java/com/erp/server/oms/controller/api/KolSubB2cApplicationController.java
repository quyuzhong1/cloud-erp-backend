package com.erp.server.oms.controller.api;


import com.erp.model.oms.dto.KolB2cApplicationDTO;
import com.erp.server.oms.service.KolB2cApplicationService;
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
import com.erp.server.oms.service.KolSubB2cApplicationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.KolSubB2cApplicationDTO;

import java.util.List;

/**
 * B2C寄样申请单拆分单
 *
 * @author jack
 * @since 2025-12-04
 */
@Slf4j
@RestController
@LogSystemModule("B2C寄样申请单拆分单")
@RequestMapping("/kolSubB2cApplication")
public class KolSubB2cApplicationController extends BaseController {

    @Resource
    private KolSubB2cApplicationService kolSubB2cApplicationService;

    /**
     * 根据来源id查询关联单据
     * @author jack
     * @date:  2025-12-09
     * @param sourceId
     * @return ApiResult<KolSubB2cApplicationDTO.ListDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2cApplication:view",
            serviceClass = KolB2cApplicationService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<List<KolSubB2cApplicationDTO.ListDTO>> listSubBySourceId(@RequestParam("sourceId") String sourceId) {
        return success(kolSubB2cApplicationService.listSubBySourceId(sourceId));
    }

}
