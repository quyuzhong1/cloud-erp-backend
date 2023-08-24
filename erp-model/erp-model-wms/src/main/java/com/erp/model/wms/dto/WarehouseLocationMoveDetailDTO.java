package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 仓位移动明细表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-24
*/
@Data
@NoArgsConstructor
public class WarehouseLocationMoveDetailDTO implements Serializable {




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
        * sku表id
        */
        private String skuId;

        /**
        * sku编号
        */
        private String skuNo;

        /**
        * 取货仓位
        */
        private String outWarehouseLocation;

        /**
        * 上架仓位
        */
        private String inWarehouseLocation;

        /**
        * 移动数量
        */
        private Integer qty;


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
        * sku表id
        */
        @NotBlank(message = "sku表id不能为空")
        @Size(max = 19,message = "sku表id最大长度不能超过19位")
        private String skuId;

        /**
        * sku编号
        */
        @NotBlank(message = "sku编号不能为空")
        @Size(max = 255,message = "sku编号最大长度不能超过255位")
        private String skuNo;

        /**
        * 取货仓位
        */
        @NotBlank(message = "取货仓位不能为空")
        @Size(max = 50,message = "取货仓位最大长度不能超过50位")
        private String outWarehouseLocation;

        /**
        * 上架仓位
        */
        @NotBlank(message = "上架仓位不能为空")
        @Size(max = 50,message = "上架仓位最大长度不能超过50位")
        private String inWarehouseLocation;

        /**
        * 移动数量
        */
        @NotNull(message = "移动数量不能为空")
        private Integer qty;


    }


}