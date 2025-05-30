package com.erp.oms.aliexpress.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @author Lambda
 * @Classname DeclareDeliverRequest
 * @Description 声明发货参数
 * @Date 2023-12-01 10:22
 * @Created by yl
 */
@Data
@Builder
public class DeclareDeliverRequest implements Serializable {
    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 店铺名
     */
    private String shopName;


    /**
     * 运单号
     */
    private String logisticsNo;

    /**
     * 声明发货类型，all表示全部发货，part表示部分声明发货。
     */
    private String sendType;


    /**
     * 交易订单号
     */
    private String outRef;

    /**
     * 物流服务名称 原生渠道服务名 对应logistics_sale_channel 表supplier_name
     */
    @NotBlank(message = "物流服务商不能为空")
    private String serviceName;


    /**
     * 实际承运商--物流方案为“卖家自定义-中国”(OTHER)时该字段为必填
     * （非必须）
     */
    private String actualCarrier;

    /**
     * 运单号轨迹查询地址-物流方案为自定义(OTHER开头)的时候必填
     * （非必须）
     */
    private String trackingWebSite;

    /**
     * 子订单下标
     */
    private List<String> subTradeOrderIndexList;

    /**
     * 子订单下标和类型
     */
    public List<SubTradeOrderDTO> subTradeOrderDTOList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubTradeOrderDTO {

        /**
         * 发货类型
         */
        private String sendType;

        /**
         * 下标
         */
        private String subTradeOrderIndex;

    }
}
