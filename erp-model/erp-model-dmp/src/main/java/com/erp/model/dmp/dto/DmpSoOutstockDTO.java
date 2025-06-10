package com.erp.model.dmp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 中台销售订单出库详情请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-04-11
*/
@Data
@NoArgsConstructor
public class DmpSoOutstockDTO implements Serializable {




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
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 平台修改时间
        */
        private LocalDateTime platformUpdateTime;

        /**
        * 来源平台：gyy，kingdee，mabang
        */
        private String sourceSystem;

        /**
        * 单据编号（唯一）
        */
        private String thirdCode;

        /**
        * 平台原始单号
        */
        private String platformCode;

        /**
        * 状态 1.已发货 2.已作废 3.未发货
        */
        private String status;

        /**
        * 平台原始状态
        */
        private String platformStatus;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 物流单号
        */
        private String logisticsCode;

        /**
        * 店铺编码
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 国家二字码
        */
        private String country;

        /**
        * 买家城市
        */
        private String city;

        /**
        * 买家省份
        */
        private String province;

        /**
        * 买家地址1
        */
        private String manStreet;

        /**
        * 买家地址2
        */
        private String secondStreet;

        /**
        * 所属区域
        */
        private String district;

        /**
        * 币种
        */
        private String currencyCode;

        /**
        * 单据总金额
        */
        private BigDecimal allAmount;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 运费
        */
        private BigDecimal shippingCost;

        /**
        * 补贴金额
        */
        private BigDecimal subsidyAmount;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 转换id
        */
        private String convertId;

        /**
        * 下一层级id
        */
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

        /**
        * 第三方单号
        */
        private String thirdBillNo;

        /**
        * 仓管员
        */
        private String stockerName;

        /**
        * 单据日期
        */
        private LocalDateTime billDate;

        /**
        * 数据来源
        */
        private String dataSources;

        /**
        * 销售组织
        */
        private String saleOrgId;

        /**
        * 整单折扣
        */
        private BigDecimal totalDiscountAmount;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 物流公司代码
        */
        private String logisticsCompanyCode;

        /**
        * 物流公司名称
        */
        private String logisticsCompanyName;

        /**
        * 订单标签
        */
        private String tradeLabel;

        /**
        * 运单号
        */
        private String transportNo;

        /**
        * 财务组织代码
        */
        private String financialCompanyId;

        /**
         * 来源类型
         */
        private String sourceType;
        
        /**
         * 明细数据
         */
        private List<DmpSoOutstockDetailDTO.ViewDTO> detailList;
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
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 平台修改时间
        */
        private LocalDateTime platformUpdateTime;

        /**
        * 来源平台：gyy，kingdee，mabang
        */
        @NotBlank(message = "来源平台：gyy，kingdee，mabang不能为空")
        @Size(max = 32,message = "来源平台：gyy，kingdee，mabang最大长度不能超过32位")
        private String sourceSystem;

        /**
        * 单据编号（唯一）
        */
        @NotBlank(message = "单据编号（唯一）不能为空")
        @Size(max = 64,message = "单据编号（唯一）最大长度不能超过64位")
        private String thirdCode;

        /**
        * 平台原始单号
        */
        @NotBlank(message = "平台原始单号不能为空")
        @Size(max = 255,message = "平台原始单号最大长度不能超过255位")
        private String platformCode;

        /**
        * 状态 1.已发货 2.已作废 3.未发货
        */
        @NotBlank(message = "状态 1.已发货 2.已作废 3.未发货不能为空")
        @Size(max = 32,message = "状态 1.已发货 2.已作废 3.未发货最大长度不能超过32位")
        private String status;

        /**
        * 平台原始状态
        */
        @NotBlank(message = "平台原始状态不能为空")
        @Size(max = 32,message = "平台原始状态最大长度不能超过32位")
        private String platformStatus;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 物流单号
        */
        @NotBlank(message = "物流单号不能为空")
        @Size(max = 500,message = "物流单号最大长度不能超过500位")
        private String logisticsCode;

