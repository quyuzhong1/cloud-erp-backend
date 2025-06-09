package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.CfgRuleWaveRecordDTO;
import com.erp.server.wms.service.CfgRuleWaveRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 波次规则执行记录表
 *
 * @author will
 * @since 2024-07-01
 */
@Slf4j
@RestController
@LogSystemModule("波次规则执行记录表")
@RequestMapping("/cfgRuleWaveRecord")
public class CfgRuleWaveRecordController extends BaseController {

    @Resource
    private CfgRuleWaveRecordService cfgRuleWaveRecordService;

    /**
    * 新增
    * @author will
    * @date:  2024-07-01
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "波次规则执行记录表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgRuleWaveRecordDTO.AddDTO dto) {
        return success(cfgRuleWaveRecordService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-07-01
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "波次规则执行记录表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:cfgRuleWaveRecord:update",
        serviceClass = CfgRuleWaveRecordService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated CfgRuleWaveRecordDTO.UpdateDTO dto) {
        cfgRuleWaveRecordService.update(dto);
        return success();
    }



}
