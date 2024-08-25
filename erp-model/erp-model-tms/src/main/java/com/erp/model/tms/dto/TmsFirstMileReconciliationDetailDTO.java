package com.erp.model.tms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.tms.enums.TmsB2cDeclareReconciliationImportEnum;
import com.sun.corba.se.spi.orb.StringPair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 头程对账单明细请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Data
@NoArgsConstructor
public class TmsFirstMileReconciliationDetailDTO implements Serializable {

    /**
     * 分页列表查询参数
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

    }

    /**
     * 列表
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class ExportDetailDTO extends ListDTO {

        /**
         * 主表id
         */
        private String id;

        /**
         * 对账单号【可排序】
         */
        private String code;

        /**
         * 审核状态【可排序】
         */
        private String approveStatus;

        /**
         * 审核名称
         */
        private String approveStatusName;

        /**
         * 审核人id【可排序】
         */
        private String approveUserId;

        /**
         * 审核人名称【可排序】
         */
        private String approveUserName;

        /**
         * 生成对账日期【可排序】
         */
        private LocalDate reconciliationDate;

        /**
         * 提交日期【可排序】
         */
        private LocalDate submitDate;

        /**
         * 审核日期【可排序】
         */
        private LocalDate approveDate;

        /**
         * 对账开始日期【可排序】
         */
        private LocalDate startDate;

        /**
         * 对账结束日期【可排序】
         */
        private LocalDate endDate;

        /**
         * 对账周期
         */
        private String cycle;

        /**
         * 物流商Id【可排序】
         */
        private String logisticsSupplierId;

        /**
         * 物流商名称【可排序】
         */
        private String logisticsSupplierName;

        /**
         * 币别【可排序】
         */
        private String currency;

        /**
         * 币别名称
         */
        private String currencyName;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 汇率
         */
        private BigDecimal exchangeRate;

        /**
         * 实际重量【箱包装重量】带单位
         */
        private String actualWeightWithUnit;

        /**
         * 体积重带单位
         */
        private String volumeWeightWithUnit;

        /**
         * 计费重带单位
         */
        private BigDecimal billingWeightWithUnit;

        /**
         * 对账次数 默认1
         */
        private Integer reconciliationCount;
        /**
         *显示为首次对账，N次对账
         */
        private String reconciliationCountName;

        /**
         * 对账月份（取值为对账周期末值所在月份）
         */
        private LocalDate reconciliationMonth;

        public String getActualWeightWithUnit() {
            BigDecimal actualWeight = this.getActualWeight();
            if (null == actualWeight){
                return "0".concat(this.getActualWeightUnit());
            }
            return actualWeight.setScale(2, RoundingMode.DOWN)
                    .stripTrailingZeros().toString()
                    .concat(this.getActualWeightUnit());
        }

        public String getBillingWeightWithUnit() {
            BigDecimal billingWeight = this.getBillingWeight();
            if (null == billingWeight){
                return "0".concat(this.getBillingWeightUnit());
            }
            return billingWeight.setScale(2, RoundingMode.DOWN)
                    .stripTrailingZeros().toString()
                    .concat(this.getBillingWeightUnit());
        }

