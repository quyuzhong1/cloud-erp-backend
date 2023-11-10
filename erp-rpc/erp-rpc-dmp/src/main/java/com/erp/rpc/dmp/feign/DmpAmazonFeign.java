package com.erp.rpc.dmp.feign;


import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.dmp.dto.DmpSyncReportScheduleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * DMP远程调用亚马逊SDK接口
 *
 * @author Jim
 * @since 2023-11-08
 */
@FeignClient(value = "erp-dmp", path = "feign/dmp", contextId = "DmpAmazonFeign")
public interface DmpAmazonFeign {

    /**
     * 拉取货件
     *
     * @Author Jim
     * @since 2023-10-10
     **/
    @PostMapping("/amazon/getShipment")
    Boolean pullShipment(@RequestBody DmpPullShipmentDTO dto);


}