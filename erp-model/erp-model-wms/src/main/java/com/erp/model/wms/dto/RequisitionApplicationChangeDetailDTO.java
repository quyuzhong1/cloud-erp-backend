package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 要货申请变更明细请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-11-18
*/
@Data
@NoArgsConstructor
public class RequisitionApplicationChangeDetailDTO implements Serializable {




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
        * 产品id
        */
        private String skuId;

        /**
        * 产品编号
        */
        private String skuNo;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * 变更类型
        */
        private String changeType;

        /**
        * 原发货通知数量
        */
        private Integer originQty;

        /**
        * 新发货通知数量
        */
        private Integer newQty;

        /**
        * 平台sku,msku
        */
        private String platformSkuNo;

        /**
        * bom版本
        */
        private String bomVersion;

        /**
        * fn_sku
        */
        private String fnSku;

        /**
        * 备注
        */
        private String remark;

        /**
        * 平台Sku名称
        */
        private String platformSkuName;


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
        * 产品id
        */
        @NotBlank(message = "产品id不能为空")
        @Size(max = 19,message = "产品id最大长度不能超过19位")
        private String skuId;

        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 30,message = "来源明细id最大长度不能超过30位")
        private String sourceDetailId;

        /**
        * 变更类型
        */
        @NotBlank(message = "变更类型不能为空")
        @Size(max = 30,message = "变更类型最大长度不能超过30位")
        private String changeType;

        /**
        * 原发货通知数量
        */
        @NotNull(message = "原发货通知数量不能为空")
        private Integer originQty;

        /**
        * 新发货通知数量
        */
        @NotNull(message = "新发货通知数量不能为空")
        private Integer newQty;

        /**
        * 平台sku,msku
        */
        @NotBlank(message = "平台sku,msku不能为空")
        @Size(max = 100,message = "平台sku,msku最大长度不能超过100位")
        private String platformSkuNo;

        /**
        * bom版本
        */
        @NotBlank(message = "bom版本不能为空")
        @Size(max = 255,message = "bom版本最大长度不能超过255位")
        private String bomVersion;

        /**
        * fn_sku
        */
        @NotBlank(message = "fn_sku不能为空")
        @Size(max = 255,message = "fn_sku最大长度不能超过255位")
        private String fnSku;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 平台Sku名称
        */
        @NotBlank(message = "平台Sku名称不能为空")
        @Size(max = 500,message = "平台Sku名称最大长度不能超过500位")
        private String platformSkuName;


    }


}