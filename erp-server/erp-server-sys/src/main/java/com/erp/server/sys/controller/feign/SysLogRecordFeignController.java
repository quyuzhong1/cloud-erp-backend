package com.erp.server.sys.controller.feign;


import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.sys.service.SysLogRecordService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.SysLogRecordDTO;

import javax.annotation.Resource;
import java.util.List;

/**
 * 操作日志
 *
 * @author Jim
 * @since 2023-08-25
 */
@Slf4j
@RestController
@RequestMapping("/feign/sys/log")
public class SysLogRecordFeignController extends BaseController {

    @Resource
    private SysLogRecordService sysLogRecordService;

    /**
    * 新增日志批量添加mq
    */
    @PostMapping("/batchMq")
    public ApiResult<Void> mqBatchSend(@RequestBody @Validated List<SysLogRecordDTO.AddDTO> dto) {
        sysLogRecordService.mqBatchSend(dto);
        return success();
    }


}
