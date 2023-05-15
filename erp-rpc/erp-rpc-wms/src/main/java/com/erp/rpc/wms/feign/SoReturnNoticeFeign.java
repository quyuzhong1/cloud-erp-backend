package com.erp.rpc.wms.feign;

import com.erp.model.wms.entity.SoReturnNoticeEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient(name = "erp-wms", contextId = "soReturnNotice")
public interface SoReturnNoticeFeign {
    /**
     * 根据来源id查询销售退货通知单主表
     * @Author Luo_WG
     * @Date 2023/5/15 12:29
     * @param sourceId sourceId
     * @return java.util.List<com.erp.model.wms.entity.SoReturnNoticeEntity>
     **/
    @PostMapping("/feign/soReturnNotice/listDetailBySourceDetailId")
    List<SoReturnNoticeEntity> listDetailBySourceDetailId(@RequestBody String sourceId);
}
