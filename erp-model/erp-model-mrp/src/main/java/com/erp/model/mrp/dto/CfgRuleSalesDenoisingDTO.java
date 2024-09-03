package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 销量去噪信息请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@NoArgsConstructor
public class CfgRuleSalesDenoisingDTO implements Serializable {




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
        * 序号
        */
        private Integer index;

        /**
        * 名称
        */
        private String name;

        /**
         * 时间
         */
        private List<LocalDate> dateList;

        /**
        * 去噪类型，percentage百分比去噪：fixedValue=固定值去噪，completely=完全去噪
        */
        private String denoisingType;

        /**
        * 有效值（去噪后的）
        */
        private Integer effectiveValue;

        /**
        * 销量表id（cfg_rule_sales_qty）
        */
        private String salesQtyId;

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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 10,message = "名称最大长度不能超过10位")
        private String name;

        /**
         * 时间
         */
        @NotEmpty(message = "时间区间不能为空")
        private List<LocalDate> dateList;

        /**
        * 去噪类型，percentage百分比去噪：fixedValue=固定值去噪，completely=完全去噪
        */
        @NotBlank(message = "去噪类型，percentage百分比去噪：fixedValue=固定值去噪，completely=完全去噪不能为空")
        @Size(max = 32,message = "去噪类型，percentage百分比去噪：fixedValue=固定值去噪，completely=完全去噪最大长度不能超过32位")
        private String denoisingType;

        /**
        * 有效值（去噪后的）
        */
        @NotNull(message = "有效值（去噪后的）不能为空")
        @Min(value = 0,message = "有效值（去噪后的）最小值为0")
        @Max(value = 999999999,message = "有效值（去噪后的）最大值为999999999")
        private Integer effectiveValue;

    }


}