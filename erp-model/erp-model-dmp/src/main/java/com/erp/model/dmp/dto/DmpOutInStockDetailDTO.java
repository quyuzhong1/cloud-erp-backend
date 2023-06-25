package com.erp.model.dmp.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 手工出入库详情表请求响应实体
 * </p>
 *
 * @author Cloud
 * @since 2023-06-25
*/
@Data
@NoArgsConstructor
public class DmpOutInStockDetailDTO implements Serializable {




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
        * 物料编码
        */
        private String skuNo;

        /**
        * 物料名称
        */
        private String productName;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 仓位
        */
        private String warehouseLocation;

        /**
        * 单价
        */
        private BigDecimal price;

        /**
        * 主表数据id
        */
        private String mainId;

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
        * 物料编码
        */
        @NotBlank(message = "物料编码不能为空")
        @Size(max = 32,message = "物料编码最大长度不能超过32位")
        private String skuNo;

        /**
        * 物料名称
        */
        @NotBlank(message = "物料名称不能为空")
        @Size(max = 255,message = "物料名称最大长度不能超过255位")
        private String productName;

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
        * 单价
        */
        @NotNull(message = "单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal price;

        /**
        * 主表数据id
        */
        @NotBlank(message = "主表数据id不能为空")
        @Size(max = 19,message = "主表数据id最大长度不能超过19位")
        private String mainId;

        /**
        * 来源详情id
        */
        @NotBlank(message = "来源详情id不能为空")
        @Size(max = 32,message = "来源详情id最大长度不能超过32位")
        private String sourceDetailId;


    }


}