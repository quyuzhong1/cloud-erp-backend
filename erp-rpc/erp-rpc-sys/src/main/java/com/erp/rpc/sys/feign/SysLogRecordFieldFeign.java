package com.erp.rpc.sys.feign;

import com.erp.model.sys.dto.SysLogRecordFieldDTO;
import com.erp.model.sys.dto.SysLogRecordFieldListDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * SYS系统日志字段保存配置表
 * @author Jim
 * @since 2023-08-29
 **/
@FeignClient(name = "erp-sys", contextId = "logRecordField")
public interface SysLogRecordFieldFeign {

    /**
     * 添加操作日志到mq队列
     **/
    @PostMapping("/feign/logRecordField/list")
    List<SysLogRecordFieldListDTO> list(@RequestBody @Validated SysLogRecordFieldDTO.ListDTO dto);
}
