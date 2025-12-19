package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.third.ThirdWarehouseCancelFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateFbaOutboundReq;
import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 发货计划rpc
 * @author will
 * @date 2024/10/18 10:22
 */
@FeignClient(name = "erp-wms", contextId = "b2bThirdDelivery" ,configuration = {FeignErrorDecoder.class})
public interface B2bThirdDeliveryFeign {

    @PostMapping("feign/b2bThirdDelivery/listBySoDetailIds")
    List<B2bThirdDeliveryDetailEntity> listBySoDetailIds(@RequestBody List<String> soDetailIds);

    @PostMapping("feign/b2bThirdDelivery/listBySoIds")
    List<B2bThirdDeliveryEntity> listBySoIds(@RequestBody List<String> soIds);

    @PostMapping("feign/b2bThirdDelivery/createFbaOutbound")
    void createFbaOutbound(@RequestBody ThirdWarehouseCreateFbaOutboundReq req);

    @PostMapping("feign/b2bThirdDelivery/cancelFbaOutbound")
    void cancelFbaOutbound(@RequestBody ThirdWarehouseCancelFbaOutboundReq req);
}
