package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ShopifyServerSoB2cDTO;
import com.erp.model.oms.dto.SoB2cForeignDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

@FeignClient(name = "erp-oms", contextId = "soB2cForeignFeign",configuration = {FeignErrorDecoder.class})
public interface SoB2cForeignFeign {

    /**
     * 根据b2c订单id获取物流信息
     */
    @PostMapping("/foreign/feign/soB2c/getB2cOrderDeliveryInfo")
    PagingVO<SoB2cForeignDTO.OrderDeliveryResp> getOrderDeliveryInfo(@RequestBody PagingDTO<SoB2cForeignDTO.OrderDeliveryReq> orderDeliveryReq);

    /**
     * 根据运单号/订单号Email或PhoneNumber获取Shopify服务物流信息
     */
    @PostMapping("/foreign/feign/soB2c/getShopifyLogisticInfo")
    List<ShopifyServerSoB2cDTO.SoB2cLogisticInfoDTO> getShopifyLogisticInfo(@RequestBody ShopifyServerSoB2cDTO.SoB2cLogisticQueryDTO dto);
}
