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
import com.erp.server.oms.service.KolFeedbackCostService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.KolFeedbackCostDTO;

/**
 * KOL回片费用表
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Slf4j
@RestController
@LogSystemModule("KOL回片费用表")
@RequestMapping("/kolFeedbackCost")
public class KolFeedbackCostController extends BaseController {

    @Resource
    private KolFeedbackCostService kolFeedbackCostService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "KOL回片费用表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KolFeedbackCostDTO.AddDTO dto) {
        return success(kolFeedbackCostService.add(dto));
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "KOL回片费用表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:kolFeedbackCost:update",
        serviceClass = KolFeedbackCostService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KolFeedbackCostDTO.UpdateDTO dto) {
        kolFeedbackCostService.update(dto);
        return success();
    }



}
