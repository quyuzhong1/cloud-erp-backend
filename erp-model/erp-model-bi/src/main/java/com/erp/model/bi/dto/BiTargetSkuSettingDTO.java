package com.erp.model.bi.dto;

import java.math.BigDecimal;

import com.common.business.validator.AddGroup;
import com.common.business.validator.UpdateGroup;
import com.erp.model.bi.enums.MetricsEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * sku 目标设置表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Data
@NoArgsConstructor
public class BiTargetSkuSettingDTO implements Serializable {


    /**
     * 分页详情
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PagingViewDTO {

        private String id;

        /**
         * 年
         */
        private String year;

        /**
         * 部门id
         */
        private String deptId;

        /**
         * 部门id
         */
        private String deptName;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 详情信息
         */
        private String skuNo;

        private String skuId;

        /**
         * 指标维度
         */

        private MetricsEnum metrics;

        private String metricsName;


        /**
         * 一月值
         */
        private BigDecimal january;

        /**
         * 二月值
         */
        private BigDecimal february;

        /**
         * 三月
         */
        private BigDecimal march;


        /**
         * 四月值
         */
        private BigDecimal april;

        /**
         * 五月值
         */
        private BigDecimal may;


        /**
         * 六月值
         */
        private BigDecimal june;


        /**
         * 七月值
         */
        private BigDecimal july;


        /**
         * 八月值
         */
        private BigDecimal august;


        /**
         * 九月值
         */
        private BigDecimal september;


        /**
         * 十月值
         */
        private BigDecimal october;

        /**
         * 十一月值
         */
        private BigDecimal november;

        /**
         * 十二月值
         */
        private BigDecimal december;


    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends BiTargetYearDTO.ViewDTO {

        List<BiTargetSkuSettingDTO.DetailDTO> detailList;


    }

    @Data
    @NoArgsConstructor
    public static class DetailDTO {

        /**
         * 考核指标
         */
        private MetricsEnum metrics;


        /**
         * 考核指标名
         */
        private String metricsName;

        /**
         * 人员设置列表
         */
        private List<CommonDTO> settingList;

    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends BiTargetYearDTO.AddDTO {



        @Size(min = 1, message = "目标设置不能为空")
        @NotNull(message = "目标设置不能为空")
        @Valid
        private List<CommonDTO> detailList;

    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends BiTargetYearDTO.UpdateDTO {

        @Size(min = 1, message = "目标设置不能为空")
        @NotNull(message = "目标设置不能为空")
        @Valid
        private List<BiTargetSkuSettingDTO.CommonDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends BiTargetYearDTO.MonthDTO {
        /**
         * 店铺id
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;

        private String skuNo;

        /**
         * 是否汇总品类  true 汇总 false 不汇总
         */
        @NotNull(message = "是否汇总品类不能为空",groups = AddGroup.class)
        private Boolean isGatherCategory;


    }


    /**
     * 导入质
     */
    @Data
    @NoArgsConstructor
    public static class ImportDTO {

        /**
         * o      * 成功返回数据
         */
        private List<DetailDTO> successList;

        /**
         * 错误的url
         */
        private String errorUrl;
    }


    @Data
    @NoArgsConstructor
    public static class ListDetailDTO {

        private String id;

        private String mainId;

        private String year;

        private String deptName;


        /**
         * skuid
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;


        /**
         * 月
         */

        private Integer month;


        private BigDecimal value;


        private MetricsEnum metrics;

    }


    @Data
    @NoArgsConstructor
    public static class RemoveDTO {

        /**
         * id
         */
        @NotBlank(message = "目标不能为空")
        private String id;

        /**
         * 指标
         */
        @NotNull(message = "指标不能为空")
        private MetricsEnum metrics;

        /**
         * sku id
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;

    }
}