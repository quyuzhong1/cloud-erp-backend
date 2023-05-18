package com.erp.rpc.wms.feign;

import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "soDeliveryNotice")
public interface SoDeliveryNoticeFeign {

    @PostMapping("feign/soDeliveryNotice/listDetailBySourceDetailId")
    List<SoDeliveryNoticeDetailEntity> listDetailBySourceDetailIds(@RequestBody List<String> sourceDetailId);
}
