package com.erp.model.dmp.dto;

import java.math.BigDecimal;
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
 * 中台销售退货订单明细表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-30
*/
@Data
@NoArgsConstructor
public class DmpSoReturnDetailDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 来源详情id
        */
        private String thirdDetailId;

        /**
        * 销售平台原始详情id
        */
        private String platformDetailId;

        /**
        * 退货原因
        */
        private String reason;

        /**
        * skuId
        */
        private String skuId;

        /**
        * sku编码
        */
        private String skuNo;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 商品售价
        */
        private BigDecimal sellPrice0;

        /**
        * 金额
        */
        private BigDecimal amount;

        /**
        * 是否赠品：true/false
        */
        private Boolean isGift;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 仓位
        */
        private String warehouseLocation;

        /**
        * 第三方平台订单编号
        */
        private String thirdOrderCode;

        /**
        * 销售平台原始订单编号
        */
        private String platformOrderCode;

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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 来源详情id
        */
        @NotBlank(message = "来源详情id不能为空")
        @Size(max = 64,message = "来源详情id最大长度不能超过64位")
        private String thirdDetailId;

        /**
        * 销售平台原始详情id
        */
        @NotBlank(message = "销售平台原始详情id不能为空")
        @Size(max = 64,message = "销售平台原始详情id最大长度不能超过64位")
        private String platformDetailId;

        /**
        * 退货原因
        */
        @NotBlank(message = "退货原因不能为空")
        @Size(max = 64,message = "退货原因最大长度不能超过64位")
        private String reason;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 商品售价
        */
        @NotNull(message = "商品售价不能为空")
        @Digits(integer = 18, fraction = 6, message = "商品售价整数位不能超过18位，小数位不能超过6位")
        private BigDecimal sellPrice0;

        /**
        * 金额
        */
        @NotNull(message = "金额不能为空")
        @Digits(integer = 18, fraction = 6, message = "金额整数位不能超过18位，小数位不能超过6位")
        private BigDecimal amount;

        /**
        * 是否赠品：true/false
        */
        @NotNull(message = "是否赠品：true/false不能为空")
        private Boolean isGift;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 32,message = "仓库id最大长度不能超过32位")
        private String warehouseId;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 64,message = "仓库名称最大长度不能超过64位")
        private String warehouseName;

        /**
        * 仓位
        */
        @NotBlank(message = "仓位不能为空")
        @Size(max = 32,message = "仓位最大长度不能超过32位")
        private String warehouseLocation;

        /**
        * 第三方平台订单编号
        */
        @NotBlank(message = "第三方平台订单编号不能为空")
        @Size(max = 64,message = "第三方平台订单编号最大长度不能超过64位")
        private String thirdOrderCode;

        /**
        * 销售平台原始订单编号
        */
        @NotBlank(message = "销售平台原始订单编号不能为空")
        @Size(max = 64,message = "销售平台原始订单编号最大长度不能超过64位")
        private String platformOrderCode;

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