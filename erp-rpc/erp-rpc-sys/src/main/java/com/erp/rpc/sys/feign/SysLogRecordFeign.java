package com.erp.rpc.sys.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.sys.dto.SysLogRecordDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 系统日志
 * @author Jim
 * @since 2023-08-25
 **/
@FeignClient(name = "erp-sys", contextId = "sysLogRecordFeign",configuration = {FeignErrorDecoder.class})
public interface SysLogRecordFeign {

    /**
     * 添加操作日志到mq队列
     **/
    @PostMapping("/feign/sys/log/batchMq")
    void addSendMq(@RequestBody @Validated List<SysLogRecordDTO.AddDTO> dto);

}
