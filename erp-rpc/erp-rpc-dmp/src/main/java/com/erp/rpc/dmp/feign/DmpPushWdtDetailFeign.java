package com.erp.rpc.dmp.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.dmp.dto.DmpPushWdtDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 推送旺店通中间表明细Feign
 * @date 2024-07-25
 * @author tanmujin
 */
@FeignClient(value = "erp-dmp", path = "/feign/dmpPushWdtDetail", contextId = "dmpPushWdtDetail",configuration = {FeignErrorDecoder.class})
public interface DmpPushWdtDetailFeign {

    @PostMapping("/addBatch")
    Boolean addBatch(@RequestBody List<DmpPushWdtDetailDTO> dtoList);
}
