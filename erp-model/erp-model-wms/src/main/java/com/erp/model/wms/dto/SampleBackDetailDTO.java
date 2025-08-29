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
 * 样品退回详情请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
*/
@Data
@NoArgsConstructor
public class SampleBackDetailDTO implements Serializable {




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
        * 主表ID（关联样品退回单）
        */
        private String mainId;

        /**
        * SKU编码
        */
        private String skuNo;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 使用方ID
        */
        private String useUserId;

        /**
        * 可退回数量 wms/sampleLedger/listSku  参数type=back
        */
        private Integer availableQty;

        /**
        * 退回数量
        */
        private Integer qty;

        /**
        * 备注
        */
        private String remark;


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
        * 主表ID（关联样品退回单）
        */
        @NotBlank(message = "主表ID（关联样品退回单）不能为空")
        @Size(max = 19,message = "主表ID（关联样品退回单）最大长度不能超过19位")
        private String mainId;

        /**
        * 来源明细ID
        */
        @Size(max = 19,message = "来源明细ID最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * SKU编码
        */
        @NotBlank(message = "SKU编码不能为空")
        @Size(max = 100,message = "SKU编码最大长度不能超过100位")
        private String skuNo;

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU ID不能为空")
        @Size(max = 19,message = "SKU ID最大长度不能超过19位")
        private String skuId;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 200,message = "产品名称最大长度不能超过200位")
        private String productName;

        /**
        * 使用方ID
        */
        @NotBlank(message = "使用方ID不能为空")
        @Size(max = 19,message = "使用方ID最大长度不能超过19位")
        private String useUserId;

        /**
        * 退回数量
        */
        @NotNull(message = "退回数量不能为空")
        private Integer qty;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;


    }


}