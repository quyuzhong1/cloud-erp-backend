package com.erp.model.dmp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
 * 中台原始销售订单明细表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-11-25
*/
@Data
@NoArgsConstructor
public class DmpSoOriginalDetailDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 第三方明细id
        */
        private String thirdDetailId;

        /**
        * 状态
        */
        private String status;

        /**
        * 平台货品名称
        */
        private String goodsName;

        /**
        * 平台货品编号
        */
        private String goodsNo;

        /**
        * 数量
        */
        private BigDecimal num;

        /**
        * 单价
        */
        private BigDecimal price;

        /**
        * 分摊优惠
        */
        private BigDecimal shareDiscount;

        /**
        * 退款金额
        */
        private BigDecimal refundAmount;

        /**
        * 物流单号
        */
        private String logisticsNo;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 第三方明细id
        */
        @NotBlank(message = "第三方明细id不能为空")
        @Size(max = 64,message = "第三方明细id最大长度不能超过64位")
        private String thirdDetailId;

        /**
        * 状态
        */
        @NotBlank(message = "状态不能为空")
        @Size(max = 4,message = "状态最大长度不能超过4位")
        private String status;

        /**
        * 平台货品名称
        */
        @NotBlank(message = "平台货品名称不能为空")
        @Size(max = 255,message = "平台货品名称最大长度不能超过255位")
        private String goodsName;

        /**
        * 平台货品编号
        */
        @NotBlank(message = "平台货品编号不能为空")
        @Size(max = 40,message = "平台货品编号最大长度不能超过40位")
        private String goodsNo;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        @Digits(integer = 12, fraction = 4, message = "数量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal num;

        /**
        * 单价
        */
        @NotNull(message = "单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal price;

        /**
        * 分摊优惠
        */
        @NotNull(message = "分摊优惠不能为空")
        @Digits(integer = 12, fraction = 4, message = "分摊优惠整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shareDiscount;

        /**
        * 退款金额
        */
        @NotNull(message = "退款金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "退款金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal refundAmount;

        /**
        * 物流单号
        */
        @NotBlank(message = "物流单号不能为空")
        @Size(max = 40,message = "物流单号最大长度不能超过40位")
        private String logisticsNo;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

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


    }


}