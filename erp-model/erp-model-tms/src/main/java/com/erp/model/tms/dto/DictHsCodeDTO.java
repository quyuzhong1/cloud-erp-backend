package com.erp.model.tms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.plm.dto.ProductCustomsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 出口申报要素表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-07-11
*/
@Data
@NoArgsConstructor
public class DictHsCodeDTO implements Serializable {




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
        * 中国海关编码
        */
        private String hsCode;

        /**
        * 报关中文名
        */
        private String declareNameCn;

        /**
        * 第一法定单位
        */
        private String firstDeclareUnit;


        /**
        * 申报要素
        */
        private String declareElement;

        /**
        * 出口退税率 (%)
        */
        private BigDecimal exportRebateRate;

        /**
        * 生效日期
        */
        private LocalDateTime effectiveDate;

        /**
        * 失效日期
        */
        private LocalDateTime expireDate;

        /**
        * 是否启用
        */
        private Boolean disabled;


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
        * 中国海关编码
        */
        @NotBlank(message = "中国海关编码不能为空")
        @Size(max = 50,message = "中国海关编码最大长度不能超过50位")
        private String hsCode;

        /**
        * 报关中文名
        */
        @NotBlank(message = "报关中文名不能为空")
        @Size(max = 200,message = "报关中文名最大长度不能超过200位")
        private String declareNameCn;

        /**
        * 报关英文名
        */
//        @NotBlank(message = "报关英文名不能为空")
//        @Size(max = 200,message = "报关英文名最大长度不能超过200位")
        private String declareNameEn;

        /**
        * 第一法定单位
        */
        @NotBlank(message = "报关单位不能为空")
        private String firstDeclareUnit;

        /**
        * 第二法定单位
        */
//        @NotBlank(message = "第二法定单位不能为空")
//        @Size(max = 50,message = "第二法定单位最大长度不能超过50位")
        private String secondDeclareUnit;

        /**
        * 申报要素
        */
        @NotBlank(message = "申报要素不能为空")
        private String declareElement;

        /**
        * 监管条件
        */
//        @NotBlank(message = "监管条件不能为空")
//        @Size(max = 200,message = "监管条件最大长度不能超过200位")
        private String supervisionConditions;

        /**
        * 最惠国税率 (%)
        */
//        @NotNull(message = "最惠国税率 (%)不能为空")
//        @Digits(integer = 12, fraction = 4, message = "最惠国税率 (%)整数位不能超过12位，小数位不能超过4位")
        private BigDecimal mfnRate;

        /**
        * 普通税率 (%)
        */
//        @NotNull(message = "普通税率 (%)不能为空")
//        @Digits(integer = 12, fraction = 4, message = "普通税率 (%)整数位不能超过12位，小数位不能超过4位")
        private BigDecimal generalRate;

        /**
        * 增值税率 (%)
        */
//        @NotNull(message = "增值税率 (%)不能为空")
//        @Digits(integer = 12, fraction = 4, message = "增值税率 (%)整数位不能超过12位，小数位不能超过4位")
        private BigDecimal vatRate;

        /**
        * 消费税率 (%)
        */
//        @NotNull(message = "消费税率 (%)不能为空")
//        @Digits(integer = 12, fraction = 4, message = "消费税率 (%)整数位不能超过12位，小数位不能超过4位")
        private BigDecimal consumptionTaxRate;

        /**
        * 出口退税率 (%)
        */
        @NotNull(message = "出口退税率 (%)不能为空")
        @Digits(integer = 12, fraction = 2, message = "出口退税率 (%)整数位不能超过12位，小数位不能超过4位")
        private BigDecimal exportRebateRate;

        /**
        * 生效日期
        */
        private LocalDateTime effectiveDate;

        /**
        * 失效日期
        */
        private LocalDateTime expireDate;

        /**
        * 是否启用
        */
//        @NotNull(message = "是否启用不能为空")
        private Boolean disabled;


    }


    @Data
    @NoArgsConstructor
    public static class BaseDTO {
        /**
         * id
         */
        private String id;

        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }


    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO extends BaseDTO {
        /**
         * 主键id
         */
        private String  id;

        /**
         * 中国海关编码
         */
        private String hsCode;

        /**
         * 报关中文名
         */
        private String declareNameCn;

        /**
         * 第一法定单位
         */
        private String firstDeclareUnit;


        /**
         * 申报要素
         */
        private String declareElement;

        /**
         * 出口退税率 (%)
         */
        private BigDecimal exportRebateRate;

        /**
         * 生效日期
         */
        private LocalDateTime effectiveDate;

        /**
         * 失效日期
         */
        private LocalDateTime expireDate;

        /**
         * 是否启用
         */
        private Boolean disabled;
    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 主键id
         */
        private List<String> ids;

        private String id;

    }


}