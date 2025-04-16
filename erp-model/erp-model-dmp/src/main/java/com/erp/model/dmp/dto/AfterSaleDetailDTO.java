package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

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
        private String id;

    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

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
        @NotBlank(message = "skuId不能为空")
        @Size(max = 32,message = "skuId最大长度不能超过32位")
        private String skuId;

        /**
        * skuName
        */
        private String productName;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer skuQty;

        /**
        * 描述
        */
        private String detailDesc;


    }


}