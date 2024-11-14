package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 试算销量去噪信息请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
*/
@Data
@NoArgsConstructor
public class CfgRuleSalesDenoisingCalcDTO implements Serializable {




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
        * 试算配置id
        */
        private String cfgRuleCalcId;


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
        * 序号
        */
        @NotNull(message = "序号不能为空")
        private Integer index;

        /**
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 64,message = "名称最大长度不能超过64位")
        private String name;

        /**
        * 开始日期
        */
        private LocalDate startDate;

        /**
        * 结束日期
        */
        private LocalDate endDate;

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
        private Integer effectiveValue;

        /**
        * 试算配置id
        */
        @NotBlank(message = "试算配置id不能为空")
        @Size(max = 19,message = "试算配置id最大长度不能超过19位")
        private String cfgRuleCalcId;


    }


}