package com.erp.model.oms.dto;

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
 * B2C寄样申请单拆分单明细请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-12-04
*/
@Data
@NoArgsConstructor
public class KolSubB2cApplicationDetailDTO implements Serializable {




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
        * 主表ID
        */
        private String mainId;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * 平台明细id
        */
        private String platformDetailId;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * SKU编码
        */
        private String skuNo;

        /**
        * 申请数量
        */
        private Integer applyQty;

        /**
        * 备注
        */
        private String remark;

        /**
        * 项目名称
        */
        private String projectTag;


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
        * 主表ID
        */
        @NotBlank(message = "主表ID不能为空")
        @Size(max = 19,message = "主表ID最大长度不能超过19位")
        private String mainId;

        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 平台明细id
        */
        @NotBlank(message = "平台明细id不能为空")
        @Size(max = 19,message = "平台明细id最大长度不能超过19位")
        private String platformDetailId;

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU ID不能为空")
        @Size(max = 19,message = "SKU ID最大长度不能超过19位")
        private String skuId;

        /**
        * 申请数量
        */
        @NotNull(message = "申请数量不能为空")
        private Integer applyQty;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 项目名称
        */
        @NotBlank(message = "项目名称不能为空")
        @Size(max = 200,message = "项目名称最大长度不能超过200位")
        private String projectTag;


    }


}