        public String getVolumeWeightWithUnit() {
            BigDecimal volumeWeight = this.getVolumeWeight();
            if (null == volumeWeight){
                return "0".concat(this.getVolumeWeightUnit());
            }
            return volumeWeight.setScale(2, RoundingMode.DOWN)
                    .stripTrailingZeros().toString()
                    .concat(this.getVolumeWeightUnit());
        }
    }

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO implements Serializable{

        /**
         * 主键id
         */
        private String id;

        /**
         * 主表id
         */
        private String mainId;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源编码(物流单号对应sourceCode)
         */
        private String sourceCode;

        /**
         * 关联单号(物流运输号关联的货件单号)
         */
        private String relationCode;

        /**
         * 运输单号
         */
        private String transportNo;

        /**
         * 货件/计划单号【业务单号】
         */
        private String businessCode;

        /**
         * 店铺ID
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 发货国家代号
         */
        private String fromCountry;

        /**
         * 发货国家
         */
        private String fromCountryName;

        /**
         * 到货国家代号
         */
        private String toCountry;

        /**
         * 到货国家
         */
        private String toCountryName;

        /**
         * 签收日期
         */
        private LocalDate receiveDate;

        /**
         * 运输状态
         */
        private String transportStatus;

        /**
         * 运输状态名称
         */
        private String transportStatusName;

        /**
         * 计费规则
         */
        private String feeRule;

        /**
         * 计费规则
         */
        private String feeRuleName;

        /**
         * 渠道商ID
         */
        private String logisticsChannelId;

        /**
         * 类型(对账类型)
         */
        private String type;

        /**
         * 类型名称(对账类型)
         */
        private String typeName;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币别名称
         */
        private String currencyName;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 总物流费用
         */
        private BigDecimal totalLogisticsCost;

        /**
         * 实际重量【箱包装重量】
         */
        private BigDecimal actualWeight;

        /**
         * 实际重量单位【箱包装重量单位】
         */
        private String actualWeightUnit;

        /**
         * 体积重
         */
        private BigDecimal volumeWeight;

        /**
         * 体积重单位
         */
        private String volumeWeightUnit;

        /**
         * 计费重
         */
        private BigDecimal billingWeight;

        /**
         * 计费重单位
         */
        private String billingWeightUnit;

        /**
         * 物流运费用【预计物流费用】
         */
        private BigDecimal shippingCost;

        /**
         * 报关费用【预计报关费用】
         */
        private BigDecimal declareCost;

        /**
         * 其他费用【预计其他费用】
         */
        private BigDecimal otherCost;
        /**
         * 其他税费【预计其他税费】
         */
        private BigDecimal otherTaxCost;

        /**
         * 备注
         */
        private String remark;

        /**
         * 对账状态
         */
        private String status;

        /**
         * 对账状态名称
         */
        private String statusName;

        /**
         * 确认时间
         */
        private LocalDate confirmDate;

        /**
         * 确认人id
         */
        private String confirmUserId;

        /**
         * 确认人名称
         */
        private String confirmUserName;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建人ID
         */
        private String createUserId;

        /**
         * 物流单对账状态(后台用)
         */
        private String reconciliationStatus;

        /**
         * 物流商ID(后台用)
         */
        private String logisticsSupplierId;

        /**
         * 发货单ID(后台用)
         */
        private String deliveryId;

        /**
         * 物流单记录的实际体积重(后台用)
         */
        private BigDecimal volumeWeightLogistics;

        /**
         * 物流单记录的实际重(后台用)
         */
        private BigDecimal weightLogistics;
        /**
         * 对账次数 默认1
         */
        private Integer reconciliationCount;
        /**
         * 显示为首次对账，N次对账
         */
        private String reconciliationCountName;

        /**
         * 对账月份（取值为对账周期末值所在月份）
         */
        private LocalDate reconciliationMonth;

        /**
         * 账单类型： actual=实际， initPeriod=期初
         */
        private String reconciliationType;

        /**
         * 费用明细详情(导入时传递)
         */
        private List<TmsCostDetailDTO.UpdateDTO> updateList;

        public StringPair getAutoStringPair() {
            return new StringPair(this.logisticsSupplierId, this.currency);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostInfoDTO {

        /**
         * 费用配置id
         */
        private String id;

        /**
         * 费用名称
         */
        private String costName;

        /**
         * 费用值
         */
        private BigDecimal costValue;
    }

    /**
     * 新增
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class AddDTO extends TmsFirstMileReconciliationDetailDTO.CommonDTO {


    }

    /**
     * 修改
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;


        /**
         * 备注
         */
        @Size(max = 255, message = "备注最大长度不能超过255位")
        private String remark;

        /**
         * 费用编辑（导入数据返回）
         */
        private List<TmsCostDetailDTO.UpdateDTO> updateList;


    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO{

        /**
         * 对账单ids
         */
        private List<String> mainIdList;
    }




    @Data
    @NoArgsConstructor
    public static class UpdateStatusDTO {

        /**
         * 对账明细id
         */
        @NotBlank(message = "对账明细id不能为空")
        private List<String> ids;

        /**
         * 对账状态
         */
        @NotBlank(message = "对账状态不能为空")
        private String status;
    }

    @Data
    @NoArgsConstructor
    public static class ExcelImportDTO {

        /**
         * 导入类型，standard标准，config配置
         */
        @NotNull(message = "导入类型不能为空")
        private TmsB2cDeclareReconciliationImportEnum typeEnum;


        /**
         * 导入文件
         */
        @NotNull(message = "导入文件不能为空")
        private MultipartFile excelFile;

        /**
         * 详情主ID
         */
        @NotBlank(message = "详情主ID")
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class ExcelDownloadTemplateDTO {

        /**
         * 导入类型，standard标准，config配置
         */
        @NotNull(message = "导入类型不能为空")
        private TmsB2cDeclareReconciliationImportEnum typeEnum;

        /**
         * 详情主ID
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class ImportDTO {

        /**
         * 成功返回数据
         */
        private List<TmsFirstMileReconciliationDetailDTO.ListDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
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
        private String id;

        /**
         * 主表id
         */
        private String mainId;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源编码
         */
        private String sourceCode;

        /**
         * 物流运输号
         */
        private String transportNo;

        /**
         * 货件/计划单号
         */
        private String businessCode;

        /**
         * 店铺ID
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 发货国家代号
         */
        private String fromCountry;

        /**
         * 发货国家代号
         */
        private String toCountry;

        /**
         * 签收日期
         */
        private LocalDate receiveDate;

        /**
         * 计费规则
         */
        private String feeRule;

        /**
         * 计费规则名称
         */
        private String feeRuleName;

        /**
         * 类型(对账类型)
         */
        private String type;

        /**
         * 总物流费用
         */
        private BigDecimal totalLogisticsCost;

        /**
         * 实际重量
         */
        private BigDecimal actualWeight;

        /**
         * 实际重量单位
         */
        private String actualWeightUnit;

        /**
         * 体积重
         */
        private BigDecimal volumeWeight;

        /**
         * 体积重单位
         */
        private String volumeWeightUnit;

        /**
         * 计费重
         */
        private BigDecimal billingWeight;

        /**
         * 计费重单位
         */
        private String billingWeightUnit;

        /**
         * 实际物流运费用
         */
        private BigDecimal actualShippingCost;

        /**
         * 实际报关费用
         */
        private BigDecimal actualDeclareCost;

        /**
         * 实际其他费用
         */
        private BigDecimal actualOtherCost;

        /**
         * 备注
         */
        private String remark;

        /**
         * 对账状态
         */
        private String status;

        /**
         * 确认时间
         */
        private LocalDate confirmDate;

        /**
         * 确认人id
         */
        private String confirmUserId;

        /**
         * 确认人名称
         */
        private String confirmUserName;


    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 主表id
         */
        @Size(max = 19, message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
         * 来源id
         */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19, message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32, message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
         * 来源编码
         */
        @NotBlank(message = "来源编码不能为空")
        @Size(max = 32, message = "来源编码最大长度不能超过32位")
        private String sourceCode;

        /**
         * 物流运输号
         */
        @Size(max = 19, message = "物流运输号最大长度不能超过19位")
        private String transportNo;

        /**
         * 货件/计划单号
         */
        @Size(max = 19, message = "货件/计划单号最大长度不能超过19位")
        private String businessCode;

        /**
         * 关联单号
         */
        private String relationCode;

        /**
         * 店铺ID
         */
        @Size(max = 32, message = "店铺ID最大长度不能超过32位")
        private String shopId;

        /**
         * 店铺名称
         */
        @Size(max = 64, message = "店铺名称最大长度不能超过64位")
        private String shopName;

        /**
         * 发货国家代号
         */
        @Size(max = 32, message = "发货国家代号最大长度不能超过32位")
        private String fromCountry;

        /**
         * 发货国家代号
         */
        @Size(max = 32, message = "发货国家代号最大长度不能超过32位")
        private String toCountry;

        /**
         * 签收日期
         */
        private LocalDate receiveDate;

        /**
         * 计费规则
         */
        @Size(max = 32, message = "计费规则最大长度不能超过32位")
        private String feeRule;

        /**
         * 计费规则名称
         */
        @Size(max = 64, message = "计费规则名称最大长度不能超过64位")
        private String feeRuleName;

        /**
         * 类型(对账类型)
         */
        @Size(max = 32, message = "类型(对账类型)最大长度不能超过32位")
        private String type;

        /**
         * 总物流费用
         */
        @Digits(integer = 12, fraction = 4, message = "总物流费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal totalLogisticsCost;

        /**
         * 实际重量
         */
        @Digits(integer = 12, fraction = 4, message = "实际重量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal actualWeight;

        /**
         * 实际重量单位
         */
        @Size(max = 32, message = "实际重量单位最大长度不能超过32位")
        private String actualWeightUnit;

        /**
         * 体积重
         */
        @Digits(integer = 12, fraction = 4, message = "体积重整数位不能超过12位，小数位不能超过4位")
        private BigDecimal volumeWeight;

        /**
         * 体积重单位
         */
        @Size(max = 32, message = "体积重单位最大长度不能超过32位")
        private String volumeWeightUnit;

        /**
         * 计费重
         */
        @Digits(integer = 12, fraction = 4, message = "计费重整数位不能超过12位，小数位不能超过4位")
        private BigDecimal billingWeight;

        /**
         * 计费重单位
         */
        @Size(max = 32, message = "计费重单位最大长度不能超过32位")
        private String billingWeightUnit;

        /**
         * 物流运费用
         */
        @Digits(integer = 12, fraction = 4, message = "实际物流运费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shippingCost;

        /**
         * 报关费用
         */
        @Digits(integer = 12, fraction = 4, message = "实际报关费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal declareCost;

        /**
         * 其他费用
         */
        @Digits(integer = 12, fraction = 4, message = "实际其他费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal otherCost;
        /**
         * 其他税费
         */
        @Digits(integer = 12, fraction = 4, message = "实际其他税费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal otherTaxCost;

        /**
         * 备注
         */
        @Size(max = 255, message = "备注最大长度不能超过255位")
        private String remark;

        /**
         * 对账状态
         */
        @Size(max = 32, message = "对账状态最大长度不能超过32位")
        private String status;

        /**
         * 确认时间
         */
        private LocalDate confirmDate;

        /**
         * 确认人id
         */
        @Size(max = 19, message = "确认人id最大长度不能超过19位")
        private String confirmUserId;

        /**
         * 确认人名称
         */
        @Size(max = 32, message = "确认人名称最大长度不能超过32位")
        private String confirmUserName;

        /**
         * 运输状态
         */
        private String transportStatus;

        /**
         * 运输状态名称
         */
        private String transportStatusName;

        /**
         * 渠道商ID
         */
        private String logisticsChannelId;
        /**
         * 对账次数 默认1
         */
        private Integer reconciliationCount;
        /**
         * 账单类型： actual=实际， initPeriod=期初
         */
        private String reconciliationType;
    }

    @Data
    public static class AddWaitListDTO {
        /**
         * 来源ID数组
         */
        @NotEmpty( message = "来源ID数组不能为空")
        private List<@NotBlank(message = "来源ID不能为空") String> sourceIdList;
    }
}