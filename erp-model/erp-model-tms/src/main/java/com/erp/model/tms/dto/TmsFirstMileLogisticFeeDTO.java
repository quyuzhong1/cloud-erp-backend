package com.erp.model.tms.dto;

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
 * 头程物流单费用请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-03-19
*/
@Data
@NoArgsConstructor
public class TmsFirstMileLogisticFeeDTO implements Serializable {




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
        * 系统配置id
        */
        private String cfgCostId;

        /**
        * 费用名称
        */
        private String costName;

        /**
        * 预估费用
        */
        private BigDecimal estimatedFee;


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
        @Size(max = 50,message = "主表id最大长度不能超过50位")
        private String mainId;

        /**
        * 系统配置id
        */
        @NotBlank(message = "系统配置id不能为空")
        @Size(max = 255,message = "系统配置id最大长度不能超过255位")
        private String cfgCostId;

        /**
        * 费用名称
        */
        @NotBlank(message = "费用名称不能为空")
        @Size(max = 64,message = "费用名称最大长度不能超过64位")
        private String costName;

        /**
        * 预估费用
        */
        private BigDecimal estimatedFee;


    }


}