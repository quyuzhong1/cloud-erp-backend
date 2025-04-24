package com.erp.model.plm.dto;

import com.common.business.annotation.Dict;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 模具 产品请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class MouldProductDTO implements Serializable {




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
        * 模具id
        */
        private String mouldDetailId;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 图片地址
        */
        private List<String> imagesUrl;

        /**
         * 模具类型
         */
        @Dict(tableName = "cfg_mould_setting", queryFieldName = "id")
        private String typeId;

        /**
         * 模具穴数
         */
        private String mouldHoles;

        /**
         * 模具长
         */
        private BigDecimal length;

        /**
         * 模具宽
         */
        private BigDecimal width;

        /**
         * 模具高
         */
        private BigDecimal height;

        /**
         * 模具材质
         */
        private String material;
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
    @Getter
    @Setter
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 255,message = "产品名称最大长度不能超过255位")
        private String productName;

        /**
        * 图片地址
        */
        @NotNull(message = "图片地址不能为空")
        private List<String> imagesUrl;

        /**
         * 模具类型
         */
        @NotBlank(message = "模具类型不能为空")
        @Size(max = 19,message = "模具类型最大长度不能超过19位")
        private String typeId;

        /**
         * 模具穴数
         */
        @NotBlank(message = "模具穴数不能为空")
        @Size(max = 255,message = "模具穴数最大长度不能超过255位")
        private String mouldHoles;

        /**
         * 模具长
         */
        private BigDecimal length;

        /**
         * 模具宽
         */
        private BigDecimal width;

        /**
         * 模具高
         */
        private BigDecimal height;

        /**
         * 模具材质
         */
        @NotBlank(message = "模具材质不能为空")
        @Size(max = 255,message = "模具材质最大长度不能超过255位")
        private String material;
    }


}