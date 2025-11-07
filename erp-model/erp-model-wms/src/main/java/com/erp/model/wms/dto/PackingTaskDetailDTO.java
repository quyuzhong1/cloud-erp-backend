package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 装箱任务明细表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-07-02
*/
@Data
@NoArgsConstructor
public class PackingTaskDetailDTO implements Serializable {




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
         * 客户PO号
         */
        private String customerPO;
        /**
        * 主表id
        */
        private String mainId;

        /**
        * skuId
        */
        private String skuId;

        /**
        * skuNo
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 发货数量
        */
        private Integer deliveryQty;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * fnSku
        */
        private String fnSku;
        /**
         * ean
         */
        private String ean;
        /**
         * 三方仓商品条码
         */
        private String thirdBarcode;
        /**
         * 来源编码
         */
        private String sourceCode;


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
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 255,message = "产品名称最大长度不能超过255位")
        private String productName;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * fnSku
        */
        @NotBlank(message = "fnSku不能为空")
        @Size(max = 64,message = "fnSku最大长度不能超过64位")
        private String fnSku;

        private String sourceCode;


    }


    @Data
    @NoArgsConstructor
    public static class HistoryCartonDTO {
        private String taskId;
        private String sourceId;
        private String sourceType;
        private Integer boxSpecNo;
        private BigDecimal packageWeight;
        private BigDecimal boxLength;
        private BigDecimal boxWidth;
        private BigDecimal boxHeight;
        private Integer boxQty;
        private Integer boxNo;
        private String skuId;
        private String skuNo;
        private Integer packQty;
        private BigDecimal grossWeight;
        private String packingUserId;
        private String packingUserName;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private String createUserId;
        private String createUserName;
        private String updateUserId;
        private String updateUserName;
    }
}