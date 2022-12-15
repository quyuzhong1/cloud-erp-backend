package com.erp.server.bi.controller;


import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.bi.dto.TargetSaleDTO;
import com.erp.model.bi.vo.TargetSaleCountVO;
import com.erp.model.bi.vo.TargetSaleSumVO;
import com.erp.server.bi.service.DmpOrderInfoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * BI报表指标模块销售数据
 *
 * @author Cloud
 */
@RestController
@RequestMapping("bi/indicator/sale")
public class BiTargetSaleController extends BaseController {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    /**
     * 销售额  区分新老品
     */
    @PostMapping("/sales")
    public ApiResult countSales(@RequestBody TargetSaleDTO dto){
        TargetSaleSumVO vo = dmpOrderInfoService.sumSales(dto);
        return success(vo);
    }

    /**
     * 销量
     */
    @PostMapping("/sales/volume")
    public ApiResult countSalesVolume(@RequestBody TargetSaleDTO dto){
        TargetSaleCountVO vo = dmpOrderInfoService.countSalesVolume(dto);
        return success(vo);
    }

    /**
     * 订单量
     */
    @PostMapping("/order/quantity")
    public ApiResult countOrderQuantity(@RequestBody TargetSaleDTO dto){
        TargetSaleCountVO vo = dmpOrderInfoService.countOrderQuantity(dto);
        return success(vo);
    }

    /**
     * 退款率
     */
    @PostMapping("/refund/rate")
    public ApiResult countRefundRate(@RequestBody TargetSaleDTO dto){
        TargetSaleSumVO vo = dmpOrderInfoService.countRefundRate(dto);
        return success(vo);
    }

    /**
     * 退款金额
     */
    @PostMapping("/refund/amount")
    public ApiResult countRefundAmount(@RequestBody TargetSaleDTO dto){
        TargetSaleSumVO vo = dmpOrderInfoService.countRefundAmount(dto);
        return success(vo);
    }

    /**
     * 退款订单数
     */
    @PostMapping("/refund/order/number")
    public ApiResult countRefundOrderNumber(@RequestBody TargetSaleDTO dto){
        TargetSaleCountVO vo = dmpOrderInfoService.countRefundOrderNum(dto);
        return success(vo);
    }

    /**
     * 客单价
     */
    @PostMapping("/customer/price")
    public ApiResult calculateCustomerPrice(@RequestBody TargetSaleDTO dto){
        TargetSaleSumVO vo = dmpOrderInfoService.statisticsCustomerPrice(dto);
        return success(vo);
    }


    /**
     * B2B 新客户营业额
     */

    /**
     * 国内销售占比
     */
    @PostMapping("/domestic/sales/ratio")
    public ApiResult domesticSalesRatio(@RequestBody TargetSaleDTO dto){
        TargetSaleSumVO vo = dmpOrderInfoService.statisticsDomesticSalesRatio(dto);
        return success(vo);
    }
}
