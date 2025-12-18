package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.third.ThirdWarehouseCancelFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateFbaOutboundReq;
import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.server.wms.service.B2bThirdDeliveryDetailService;
import com.erp.server.wms.service.B2bThirdDeliveryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zdy
 * @version 1.0

 * @date 2025/12/11 16:01
 */
@RestController
@RequestMapping("/feign/b2bThirdDelivery")
public class B2bThirdDeliveryFeignController {

    @Resource
    private B2bThirdDeliveryDetailService b2bThirdDeliveryDetailService;
    @Resource
    private B2bThirdDeliveryService b2bThirdDeliveryService;

    @PostMapping("/listBySoDetailIds")
    public List<B2bThirdDeliveryDetailEntity> listBySoDetailIds(@RequestBody List<String> soDetailIds) {
        return b2bThirdDeliveryDetailService.listBySoDetailIds(soDetailIds);
    }

    @PostMapping("/listBySoIds")
    public List<B2bThirdDeliveryEntity> listBySoIds(@RequestBody List<String> soIds) {
        return b2bThirdDeliveryService.listBySoIds(soIds);
    }
    @PostMapping("/createFbaOutbound")
    public void createFbaOutbound(@RequestBody ThirdWarehouseCreateFbaOutboundReq req){
        b2bThirdDeliveryService.createFbaOutbound(req);
    }
    @PostMapping("/cancelFbaOutbound")
    public void cancelFbaOutbound(ThirdWarehouseCancelFbaOutboundReq req){
        b2bThirdDeliveryService.cancelFbaOutbound(req);
    }
}
