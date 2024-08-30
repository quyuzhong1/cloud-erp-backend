package com.erp.server.mrp.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.ReplenishmentRefLabelDTO;
import com.erp.server.mrp.service.ReplenishmentRefLabelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 补货建议标签关系表
 *
 * @author will
 * @since 2024-08-30
 */
@Slf4j
@RestController
@LogSystemModule("补货建议标签关系表")
@RequestMapping("/replenishmentRefLabel")
public class ReplenishmentRefLabelController extends BaseController {

    @Resource
    private ReplenishmentRefLabelService replenishmentRefLabelService;

    /**
    * 新增
    * @author will
    * @date:  2024-08-30
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "补货建议标签关系表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ReplenishmentRefLabelDTO.AddDTO dto) {
        return success(replenishmentRefLabelService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-08-30
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "补货建议标签关系表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "mrp:replenishmentRefLabel:update",
        serviceClass = ReplenishmentRefLabelService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ReplenishmentRefLabelDTO.UpdateDTO dto) {
        replenishmentRefLabelService.update(dto);
        return success();
    }



}
