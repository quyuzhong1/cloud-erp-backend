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
import com.erp.server.dmp.service.ThirdLogisticsService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.ThirdLogisticsDTO;

/**
 * 三方渠道表
 *
 * @author lrp
 * @since 2024-12-11
 */
@Slf4j
@RestController
@LogSystemModule("三方渠道表")
@RequestMapping("/thirdLogistics")
public class ThirdLogisticsController extends BaseController {

    @Resource
    private ThirdLogisticsService thirdLogisticsService;

    /**
    * 新增
    * @author lrp
    * @date:  2024-12-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "三方渠道表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ThirdLogisticsDTO.AddDTO dto) {
        return success(thirdLogisticsService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2024-12-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "三方渠道表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:thirdLogistics:update",
        serviceClass = ThirdLogisticsService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ThirdLogisticsDTO.UpdateDTO dto) {
        thirdLogisticsService.update(dto);
        return success();
    }



}
