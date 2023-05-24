package com.erp.model.oms.dto;

import com.common.core.anno.StateEnumValue;
import com.erp.model.oms.enums.SoChangeTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Lambda
 * @Classname SoInfoDTO
 * @Description TODO
 * @Date 2023-05-10 17:55
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SoChangeDetailDTO implements Serializable {


    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {


        /**
         * 销售订单详情id
         */
        @NotBlank(message = "销售订单详情不能为空")
        private String  soDetailId;

        /**
         * sku id
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;

        /**
         * 变更类型
         * 来源 http://172.16.100.11:3002/project/110/interface/api/13480  type=SoChangeType
         */
        @NotNull(message = "变更类型不能为空")
        @StateEnumValue(clazz = SoChangeTypeEnum.class, message = "变更类型输入值有误")
        private SoChangeTypeEnum changeType;

        /**
         * 销售数量
         */
        @NotNull(message = "销售数量不能为空")
        @DecimalMax(value = "999999999",message ="销售数量最大值")
        private Integer qty;

        /**
         * 单价
         */
        @NotNull(message = "销售单价不能为空")
        private BigDecimal price;


        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空")
        private String currency;


        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 是否赠品
         */
        @NotNull(message = "是否赠品不能为空")
        private Boolean isGift;

        /**
         * 是否补发
         */
        @NotNull(message = "是否补发不能为空")
        private Boolean isReissue;

        /**
         * 是否关闭
         */
        private Boolean isClose;


        /**
         * 备注
         */
        @Size(max = 200, message = "备注最大200字符")
        private String remark;


    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * id
         */
        private String id;


        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 变更类型
         */
        private String type;

        /**
         * 销售数量
         */
        private Integer qty;

        /**
         * 原销售数量
         */
        private Integer oldQty;




        /**
         * 单价
         */
        private BigDecimal price;

        /**
         * 原销售单价
         */
        private BigDecimal oldPrice;


        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 原税率
         */
        private BigDecimal oldRate;


        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 原含税单价
         */
        private BigDecimal oldTaxPrice;

        /**
         * 单位
         */
        private String unit;

        /**
         * 销售金额
         */
        private BigDecimal amount;

        /**
         * 原销售金额
         */
        private BigDecimal oldAmount;


        /**
         * 是否赠品
         */
        private Boolean isGift;

        /**
         * 是否补发
         */
        private Boolean isReissue;


        /**
         * 备注
         */
        private String remark;


    }


    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public class UpdateDTO {


        private String id;


        /**
         * sku id
         */
        private String skuId;

        /**
         * 销售数量
         */
        private Integer qty;

        /**
         * 单价
         */
        private BigDecimal price;


        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 是否赠品
         */
        private Boolean isGift;

        /**
         * 是否补发
         */
        private Boolean isReissue;

        /**
         * 是否关闭
         */
        private Boolean isClose;


        /**
         * 备注
         */
        private String remark;
    }


    /**
     * SKU 信息
     */
    @Data
    @NoArgsConstructor
    public static class SkuDTO {

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 销售数量
         */
        private Integer qty;

        /**
         * 缺货数量
         */
        private Integer scarceQty;

        /**
         * 可出数量
         */
        private Integer availableQty;

        /**
         * 已经出库数量
         */
        private Integer deliveryQty;

        /**
         * 剩余数量
         */
        private Integer waitQty;


        /**
         * 单价
         */
        private BigDecimal price;


        /**
         * 税率
         */
        private BigDecimal taxRate;


        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 单位
         */
        private String unit;

        /**
         * 销售金额
         */
        private BigDecimal amount;

        /**
         * 及时库存
         */
        private Integer curInventoryQty;


        /**
         * 历史最高
         */
        private BigDecimal maxPrice;

        /**
         * 历史最低
         */
        private BigDecimal minPrice;

        /**
         * 平均价格
         */
        private BigDecimal avgPrice;


        /**
         * 是否赠品
         */
        private Boolean isGift;

        /**
         * 是否补发
         */
        private Boolean isReissue;

        /**
         * 是否关闭
         */
        private Boolean isClose;


        /**
         * 备注
         */
        private String remark;


    }

    /**
     * 导入返回
     */
    @Data
    @NoArgsConstructor
    public static class ImportDTO {

        /**
         * 成功返回数据
         */
        private List<SkuDTO> successList;

        /**
         * 错误的url
         */
        private String errorUrl;
    }
}
