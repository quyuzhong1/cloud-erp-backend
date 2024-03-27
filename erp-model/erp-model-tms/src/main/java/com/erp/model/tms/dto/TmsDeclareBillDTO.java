package com.erp.model.tms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
 * 报关单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
*/
@Data
@NoArgsConstructor
public class TmsDeclareBillDTO implements Serializable {




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
        * 合同协议号
        */
        private String code;

        /**
        * 来源类型：头程,B2B
        */
        private String sourceType;

        /**
        * 报关状态
        */
        private String declareStatus;

        /**
        * 物流商id
        */
        private String logisticsSupplierId;

        /**
        * 发货类型
        */
        private String deliveryType;

        /**
        * 目的国家
        */
        private String country;

        /**
        * 总净重
        */
        private BigDecimal netWeight;

        /**
        * 总毛重
        */
        private BigDecimal grossWeight;

        /**
        * 报关日期
        */
        private LocalDateTime declareDate;

        /**
        * 报关类型
        */
        private String declareType;

        /**
        * 预录入编号
        */
        private String preInputNo;

        /**
        * 申报地海关
        */
        private String destCustoms;

        /**
        * 发货人id
        */
        private String senderId;

        /**
        * 发货人名称
        */
        private String senderName;

        /**
        * 出境关别
        */
        private String exportCustomsName;

        /**
        * 出口日期
        */
        private LocalDateTime exportDate;

        /**
        * 收货人名称
        */
        private String receiverName;

        /**
        * 监管方式
        */
        private String dictSupervisionMethod;

        /**
        * 征免性质
        */
        private String dictNatureLevy;

        /**
        * 许可证号
        */
        private String licenseNo;

        /**
        * 贸易国
        */
        private String tradingArea;

        /**
        * 运抵国
        */
        private String toArea;

        /**
        * 运抵港
        */
        private String toPort;

        /**
        * 出境口岸
        */
        private String exportPort;

        /**
        * 包装种类
        */
        private String dictPackType;

        /**
        * 成交方式
        */
        private String dictTransactionMethod;

        /**
        * 备注
        */
        private String remark;

        /**
        * 运费
        */
        private BigDecimal shippingFee;

        /**
        * 保费
        */
        private BigDecimal insuranceFee;

        /**
        * 杂费
        */
        private BigDecimal otherFee;

        /**
        * 总箱数
        */
        private Integer boxQty;

        /**
        * 是否作废 
        */
        private Boolean isInvalid;

        /**
        * 合并后表头
        */
        private String mergedCode;


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
        * 来源类型：头程,B2B
        */
        @NotBlank(message = "来源类型：头程,B2B不能为空")
        @Size(max = 255,message = "来源类型：头程,B2B最大长度不能超过255位")
        private String sourceType;

        /**
        * 报关状态
        */
        @NotBlank(message = "报关状态不能为空")
        @Size(max = 30,message = "报关状态最大长度不能超过30位")
        private String declareStatus;

        /**
        * 物流商id
        */
        @NotBlank(message = "物流商id不能为空")
        @Size(max = 30,message = "物流商id最大长度不能超过30位")
        private String logisticsSupplierId;

        /**
        * 发货类型
        */
        @NotBlank(message = "发货类型不能为空")
        @Size(max = 30,message = "发货类型最大长度不能超过30位")
        private String deliveryType;

        /**
        * 目的国家
        */
        @NotBlank(message = "目的国家不能为空")
        @Size(max = 30,message = "目的国家最大长度不能超过30位")
        private String country;

        /**
        * 总净重
        */
        @NotNull(message = "总净重不能为空")
        @Digits(integer = 12, fraction = 4, message = "总净重整数位不能超过12位，小数位不能超过4位")
        private BigDecimal netWeight;

        /**
        * 总毛重
        */
        @NotNull(message = "总毛重不能为空")
        @Digits(integer = 12, fraction = 4, message = "总毛重整数位不能超过12位，小数位不能超过4位")
        private BigDecimal grossWeight;

        /**
        * 报关日期
        */
        private LocalDateTime declareDate;

