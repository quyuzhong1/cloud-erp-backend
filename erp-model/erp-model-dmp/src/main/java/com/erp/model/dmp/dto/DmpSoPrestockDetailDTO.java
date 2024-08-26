package com.erp.model.dmp.dto;

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
 * 销售预入库明细表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-08-09
*/
@Data
@NoArgsConstructor
public class DmpSoPrestockDetailDTO implements Serializable {




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
        * 来源详情id
        */
        private String thirdDetailId;

        /**
        * skuId
        */
        private String skuId;

        /**
        * sku名称
        */
        private String skuNo;

        /**
        * sku名称
        */
        private String skuName;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 仓位
        */
        private String warehouseLocation;

        /**
        * 备注
        */
        private String remark;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 转换id
        */
        private String convertId;

        /**
        * 下一层级id
        */
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


    }
    
    /**
     * 详情
     */
     @Data
     @NoArgsConstructor
     public static class PrestockDetailDTO {

         /**
         * 来源详情id
         */
         private String thirdDetailId;

         /**
         * skuId
         */
         private String skuId;

         /**
         * sku名称
         */
         private String skuNo;

         /**
         * sku名称
         */
         private String skuName;

         /**
         * 数量
         */
         private Integer qty;

         /**
         * 仓位
         */
         private String warehouseLocation;

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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 来源详情id
        */
        @NotBlank(message = "来源详情id不能为空")
        @Size(max = 64,message = "来源详情id最大长度不能超过64位")
        private String thirdDetailId;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * sku名称
        */
        @NotBlank(message = "sku名称不能为空")
        @Size(max = 64,message = "sku名称最大长度不能超过64位")
        private String skuName;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 仓位
        */
        @NotBlank(message = "仓位不能为空")
        @Size(max = 32,message = "仓位最大长度不能超过32位")
        private String warehouseLocation;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        private String remark;

        /**
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 转换id
        */
        @NotBlank(message = "转换id不能为空")
        @Size(max = 19,message = "转换id最大长度不能超过19位")
        private String convertId;

        /**
        * 下一层级id
        */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 19,message = "下一层级id最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


    }


}