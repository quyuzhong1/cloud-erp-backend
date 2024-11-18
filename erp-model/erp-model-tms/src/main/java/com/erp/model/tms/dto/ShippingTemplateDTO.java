package com.erp.model.tms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 运费模板请求响应实体
 * </p>
 *
 * @author Will
 * @since 2023-11-03
*/
@Data
@NoArgsConstructor
public class ShippingTemplateDTO implements Serializable {

    /**
     * tab列表
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {

        /**
         * 类型（启用true,停用false）
         */
        private Boolean tabFlag;

        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 模板名称
         */
        private String name;

        /**
         * 是否禁用（启用停用，true停用，false启用）
         */
        private Boolean disabled;

        /**
         * 生效日期
         */
        private List<LocalDate> effectiveDateList;

        /**
         * 失效日期
         */
        private List<LocalDate> expireDateList;

        /**
         * 创建人id集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;
    }

    /**
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class ExportExcelParamDTO extends PagingParamDTO{

        /**
         * 主键ids
         */
        private List<String> ids;

    }


    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 模板名称
         */
        private String  name;
        /**
         * 模板类型 （字典shippingTemplateType，country按国家/地区,region按国家/地区+分区,warehouse按仓库"）
         */
        private String type;
        /**
         * 模板类型名称
         */
        private String typeName;
        /**
         * 生效日期
         */
        private LocalDate effectiveDate;
        /**
         * 失效日期
         */
        private LocalDate expireDate;

        /**
         * 物流商id集合
         */
        private List<String> logisticsSupplierIdList;

        /**
         * 渠道id集合
         */
        private List<String> channelIdList;

        /**
         * 应用渠道
         */
        private List<String> channelNameList;

        /**
         * 应用渠道字符串
         */
        private String channelNames;

        /**
         * 是否禁用
         */
        private Boolean disabled;
        /**
         * 是否禁用名称
         */
        private String disabledName;
        /**
         * 创建人
         */
        private String createUserName;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 重量单位
         */
        private String weightUnit;

        /**
         * 币种
         */
        private String currency;
        /**
         * 币种符号
         */
        private String currencySymbol;
    }


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
        * 模板名称
        */
        private String name;

        /**
        * 计费方式 （字典 shippingBillingMethod）
        */
        private String billingMethod;

        /**
         * 计费方式名称
         */
        private String billingMethodName;

        /**
        * 币别
        */
        private String currency;

        /**
        * 重量单位
        */
        private String weightUnit;

        /**
        * 价格进制（字典 priceBinary）
        */
        private String priceBinary;

        /**
         * 价格进制名称
         */
        private String priceBinaryName;

        /**
        * 材积设置
        */
        private Integer volumeSetting;

        /**
        * 生效日期
        */
        private LocalDate effectiveDate;

        /**
        * 失效日期
        */
        private LocalDate expireDate;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 模板类型
        */
        private String type;

        /**
         * 模板规则明细
         */
        private List<ShippingTemplateRuleDTO.ViewDTO> detailList;

        /**
         * 其他费用
         */
        private List<ShippingTemplateOtherCostDTO.ViewDTO> otherCostList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 模板规则明细
         */
        @NotEmpty(message = "模板规则不能为空")
        private List<ShippingTemplateRuleDTO.AddDTO> detailList;

        /**
         * 其他费用
         */
        private List<ShippingTemplateOtherCostDTO.AddDTO> otherCostList;
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

        /**
         * 模板规则明细
         */
        @NotEmpty(message = "模板规则不能为空")
        private List<ShippingTemplateRuleDTO.UpdateDTO> detailList;

        /**
         * 其他费用
         */
        private List<ShippingTemplateOtherCostDTO.UpdateDTO> otherCostList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 模板名称
        */
        @NotBlank(message = "模板名称不能为空")
        @Size(max = 64,message = "模板名称最大长度不能超过64位")
        private String name;

        /**
        * 计费方式
        */
        @NotBlank(message = "计费方式不能为空")
        @Size(max = 32,message = "计费方式最大长度不能超过32位")
        private String billingMethod;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 16,message = "币别最大长度不能超过16位")
        private String currency;

        /**
        * 重量单位
        */
        @NotBlank(message = "重量单位不能为空")
        @Size(max = 16,message = "重量单位最大长度不能超过16位")
        private String weightUnit;

        /**
        * 价格进制
        */
        @NotBlank(message = "价格进制不能为空")
        @Size(max = 32,message = "价格进制最大长度不能超过32位")
        private String priceBinary;

        /**
        * 材积设置
        */
        @NotNull(message = "材积设置不能为空")
        @DecimalMax(value = "999999999", message = "材积设置最大值")
        @DecimalMin(value = "1", message = "材积设置最小值不能为0")
        private Integer volumeSetting;

        /**
        * 生效日期
        */
        @NotNull(message = "生效日期不能为空")
        private LocalDate effectiveDate;

        /**
        * 失效日期
        */
        private LocalDate expireDate;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 模板类型
        */
        @NotBlank(message = "模板类型不能为空")
        @Size(max = 32,message = "模板类型最大长度不能超过32位")
        private String type;


    }

    /**
     * 试算查询入参
     */
    @Data
    @NoArgsConstructor
    public static class TrialCalculationParamDTO  {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 起始地
         */
        @NotBlank(message = "起始地不能为空")
        private String fromCountry;

        /**
         * 目的地
         */
        private String toCountry;

        /**
         * 重量
         */
        @NotNull(message = "总量不能为空")
        private BigDecimal weight;

        /**
         * 目的区域
         */
        private String region;

        /**
         * 目的仓库
         */
        private String toWarehouseName;

    }

    /**
     * 应用渠道
     */
    @Data
    @NoArgsConstructor
    public static class ChannelParamDTO  {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 渠道id集合
         */
        @NotEmpty(message = "应用渠道不能为空")
        private List<String> logisticsChannelIdList;

    }

    /**
     * 启用/禁用
     */
    @Data
    @NoArgsConstructor
    public static class DisabledParamDTO  {

        /**
         * 主键id
         */
        @NotEmpty(message = "主键ids不能为空")
        private List<String> ids;

        /**
         * 是否禁用
         */
        @NotNull(message = "启用停用状态不能为空")
        private Boolean disabled;
    }

    @Data
    @NoArgsConstructor
    public static class SelectDTO  {

        /**
         * 主键id
         */
        private String id;

        /**
         * 名称
         */
        private String name;

        /**
         * 是否禁用
         */
        private Boolean disabled;

    }

}