        /**
        * 报关类型
        */
        @NotBlank(message = "报关类型不能为空")
        @Size(max = 30,message = "报关类型最大长度不能超过30位")
        private String declareType;

        /**
        * 预录入编号
        */
        @NotBlank(message = "预录入编号不能为空")
        @Size(max = 255,message = "预录入编号最大长度不能超过255位")
        private String preInputNo;

        /**
        * 申报地海关
        */
        @NotBlank(message = "申报地海关不能为空")
        @Size(max = 30,message = "申报地海关最大长度不能超过30位")
        private String destCustoms;

        /**
        * 发货人id
        */
        @NotBlank(message = "发货人id不能为空")
        @Size(max = 30,message = "发货人id最大长度不能超过30位")
        private String senderId;

        /**
        * 发货人名称
        */
        @NotBlank(message = "发货人名称不能为空")
        @Size(max = 50,message = "发货人名称最大长度不能超过50位")
        private String senderName;

        /**
        * 出境关别
        */
        @NotBlank(message = "出境关别不能为空")
        @Size(max = 30,message = "出境关别最大长度不能超过30位")
        private String exportCustomsName;

        /**
        * 出口日期
        */
        private LocalDateTime exportDate;

        /**
        * 收货人名称
        */
        @NotBlank(message = "收货人名称不能为空")
        @Size(max = 255,message = "收货人名称最大长度不能超过255位")
        private String receiverName;

        /**
        * 监管方式
        */
        @NotBlank(message = "监管方式不能为空")
        @Size(max = 30,message = "监管方式最大长度不能超过30位")
        private String dictSupervisionMethod;

        /**
        * 征免性质
        */
        @NotBlank(message = "征免性质不能为空")
        @Size(max = 30,message = "征免性质最大长度不能超过30位")
        private String dictNatureLevy;

        /**
        * 许可证号
        */
        @NotBlank(message = "许可证号不能为空")
        @Size(max = 255,message = "许可证号最大长度不能超过255位")
        private String licenseNo;

        /**
        * 贸易国
        */
        @NotBlank(message = "贸易国不能为空")
        @Size(max = 30,message = "贸易国最大长度不能超过30位")
        private String tradingArea;

        /**
        * 运抵国
        */
        @NotBlank(message = "运抵国不能为空")
        @Size(max = 30,message = "运抵国最大长度不能超过30位")
        private String toArea;

        /**
        * 运抵港
        */
        @NotBlank(message = "运抵港不能为空")
        @Size(max = 30,message = "运抵港最大长度不能超过30位")
        private String toPort;

        /**
        * 出境口岸
        */
        @NotBlank(message = "出境口岸不能为空")
        @Size(max = 30,message = "出境口岸最大长度不能超过30位")
        private String exportPort;

        /**
        * 包装种类
        */
        @NotBlank(message = "包装种类不能为空")
        @Size(max = 30,message = "包装种类最大长度不能超过30位")
        private String dictPackType;

        /**
        * 成交方式
        */
        @NotBlank(message = "成交方式不能为空")
        @Size(max = 30,message = "成交方式最大长度不能超过30位")
        private String dictTransactionMethod;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 运费
        */
        @NotNull(message = "运费不能为空")
        @Digits(integer = 12, fraction = 4, message = "运费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shippingFee;

        /**
        * 保费
        */
        @NotNull(message = "保费不能为空")
        @Digits(integer = 12, fraction = 4, message = "保费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal insuranceFee;

        /**
        * 杂费
        */
        @NotNull(message = "杂费不能为空")
        @Digits(integer = 12, fraction = 4, message = "杂费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal otherFee;

        /**
        * 总箱数
        */
        @NotNull(message = "总箱数不能为空")
        private Integer boxQty;

        /**
        * 是否作废 
        */
        @NotNull(message = "是否作废 不能为空")
        private Boolean isInvalid;

        /**
        * 合并后表头
        */
        @NotBlank(message = "合并后表头不能为空")
        @Size(max = 255,message = "合并后表头最大长度不能超过255位")
        private String mergedCode;


    }


}