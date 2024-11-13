package com.erp.rpc.dmp.feign;


import com.erp.model.dmp.dto.DmpLogisticsTrackRegisterDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * DMP远程调用物流接口
 *
 * @author zdy
 * @since 2023-11-08
 */
@FeignClient(value = "erp-dmp", path = "feign/trackRegister", contextId = "DmpLogisticsFeign")
public interface DmpLogisticsFeign {

    @PostMapping("/batchAdd")
    void batchAdd(@RequestBody List<DmpLogisticsTrackRegisterDTO.AddDTO> addDTOList);
}