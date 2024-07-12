package com.erp.model.dmp.dto;

import java.math.BigDecimal;
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
 * @since 2024-07-12
*/
@Data
@NoArgsConstructor
public class DmpSoOutstockPositionDTO implements Serializable {




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
        * 主id
        */
        private String mainId;

        /**
        * 批次号
        */
        private String batchNo;

        /**
        * 有效期
        */
        private String expireDate;

        /**
        * 销售出库单详情id
        */
        private String stockoutDetailId;

        /**
        * 货位号
        */
        private String positionNo;

        /**
        * 当前货位出库货品总量
        */
        private BigDecimal positionGoodsCount;

        /**
        * 货位明细id
        */
        private String recId;

        /**
        * 货位id
        */
        private String positionId;

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
        * 主id
        */
        @NotBlank(message = "主id不能为空")
        @Size(max = 255,message = "主id最大长度不能超过255位")
        private String mainId;

        /**
        * 批次号
        */
        @NotBlank(message = "批次号不能为空")
        @Size(max = 255,message = "批次号最大长度不能超过255位")
        private String batchNo;

        /**
        * 有效期
        */
        @NotBlank(message = "有效期不能为空")
        @Size(max = 50,message = "有效期最大长度不能超过50位")
        private String expireDate;

        /**
        * 销售出库单详情id
        */
        @NotBlank(message = "销售出库单详情id不能为空")
        @Size(max = 255,message = "销售出库单详情id最大长度不能超过255位")
        private String stockoutDetailId;

        /**
        * 货位号
        */
        @NotBlank(message = "货位号不能为空")
        @Size(max = 500,message = "货位号最大长度不能超过500位")
        private String positionNo;

        /**
        * 当前货位出库货品总量
        */
        @NotNull(message = "当前货位出库货品总量不能为空")
        @Digits(integer = 12, fraction = 4, message = "当前货位出库货品总量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal positionGoodsCount;

        /**
        * 货位明细id
        */
        @NotBlank(message = "货位明细id不能为空")
        @Size(max = 50,message = "货位明细id最大长度不能超过50位")
        private String recId;

        /**
        * 货位id
        */
        @NotBlank(message = "货位id不能为空")
        @Size(max = 50,message = "货位id最大长度不能超过50位")
        private String positionId;

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