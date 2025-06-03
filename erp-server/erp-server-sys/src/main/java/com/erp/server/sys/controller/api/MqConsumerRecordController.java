package com.erp.server.sys.controller.api;


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
import com.erp.server.sys.service.MqConsumerRecordService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.MqConsumerRecordDTO;

/**
 * mq消费记录
 *
 * @author jack
 * @since 2025-05-29
 */
@Slf4j
@RestController
@LogSystemModule("mq消费记录")
@RequestMapping("/mqConsumerRecord")
public class MqConsumerRecordController extends BaseController {

    @Resource
    private MqConsumerRecordService mqConsumerRecordService;

    /**
    * 新增
    * @author jack
    * @date:  2025-05-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "mq消费记录新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated MqConsumerRecordDTO.AddDTO dto) {
        return success(mqConsumerRecordService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-05-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "mq消费记录修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "sys:mqConsumerRecord:update",
        serviceClass = MqConsumerRecordService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated MqConsumerRecordDTO.UpdateDTO dto) {
        mqConsumerRecordService.update(dto);
        return success();
    }



}
