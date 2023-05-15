package com.erp.rpc.wms.feign;

import com.erp.model.wms.entity.SoReturnNoticeEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient(name = "erp-oms", contextId = "soReturnNotice")
public interface SoReturnNoticeFeign {

    @PostMapping("/feign/soReturnNotice/listDetailBySourceDetailId")
    List<SoReturnNoticeEntity> listDetailBySourceDetailId(@RequestBody String sourceId);
}
