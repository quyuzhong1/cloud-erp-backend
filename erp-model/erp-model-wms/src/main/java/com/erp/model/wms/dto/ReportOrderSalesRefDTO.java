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
 * 订单销量关联表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-09-23
*/
@Data
@NoArgsConstructor
public class ReportOrderSalesRefDTO implements Serializable {




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
        * 仓库id 
        */
        private String warehouseId;

        /**
        * 虚拟仓库id
        */
        private String virtualWarehouseId;

        /**
        * skuId
        */
        private String skuId;

        /**
        * 业务id
        */
        private String businessId;

        /**
        * 业务编码
        */
        private String businessCode;

        /**
        * 业务类型(取sourceTypeEnum枚举)
        */
        private String businessType;


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
        * 仓库id 
        */
        @NotBlank(message = "仓库id 不能为空")
        @Size(max = 19,message = "仓库id 最大长度不能超过19位")
        private String warehouseId;

        /**
        * 虚拟仓库id
        */
        @NotBlank(message = "虚拟仓库id不能为空")
        @Size(max = 19,message = "虚拟仓库id最大长度不能超过19位")
        private String virtualWarehouseId;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 业务id
        */
        @NotBlank(message = "业务id不能为空")
        @Size(max = 19,message = "业务id最大长度不能超过19位")
        private String businessId;

        /**
        * 业务编码
        */
        @NotBlank(message = "业务编码不能为空")
        @Size(max = 32,message = "业务编码最大长度不能超过32位")
        private String businessCode;

        /**
        * 业务类型(取sourceTypeEnum枚举)
        */
        @NotBlank(message = "业务类型(取sourceTypeEnum枚举)不能为空")
        @Size(max = 32,message = "业务类型(取sourceTypeEnum枚举)最大长度不能超过32位")
        private String businessType;


    }


}