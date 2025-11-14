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
 * 样品调整单明细表请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-11-14
*/
@Data
@NoArgsConstructor
public class SampleAdjustmentDetailDTO implements Serializable {




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
        * 关联主表ID
        */
        private String mainId;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * sku编号
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 台账数量
        */
        private Integer ledgerQty;

        /**
        * 实际数量
        */
        private Integer actualQty;

        /**
        * 差异数量
        */
        private Integer differenceQty;

        /**
        * 备注
        */
        private String remark;

        /**
        * 样品台账id
        */
        private String sampleLedgerId;


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
        * 关联主表ID
        */
        @NotBlank(message = "关联主表ID不能为空")
        @Size(max = 19,message = "关联主表ID最大长度不能超过19位")
        private String mainId;

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU ID不能为空")
        @Size(max = 19,message = "SKU ID最大长度不能超过19位")
        private String skuId;

        /**
        * sku编号
        */
        private String skuNo;

        /**
        * 产品名称
        */
        @Size(max = 500,message = "产品名称最大长度不能超过500位")
        private String productName;

        /**
        * 使用方
        */
        private String userSide;

        /**
        * 台账数量
        */
        @NotNull(message = "台账数量不能为空")
        private Integer ledgerQty;

        /**
        * 实际数量
        */
        @NotNull(message = "实际数量不能为空")
        private Integer actualQty;

        /**
        * 差异数量
        */
        private Integer differenceQty;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 样品台账id
        */
        @NotBlank(message = "样品台账id不能为空")
        @Size(max = 19,message = "样品台账id最大长度不能超过19位")
        private String sampleLedgerId;


    }


}