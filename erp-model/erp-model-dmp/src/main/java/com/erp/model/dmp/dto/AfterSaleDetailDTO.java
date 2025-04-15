package com.erp.model.dmp.dto;

import java.math.BigDecimal;
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
 * 售后申请明细表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-04-06
*/
@Data
@NoArgsConstructor
public class AfterSaleDetailDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 货值
        */
        private BigDecimal price;

        /**
        * sku维修金额
        */
        private BigDecimal repairAmount;

        /**
        * skuId
        */
        private String skuId;

        /**
        * skuNo
        */
        private String skuNo;

        /**
        * skuName
        */
        private String productName;

        /**
        * 数量
        */
        private Integer skuQty;

        /**
        * 描述
        */
        private String detailDesc;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 货值
        */
        @NotNull(message = "货值不能为空")
        @Digits(integer = 14, fraction = 2, message = "货值整数位不能超过14位，小数位不能超过2位")
        private BigDecimal price;

        /**
        * sku维修金额
        */
        @NotNull(message = "sku维修金额不能为空")
        @Digits(integer = 14, fraction = 2, message = "sku维修金额整数位不能超过14位，小数位不能超过2位")
        private BigDecimal repairAmount;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 32,message = "skuId最大长度不能超过32位")
        private String skuId;

        /**
        * skuName
        */
        @NotBlank(message = "skuName不能为空")
        @Size(max = 64,message = "skuName最大长度不能超过64位")
        private String productName;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer skuQty;

        /**
        * 描述
        */
        @NotBlank(message = "描述不能为空")
        @Size(max = 500,message = "描述最大长度不能超过500位")
        private String detailDesc;


    }


}