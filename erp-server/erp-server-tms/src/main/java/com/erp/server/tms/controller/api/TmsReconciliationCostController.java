package com.erp.server.tms.controller.api;


import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.TmsReconciliationCostDTO;
import com.erp.server.tms.service.TmsReconciliationCostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 对账费用单
 *
 * @author will
 * @since 2024-03-26
 */
@Slf4j
@RestController
@LogSystemModule("对账费用单")
@RequestMapping("/tmsReconciliationCost")
public class TmsReconciliationCostController extends BaseController {

    @Resource
    private TmsReconciliationCostService tmsReconciliationCostService;

    /**
    * 新增
    * @author will
    * @date:  2024-03-26
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "对账费用单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TmsReconciliationCostDTO.AddDTO dto) {
        return success(tmsReconciliationCostService.add(dto));
    }
}
