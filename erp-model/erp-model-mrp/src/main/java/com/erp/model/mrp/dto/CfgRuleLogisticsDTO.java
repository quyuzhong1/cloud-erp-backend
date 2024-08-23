package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 备货物流（规则设置）请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@NoArgsConstructor
public class CfgRuleLogisticsDTO implements Serializable {




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
        * 排序字段
        */
        private Integer index;

        /**
        * 物流方式
        */
        private String logisticsMethod;

        /**
        * 物流时效（天）
        */
        private Integer logisticsDays;

        /**
        * 发货频率（天）
        */
        private Integer logisticsCycleDays;

        /**
        * 备货id（cfg_rule_stock_up）
        */
        private String stockUpId;


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
        * 排序字段
        */
        @NotNull(message = "排序字段不能为空")
        private Integer index;

        /**
        * 物流方式
        */
        @NotBlank(message = "物流方式不能为空")
        @Size(max = 32,message = "物流方式最大长度不能超过32位")
        private String logisticsMethod;

        /**
        * 物流时效（天）
        */
        @NotNull(message = "物流时效（天）不能为空")
        private Integer logisticsDays;

        /**
        * 发货频率（天）
        */
        @NotNull(message = "发货频率（天）不能为空")
        private Integer logisticsCycleDays;

        /**
        * 备货id（cfg_rule_stock_up）
        */
        @NotBlank(message = "备货id（cfg_rule_stock_up）不能为空")
        @Size(max = 19,message = "备货id（cfg_rule_stock_up）最大长度不能超过19位")
        private String stockUpId;


    }


}