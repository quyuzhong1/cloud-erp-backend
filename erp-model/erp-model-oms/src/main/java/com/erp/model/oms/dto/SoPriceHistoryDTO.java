package com.erp.model.oms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-03-24
*/
@Data
@NoArgsConstructor
public class SoPriceHistoryDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * sku 表id
        */
        private String skuId;

        /**
        * 销售价表 id
        */
        private String soPriceId;

        /**
        * sku no
        */
        private String skuNo;

        /**
        * 区间开始数量
        */
        private Integer minQty;

        /**
        * 区间结束数量
        */
        private Integer maxQty;

        /**
        * 币种
        */
        private String currency;

        /**
        * 含税单价
        */
        private BigDecimal taxPrice;

        /**
        * 失效时间
        */
        private LocalDateTime expireDate;

        /**
        * 税率
        */
        private BigDecimal taxRate;

        /**
        * 交期
        */
        private Integer deliveryDay;

        /**
        * 生效时间
        */
        private LocalDateTime effectiveDate;

        /**
        * 销售价目详情表id
        */
        private String priceDetailId;

        /**
        * 客户id
        */
        private String customerId;

        /**
        * 销售价目变更详情id
        */
        private String changeDetailId;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * sku 表id
        */
        @NotBlank(message = "sku 表id不能为空")
        @Size(max = 19,message = "sku 表id最大长度不能超过19位")
        private String skuId;

        /**
        * 销售价表 id
        */
        @NotBlank(message = "销售价表 id不能为空")
        @Size(max = 19,message = "销售价表 id最大长度不能超过19位")
        private String soPriceId;

        /**
        * 区间开始数量
        */
        @NotNull(message = "区间开始数量不能为空")
        private Integer minQty;

        /**
        * 区间结束数量
        */
        @NotNull(message = "区间结束数量不能为空")
        private Integer maxQty;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        @Size(max = 30,message = "币种最大长度不能超过30位")
        private String currency;

        /**
        * 含税单价
        */
        @NotNull(message = "含税单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxPrice;

        /**
        * 失效时间
        */
        @NotNull(message = "失效时间不能为空")
        private LocalDateTime expireDate;

        /**
        * 税率
        */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 12, fraction = 4, message = "税率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxRate;

        /**
        * 交期
        */
        @NotNull(message = "交期不能为空")
        private Integer deliveryDay;

        /**
        * 生效时间
        */
        @NotNull(message = "生效时间不能为空")
        private LocalDateTime effectiveDate;

        /**
        * 销售价目详情表id
        */
        @NotBlank(message = "销售价目详情表id不能为空")
        @Size(max = 19,message = "销售价目详情表id最大长度不能超过19位")
        private String priceDetailId;

        /**
        * 客户id
        */
        @NotBlank(message = "客户id不能为空")
        @Size(max = 19,message = "客户id最大长度不能超过19位")
        private String customerId;

        /**
        * 销售价目变更详情id
        */
        @NotBlank(message = "销售价目变更详情id不能为空")
        @Size(max = 19,message = "销售价目变更详情id最大长度不能超过19位")
        private String changeDetailId;


    }


}