package com.erp.model.dmp.dto;

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
 * 销售退货订单主表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-30
*/
@Data
@NoArgsConstructor
public class DmpSoReturnInfoDTO implements Serializable {




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
        * 退货时间
        */
        private LocalDateTime returnTime;

        /**
        * 来源平台：gyy，kingdee，mabang
        */
        private String sourceSystem;

        /**
        * 第三方单据编号（唯一）
        */
        private String thirdCode;

        /**
        * 平台原始单号
        */
        private String platformCode;

        /**
        * 店铺编号
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 单据状态：1待处理 2已退款 3已重发 4已完成 5已作废
        */
        private String status;

        /**
        * 平台原始状态
        */
        private String platformStatus;

        /**
        * 国家二字码
        */
        private String country;

        /**
        * 买家账号
        */
        private String buyerUserId;

        /**
        * 买家姓名
        */
        private String buyerName;

        /**
        * 备注
        */
        private String remark;

        /**
        * 币种
        */
        private String currencyCode;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 金额
        */
        private BigDecimal allAmount;

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
        * 退货时间
        */
        private LocalDateTime returnTime;

        /**
        * 来源平台：gyy，kingdee，mabang
        */
        @NotBlank(message = "来源平台：gyy，kingdee，mabang不能为空")
        @Size(max = 32,message = "来源平台：gyy，kingdee，mabang最大长度不能超过32位")
        private String sourceSystem;

        /**
        * 第三方单据编号（唯一）
        */
        @NotBlank(message = "第三方单据编号（唯一）不能为空")
        @Size(max = 64,message = "第三方单据编号（唯一）最大长度不能超过64位")
        private String thirdCode;

        /**
        * 平台原始单号
        */
        @NotBlank(message = "平台原始单号不能为空")
        @Size(max = 64,message = "平台原始单号最大长度不能超过64位")
        private String platformCode;

        /**
        * 店铺编号
        */
        @NotBlank(message = "店铺编号不能为空")
        @Size(max = 32,message = "店铺编号最大长度不能超过32位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 64,message = "店铺名称最大长度不能超过64位")
        private String shopName;

        /**
        * 单据状态：1待处理 2已退款 3已重发 4已完成 5已作废
        */
        @NotBlank(message = "单据状态：1待处理 2已退款 3已重发 4已完成 5已作废不能为空")
        @Size(max = 32,message = "单据状态：1待处理 2已退款 3已重发 4已完成 5已作废最大长度不能超过32位")
        private String status;

        /**
        * 平台原始状态
        */
        @NotBlank(message = "平台原始状态不能为空")
        @Size(max = 32,message = "平台原始状态最大长度不能超过32位")
        private String platformStatus;

        /**
        * 国家二字码
        */
        @NotBlank(message = "国家二字码不能为空")
        @Size(max = 10,message = "国家二字码最大长度不能超过10位")
        private String country;

        /**
        * 买家账号
        */
        @NotBlank(message = "买家账号不能为空")
        @Size(max = 32,message = "买家账号最大长度不能超过32位")
        private String buyerUserId;

        /**
        * 买家姓名
        */
        @NotBlank(message = "买家姓名不能为空")
        @Size(max = 32,message = "买家姓名最大长度不能超过32位")
        private String buyerName;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        @Size(max = 32,message = "币种最大长度不能超过32位")
        private String currencyCode;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 12, fraction = 4, message = "汇率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal exchangeRate;

        /**
        * 金额
        */
        @NotNull(message = "金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal allAmount;

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


    }


}