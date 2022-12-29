package com.erp.server.bi.controller;


import com.erp.common.business.annotation.DataPermission;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.enums.DataAttributeEnum;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.TargetSaleCountVO;
import com.erp.model.bi.vo.TargetSaleSumVO;
import com.erp.server.bi.service.DmpOrderInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * BI报表指标
 *
 * @author Cloud
 */
@RestController
@RequestMapping("bi/indicator/sale")
public class BiTargetSaleController extends BaseController {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    /**
     * 指标-销售额（区分新老品）
     * menuCode = "bi:indicator:sales"
     * code = indicator_sales
     */
    @PostMapping("/sales")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:indicator:sales",
//            tableAlias = "dmp_order_info"
//    )
    public ApiResult countSales(@RequestBody @Validated BiFilterDTO dto){
        TargetSaleSumVO vo = dmpOrderInfoService.sumSales(dto);
        return success(vo);
    }

    /**
     * 指标-销量
     * menuCode = "bi:indicator:sales:volume"
     * code = indicator_volume
     */
    @PostMapping("/sales/volume")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:indicator:sales:volume",
//            tableAlias = "dmp_order_info"
//    )
    public ApiResult countSalesVolume(@RequestBody @Validated BiFilterDTO dto){
        TargetSaleCountVO vo = dmpOrderInfoService.countSalesVolume(dto);
        return success(vo);
    }

    /**
     * 指标-订单量
     * menuCode = "bi:indicator:order:quantity"
     * code = indicator_quantity
     */
    @PostMapping("/order/quantity")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:indicator:order:quantity",
//            tableAlias = "dmp_order_info"
//    )
    public ApiResult countOrderQuantity(@RequestBody @Validated BiFilterDTO dto){
        TargetSaleCountVO vo = dmpOrderInfoService.countOrderQuantity(dto);
        return success(vo);
    }

    /**
     * 指标-退款率
     * menuCode = "bi:indicator:refund:rate"
     * code = indicator_refund_rate
     */
    @PostMapping("/refund/rate")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:indicator:refund:rate",
//            tableAlias = "dmp_order_info"
//    )
    public ApiResult countRefundRate(@RequestBody @Validated BiFilterDTO dto){
        TargetSaleSumVO vo = dmpOrderInfoService.countRefundRate(dto);
        return success(vo);
    }

    /**
     * 指标-退款金额
     * menuCode = "bi:indicator:refund:amount"
     * code = indicator_refund_amount
     */
    @PostMapping("/refund/amount")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:indicator:refund:amount",
//            tableAlias = "dmp_order_info"
//    )
    public ApiResult countRefundAmount(@RequestBody @Validated BiFilterDTO dto){
        TargetSaleSumVO vo = dmpOrderInfoService.countRefundAmount(dto);
        return success(vo);
    }

    /**
     * 指标-退款订单数
     * menuCode = "bi:indicator:refund:order:number"
     * code = indicator_refund_order
     */
    @PostMapping("/refund/order/number")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:indicator:refund:order:number",
//            tableAlias = "dmp_order_info"
//    )
    public ApiResult countRefundOrderNumber(@RequestBody @Validated BiFilterDTO dto){
        TargetSaleCountVO vo = dmpOrderInfoService.countRefundOrderNum(dto);
        return success(vo);
    }

    /**
     * 指标-客单价
     * menuCode = "bi:indicator:customer:price"
     * code = indicator_customer_price
     */
    @PostMapping("/customer/price")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:indicator:customer:price",
//            tableAlias = "dmp_order_info"
//    )
    public ApiResult calculateCustomerPrice(@RequestBody @Validated BiFilterDTO dto){
        TargetSaleSumVO vo = dmpOrderInfoService.statisticsCustomerPrice(dto);
        return success(vo);
    }


    /**
     * 指标-B2B新客户营业额
     */

    /**
     * 指标-国内销售占比
     * menuCode = "bi:indicator:domestic:ratio"
     * code = indicator_domestic_ratio
     */
    @PostMapping("/domestic/sales/ratio")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:indicator:domestic:ratio",
//            tableAlias = "dmp_order_info"
//    )
    public ApiResult domesticSalesRatio(@RequestBody @Validated BiFilterDTO dto){
        TargetSaleSumVO vo = dmpOrderInfoService.statisticsDomesticSalesRatio(dto);
        return success(vo);
    }

    /**
     * 指标-销售额环比
     * menuCode = "bi:indicator:ring:ratio"
     * code = indicator_ring_ratio
     */
    @PostMapping("/ring/ratio")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:indicator:ring:ratio",
//            tableAlias = "dmp_order_info"
//    )
    public ApiResult countRingRatio(@RequestBody @Validated BiFilterDTO dto){
        TargetSaleSumVO vo = dmpOrderInfoService.statisticsRingRatio(dto);
        return success(vo);
    }

    /**
     * 指标-销售额同比
     * menuCode = "bi:indicator:yoy:ratio"
     * code = indicator_yoy_ratio
     */
    @PostMapping("/yoy/ratio")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:indicator:yoy:ratio",
//            tableAlias = "dmp_order_info"
//    )
    public ApiResult countYoyRatio(@RequestBody @Validated BiFilterDTO dto){
        TargetSaleSumVO vo = dmpOrderInfoService.statisticsYoyRatio(dto);
        return success(vo);
    }
}
