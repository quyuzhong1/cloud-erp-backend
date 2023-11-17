package com.erp.model.wms.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 发货单箱规信息请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class FirstMileCartonDTO implements Serializable {




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
        * 箱规编号
        */
        private String boxSpecNo;

        /**
        * 包装重量
        */
        private BigDecimal packageWeight;

        /**
        * 箱子尺寸（长）
        */
        private BigDecimal boxLength;

        /**
        * 箱子尺寸（宽）
        */
        private BigDecimal boxWidth;

        /**
        * 箱子尺寸（高）
        */
        private BigDecimal boxHeight;

        /**
        * 箱数
        */
        private Integer boxQty;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 详情
         */
        private List<FirstMileCartonDetailDTO.AddDTO> detailList;
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
         * 详情
         */
        private List<FirstMileCartonDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
        * 主表id
        */
        private String mainId;

        /**
        * 箱规编号
        */
        @NotBlank(message = "箱规编号不能为空")
        @Size(max = 255,message = "箱规编号最大长度不能超过255位")
        private String boxSpecNo;

        /**
        * 包装重量
        */
        @NotNull(message = "包装重量不能为空")
        @Digits(integer = 8, fraction = 2, message = "包装重量整数位不能超过8位，小数位不能超过2位")
        private BigDecimal packageWeight;

        /**
        * 箱子尺寸（长）
        */
        @NotNull(message = "箱子尺寸（长）不能为空")
        @Digits(integer = 8, fraction = 2, message = "箱子尺寸（长）整数位不能超过8位，小数位不能超过2位")
        private BigDecimal boxLength;

        /**
        * 箱子尺寸（宽）
        */
        @NotNull(message = "箱子尺寸（宽）不能为空")
        @Digits(integer = 8, fraction = 2, message = "箱子尺寸（宽）整数位不能超过8位，小数位不能超过2位")
        private BigDecimal boxWidth;

        /**
        * 箱子尺寸（高）
        */
        @NotNull(message = "箱子尺寸（高）不能为空")
        @Digits(integer = 8, fraction = 2, message = "箱子尺寸（高）整数位不能超过8位，小数位不能超过2位")
        private BigDecimal boxHeight;

        /**
        * 箱数
        */
        @NotNull(message = "箱数不能为空")
        private Integer boxQty;
    }


    /**
     * 装箱清单
     */
    @Data
    @NoArgsConstructor
    public static class ListPackingDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 发货单号
         */
        private String code;

        /**
         * 箱数
         */
        private Integer boxQty;

        /**
         * 详情
         */
        private List<FirstMileCartonDetailDTO.ListPackingDetailDTO> detailList;
    }
}