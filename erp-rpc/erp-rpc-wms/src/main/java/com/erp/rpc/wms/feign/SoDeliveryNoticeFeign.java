package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "soDeliveryNotice")
public interface SoDeliveryNoticeFeign {

    @PostMapping("feign/soDeliveryNotice/listDetailBySourceDetailId")
    List<SoDeliveryNoticeDetailEntity> listDetailBySourceDetailIds(@RequestBody List<String> sourceDetailId);

    /**
     * 根据来源id 集合获取到审核通过的数据
     * @author yl
     * @date 2023-06-26 10:27
     * @param sourceIdList
     * @return java.util.List<com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO.ListDTO>
     */
    @PostMapping("feign/soDeliveryNotice/listBySourceIdList")
    List<SoDeliveryNoticeDetailDTO.ListDTO> listBySourceIdList(@RequestBody List<String> sourceIdList);
}
