package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * sku成本关系记录请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-08-24
*/
@Data
@NoArgsConstructor
public class FirstMileSkuCostRefDTO implements Serializable {




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
        * sku成本明细id
        */
        private String skuCostDetailId;

        /**
        * 费用分摊明细id
        */
        private String firstMileSkuAllocationId;


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
        * sku成本明细id
        */
        @NotBlank(message = "sku成本明细id不能为空")
        @Size(max = 19,message = "sku成本明细id最大长度不能超过19位")
        private String skuCostDetailId;

        /**
        * 费用分摊明细id
        */
        @NotBlank(message = "费用分摊明细id不能为空")
        @Size(max = 19,message = "费用分摊明细id最大长度不能超过19位")
        private String firstMileSkuAllocationId;


    }


}