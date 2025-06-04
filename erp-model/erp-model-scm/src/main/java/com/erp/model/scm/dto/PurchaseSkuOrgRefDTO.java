package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * SKU与采购组织关系请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-05-28
*/
@Data
@NoArgsConstructor
public class PurchaseSkuOrgRefDTO implements Serializable {




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
        * 采购组织
        */
        private String purchaseOrgId;

        /**
        * 采购组织名
        */
        private String purchaseOrgName;

        /**
        * sku 表id
        */
        private String skuId;

        /**
        * sku no
        */
        private String skuNo;

        /**
        * 备注
        */
        private String remark;

        /**
        * 是否禁用
        */
        private Boolean disabled;


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
        * 采购组织
        */
        @NotBlank(message = "采购组织不能为空")
        @Size(max = 19,message = "采购组织最大长度不能超过19位")
        private String purchaseOrgId;

        /**
        * 采购组织名
        */
        @NotBlank(message = "采购组织名不能为空")
        @Size(max = 200,message = "采购组织名最大长度不能超过200位")
        private String purchaseOrgName;

        /**
        * sku 表id
        */
        @NotBlank(message = "sku 表id不能为空")
        @Size(max = 19,message = "sku 表id最大长度不能超过19位")
        private String skuId;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;


    }


    @Data
    @NoArgsConstructor
    public static class QuerySkuDTO {
        @NotEmpty(message = "sku集合不能为空")
        private List<String> skuIdList;
    }
}