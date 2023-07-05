package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 加工单明细请求响应实体
 * </p>
 *
 * @author Cloud
 * @since 2023-06-25
*/
@Data
@NoArgsConstructor
public class DmpMachineDetailDTO implements Serializable {




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
        * sku编码
        */
        private String skuNo;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 单位
        */
        private String unit;

        /**
        * 库位
        */
        private String warehouseLocation;

        /**
        * 参照版本
        */
        private Integer referenceVersion;

        /**
        * 备注
        */
        private String remark;

        /**
        * 来源详情id
        */
        private String sourceDetailId;


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
        * sku编码
        */
        @NotBlank(message = "sku编码不能为空")
        @Size(max = 32,message = "sku编码最大长度不能超过32位")
        private String skuNo;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 单位
        */
        @NotBlank(message = "单位不能为空")
        @Size(max = 32,message = "单位最大长度不能超过32位")
        private String unit;

        /**
        * 库位
        */
        @NotBlank(message = "库位不能为空")
        @Size(max = 32,message = "库位最大长度不能超过32位")
        private String warehouseLocation;

        /**
        * 参照版本
        */
        @NotNull(message = "参照版本不能为空")
        private Integer referenceVersion;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 来源详情id
        */
        @NotBlank(message = "来源详情id不能为空")
        @Size(max = 32,message = "来源详情id最大长度不能超过32位")
        private String sourceDetailId;


    }


}