        /**
        * 店铺编码
        */
        @NotBlank(message = "店铺编码不能为空")
        @Size(max = 32,message = "店铺编码最大长度不能超过32位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 64,message = "店铺名称最大长度不能超过64位")
        private String shopName;

        /**
        * 国家二字码
        */
        @NotBlank(message = "国家二字码不能为空")
        @Size(max = 10,message = "国家二字码最大长度不能超过10位")
        private String country;

        /**
        * 买家城市
        */
        @NotBlank(message = "买家城市不能为空")
        @Size(max = 64,message = "买家城市最大长度不能超过64位")
        private String city;

        /**
        * 买家省份
        */
        @NotBlank(message = "买家省份不能为空")
        @Size(max = 64,message = "买家省份最大长度不能超过64位")
        private String province;

        /**
        * 买家地址1
        */
        @NotBlank(message = "买家地址1不能为空")
        @Size(max = 500,message = "买家地址1最大长度不能超过500位")
        private String manStreet;

        /**
        * 买家地址2
        */
        @NotBlank(message = "买家地址2不能为空")
        @Size(max = 255,message = "买家地址2最大长度不能超过255位")
        private String secondStreet;

        /**
        * 所属区域
        */
        @NotBlank(message = "所属区域不能为空")
        @Size(max = 255,message = "所属区域最大长度不能超过255位")
        private String district;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        @Size(max = 255,message = "币种最大长度不能超过255位")
        private String currencyCode;

        /**
        * 单据总金额
        */
        @NotNull(message = "单据总金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "单据总金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal allAmount;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 12, fraction = 4, message = "汇率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal exchangeRate;

        /**
        * 运费
        */
        @NotNull(message = "运费不能为空")
        @Digits(integer = 12, fraction = 4, message = "运费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shippingCost;

        /**
        * 补贴金额
        */
        @NotNull(message = "补贴金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "补贴金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal subsidyAmount;

        /**
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 转换id
        */
        @NotBlank(message = "转换id不能为空")
        @Size(max = 19,message = "转换id最大长度不能超过19位")
        private String convertId;

        /**
        * 下一层级id
        */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 19,message = "下一层级id最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

        /**
        * 第三方单号
        */
        @NotBlank(message = "第三方单号不能为空")
        @Size(max = 255,message = "第三方单号最大长度不能超过255位")
        private String thirdBillNo;

        /**
        * 仓管员
        */
        @NotBlank(message = "仓管员不能为空")
        @Size(max = 255,message = "仓管员最大长度不能超过255位")
        private String stockerName;

        /**
        * 单据日期
        */
        private LocalDateTime billDate;

        /**
        * 数据来源
        */
        @NotBlank(message = "数据来源不能为空")
        @Size(max = 255,message = "数据来源最大长度不能超过255位")
        private String dataSources;

        /**
        * 销售组织
        */
        @NotBlank(message = "销售组织不能为空")
        @Size(max = 255,message = "销售组织最大长度不能超过255位")
        private String saleOrgId;

        /**
        * 整单折扣
        */
        @NotNull(message = "整单折扣不能为空")
        @Digits(integer = 12, fraction = 4, message = "整单折扣整数位不能超过12位，小数位不能超过4位")
        private BigDecimal totalDiscountAmount;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 100,message = "来源id最大长度不能超过100位")
        private String sourceId;

        /**
        * 物流公司代码
        */
        @NotBlank(message = "物流公司代码不能为空")
        @Size(max = 40,message = "物流公司代码最大长度不能超过40位")
        private String logisticsCompanyCode;

        /**
        * 物流公司名称
        */
        @NotBlank(message = "物流公司名称不能为空")
        @Size(max = 40,message = "物流公司名称最大长度不能超过40位")
        private String logisticsCompanyName;

        /**
        * 订单标签
        */
        @NotBlank(message = "订单标签不能为空")
        @Size(max = 255,message = "订单标签最大长度不能超过255位")
        private String tradeLabel;

        /**
        * 运单号
        */
        @NotBlank(message = "运单号不能为空")
        @Size(max = 255,message = "运单号最大长度不能超过255位")
        private String transportNo;

        /**
        * 财务组织代码
        */
        @NotBlank(message = "财务组织代码不能为空")
        @Size(max = 64,message = "财务组织代码最大长度不能超过64位")
        private String financialCompanyCode;


    }


}