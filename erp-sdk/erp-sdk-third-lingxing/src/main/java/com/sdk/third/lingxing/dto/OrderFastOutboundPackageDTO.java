package com.sdk.third.lingxing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderFastOutboundPackageDTO implements Serializable {

    /**
     * 每个单对应出库信息
     * 	出库包裹信息，最多1000个订单
     */
    @NotNull(message = "出库信息不能为空")
    @Size(max = 1000)
    private List<@NotNull(message = "出库信息不能为空") PackageInfo> packageList;


    @Data
    @NoArgsConstructor
    public static class PackageInfo implements Serializable {
        /**
         * 系统单号
         */
        @NotNull(message = "系统单号不能为空")
        private String globalOrderNo;

        /**
         * 出库仓库ID，可通过查询本地仓库接口获得
         */
        @NotNull(message = "出库仓库ID不能为空")
        private Long wid;

        /**
         * 物流商ID-物流方式ID，物流商ID对应查询已启用的自发货物流方式接口字段【logistics_provider_id】，
         * 物流方式ID对应查询已启用的自发货物流方式接口字段【type_id】
         */
        @NotNull(message = "物流商ID-物流方式ID不能为空")
        private String logisticsTypeId;

        /**
         * 运单号
         */
        @NotNull(message = "运单号不能为空")
        private String waybillNo;

        /**
         * 跟踪号
         */
        private String trackingNo;

        /**
         * 重量单位，可选g、kg，默认g
         */
        private String weightUnit;

        /**
         * 包裹重量
         */
        private String realWeight;

        /**
         * 尺寸单位，可选mm、cm，默认cm
         */
        private String sizeUnit;

        /**
         * 包裹尺寸长
         */
        private String length;

        /**
         * 包裹尺寸宽
         */
        private String width;

        /**
         * 包裹尺寸高
         */
        private String height;

        /**
         * 包裹计费重
         */
        private String feeWeight;

        /**
         * 物流运费
         */
        private String logisticsFreight;

        /**
         * 物流运费币种代码，默认CNY
         */
        private String logisticsFreightCurrencyCode;

    }

}
