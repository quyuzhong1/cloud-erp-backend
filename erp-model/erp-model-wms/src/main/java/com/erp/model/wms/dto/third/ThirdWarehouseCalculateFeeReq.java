package com.erp.model.wms.dto.third;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ThirdWarehouseCalculateFeeReq extends ThirdWarehouseAuth{

    //	发货仓库代码
    private String warehouseCode;

    //目的国家代码
    private String countryCode;
    //渠道编码
    private String channelCode;

    //配送方式
    private List<String> shippingMethod;

    //邮政编码
    private String postCode;

    //包裹重量
    private BigDecimal weight;

    //包裹长
    private BigDecimal length;

    //包裹宽
    private BigDecimal width;

    //包裹高
    private BigDecimal height;

    //地址1
    private String address1;

    //省/州
    private String province;
    //城市
    private String city;
    //sku
    private List<String> skuList;

    private List<SkusDTO> skus;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SkusDTO {

        private String skuNo;

        private String platformSkuNo;

        private Integer qty;
    }
}
