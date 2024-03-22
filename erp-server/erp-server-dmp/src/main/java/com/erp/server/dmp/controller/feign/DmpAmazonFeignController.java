package com.erp.server.dmp.controller.feign;

import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.dmp.dto.DmpPullSoOutStockDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.server.dmp.service.AmzBusinessHandleService;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.server.dmp.service.AmzReportHandleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 中台请求亚马逊Feign控制类
 *
 * @Author Cloud
 * @Date 2023/9/1 12:03
 **/
@Slf4j
@RestController
@RequestMapping("feign/dmp")
public class DmpAmazonFeignController {
    @Resource
    private AmzReportHandleService amzReportHandleService;
    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private AmzBusinessHandleService amzBusinessHandleService;



    /**
     * 拉取货件
     *
     * @Author Jim
     * @since 2023-10-10
     **/
    @PostMapping("/amazon/getShipment")
    public Boolean pullShipment(@RequestBody @Valid DmpPullShipmentDTO dto){
        return amzReportHandleService.pullShipment(dto);
    }

    /**
     * 缓存和获取亚马逊授权相关信息
     *
     * @Author Jim
     * @since 2023-12-01
     **/
    @PostMapping ("/amazon/shop")
    public AmazonShopInfoDTO getShopAuth(@RequestBody String shopId){
        return cfgAppClientService.cacheAndFindShopAuth(shopId);
    }

    /**
     * 重推销售出库单
     *
     * @Author Jim
     * @since 2024-03-12
     **/
    @PostMapping("/amazon/checkAndSendSoOutStock")
    public Boolean checkAndSendSoOutStock(@RequestBody DmpPullSoOutStockDTO dto){
        return amzBusinessHandleService.checkAndSendSoOutStock(dto);
    }

}
