package com.erp.model.plm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 模具明细请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class MouldDetailDTO implements Serializable {




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
        * 模具编号
        */
        private String mouldNo;

        /**
        * 外部模具编号(供应商)
        */
        private String thirdMouldNo;

        /**
        * 模具类型
        */
        private String typeId;

        /**
        * 模具穴数
        */
        private String moldHoles;

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

        /**
        * 模具寿命(万)(啤)
        */
        private Integer lifeCycle;

        /**
        * 开模周期(自然日)
        */
        private Integer developCycle;

        /**
        * 启用时间
        */
        private LocalDateTime enableDate;

        /**
        * 供应商id
        */
        private String supplierId;

        /**
        * 备注
        */
        private String remark;


    }

    /**
    * 新增
    */
    @Getter
    @Setter
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
        * 外部模具编号(供应商)
        */
        private String thirdMouldNo;

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
        private String moldHoles;

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

        /**
        * 模具寿命(万)(啤)
        */
        @NotNull(message = "模具寿命(万)(啤)不能为空")
        private Integer lifeCycle;

        /**
        * 开模周期(自然日)
        */
        @NotNull(message = "开模周期(自然日)不能为空")
        private Integer developCycle;

        /**
        * 启用时间
        */
        @NotNull(message = "启用时间不能为空")
        private LocalDateTime enableDate;

        /**
        * 供应商id
        */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 19,message = "供应商id最大长度不能超过19位")
        private String supplierId;

        /**
        * 备注
        */
        private String remark;


    }


}