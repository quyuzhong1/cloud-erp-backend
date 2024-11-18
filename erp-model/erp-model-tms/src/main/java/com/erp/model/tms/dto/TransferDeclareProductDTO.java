package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 中转报关产品请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-01-27
*/
@Data
@NoArgsConstructor
public class TransferDeclareProductDTO implements Serializable {




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
        * 销售订单明细id
        */
        private String soDetailId;

        /**
        * 来源id[中转报关单据]
        */
        private String declareId;

        /**
        * 来源明细id[中转报关单据]
        */
        private String declareDetailId;

        /**
        * 产品sku编号
        */
        private String skuNo;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 中文报关名称
        */
        private String declareChineseName;

        /**
        * 英文报关名称
        */
        private String declareEnglishName;

        /**
        * 申报价
        */
        private BigDecimal declarePrice;

        /**
        * 申报币种
        */
        private String currency;


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
        * 销售订单明细id
        */
        @NotBlank(message = "销售订单明细id不能为空")
        @Size(max = 19,message = "销售订单明细id最大长度不能超过19位")
        private String soDetailId;

        /**
        * 来源id[中转报关单据]
        */
        @NotBlank(message = "来源id[中转报关单据]不能为空")
        @Size(max = 19,message = "来源id[中转报关单据]最大长度不能超过19位")
        private String declareId;

        /**
        * 来源明细id[中转报关单据]
        */
        @NotBlank(message = "来源明细id[中转报关单据]不能为空")
        @Size(max = 19,message = "来源明细id[中转报关单据]最大长度不能超过19位")
        private String declareDetailId;

        @NotBlank(message = "产品sku编号不能为空")
        private String skuNo;
        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 中文报关名称
        */
        @NotBlank(message = "中文报关名称不能为空")
        @Size(max = 255,message = "中文报关名称最大长度不能超过255位")
        private String declareChineseName;

        /**
        * 英文报关名称
        */
        @NotBlank(message = "英文报关名称不能为空")
        @Size(max = 255,message = "英文报关名称最大长度不能超过255位")
        private String declareEnglishName;

        /**
        * 申报价
        */
        @NotNull(message = "申报价不能为空")
        private BigDecimal declarePrice;

        /**
        * 申报币种
        */
        @NotBlank(message = "申报币种不能为空")
        @Size(max = 32,message = "申报币种最大长度不能超过32位")
        private String currency;


    }


}