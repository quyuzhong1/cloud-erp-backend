package com.erp.model.dmp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * sku bom关系表请求响应实体
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
*/
@Data
@NoArgsConstructor
public class DmpSkuCostDTO implements Serializable {




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
        * sku编号
        */
        private String skuNo;
        /**
        * 成本日期
        */
        private LocalDate costDate;
        /**
        * 备注
        */
        private String remark;
        /**
        * 有效状态
        */
        private Boolean status;
        /**
        * 成本价格
        */
        private BigDecimal costPrice;

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
        * sku编号
        */
        @NotBlank(message = "sku编号不能为空")
        @Size(max = 64,message = "sku编号最大长度不能超过64位")
        private String skuNo;
        /**
        * 成本日期
        */
        private LocalDate costDate;
        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;
        /**
        * 有效状态
        */
        @NotNull(message = "有效状态不能为空")
        private Boolean status;
        /**
        * 成本价格
        */
        @NotNull(message = "成本价格不能为空")
        @Digits(integer = 12, fraction = 4, message = "成本价格整数位不能超过12位，小数位不能超过4位")
        private BigDecimal costPrice;

    }


}