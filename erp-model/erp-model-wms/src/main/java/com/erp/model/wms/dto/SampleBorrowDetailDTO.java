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
 * 借用变更单明细表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-08-20
*/
@Data
@NoArgsConstructor
public class SampleBorrowDetailDTO implements Serializable {




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
        * 借用数量
        */
        private Integer borrowQty;

        /**
        * 待归还数量
        */
        private Integer waitReturnQty;

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
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 500,message = "产品名称最大长度不能超过500位")
        private String productName;

        /**
        * 借用数量
        */
        @NotNull(message = "借用数量不能为空")
        private Integer borrowQty;

        /**
        * 待归还数量
        */
        @NotNull(message = "待归还数量不能为空")
        private Integer waitReturnQty;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;


    }


}