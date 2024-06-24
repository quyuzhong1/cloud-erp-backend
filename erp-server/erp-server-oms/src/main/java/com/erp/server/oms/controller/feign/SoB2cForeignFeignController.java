package com.erp.server.oms.controller.feign;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.erp.model.oms.dto.SoB2cForeignDTO;
import com.erp.server.oms.service.SoB2cForeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * B2C订单外部接口
 */
@Slf4j
@RestController
@RequestMapping("/foreign/feign/soB2c")
public class SoB2cForeignFeignController extends BaseController {

    @Resource
    private SoB2cForeignService soB2cForeignService;

    /**
     * 订单发货信息接口
     */
    @PostMapping("/getB2cOrderDeliveryInfo")
    public PagingVO<SoB2cForeignDTO.OrderDeliveryResp> getOrderDeliveryInfo(@RequestBody PagingDTO<SoB2cForeignDTO.OrderDeliveryReq> orderDeliveryReq){
        return soB2cForeignService.getOrderDeliveryInfo(orderDeliveryReq);
    }

}
