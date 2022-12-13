package com.erp.server.bi.controller;


import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.bi.dto.IndicatorSaleDTO;
import com.erp.model.bi.vo.IndicatorSaleSumVO;
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
public class BiIndicatorSaleController extends BaseController {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    /**
     * 销售额
     */
    @PostMapping("/sales")
    public ApiResult countSales(@RequestBody IndicatorSaleDTO dto){

        IndicatorSaleSumVO vo = dmpOrderInfoService.countSales(dto);
        return success(vo);
    }


    /**
     * 销量
     */
    @PostMapping("/sales/volume")
    public ApiResult countSalesVolume(){


        return success();
    }
    /**
     * 订单量
     */
    @PostMapping("/order/quantity")
    public ApiResult countOrderQuantity(){


        return success();
    }

    /**
     * 退款率
     */
    @PostMapping("/refund/rate")
    public ApiResult countRefundRate(){


        return success();
    }
    /**
     * 退款金额
     */
    @PostMapping("/refund/amount")
    public ApiResult countRefundAmount(){


        return success();
    }
    /**
     * 退款订单数
     */
    @PostMapping("/refund/order/number")
    public ApiResult countRefundOrderNumber(){


        return success();
    }
}
