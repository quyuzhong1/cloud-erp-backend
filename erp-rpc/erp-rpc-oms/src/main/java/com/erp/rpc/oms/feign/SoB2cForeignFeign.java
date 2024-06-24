package com.erp.rpc.oms.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cForeignDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "soB2cForeign")
public interface SoB2cForeignFeign {

    /**
     * 根据b2c订单id获取物流信息
     */
    @PostMapping("/foreign/feign/soB2c/getB2cOrderDeliveryInfo")
    PagingVO<SoB2cForeignDTO.OrderDeliveryResp> getOrderDeliveryInfo(@RequestBody PagingDTO<SoB2cForeignDTO.OrderDeliveryReq> orderDeliveryReq);

}
