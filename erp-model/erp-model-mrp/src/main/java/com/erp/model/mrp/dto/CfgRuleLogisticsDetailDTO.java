package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 备货物流明细（规则设置）请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@NoArgsConstructor
public class CfgRuleLogisticsDetailDTO implements Serializable {




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
        * 主表(cfg_rule_logistics)id
        */
        private String mainId;

        /**
         * 区域
         */
        private String area;

        /**
        * 店铺类型（all全部店铺，part指定店铺）
        */
        private String type;

        /**
        * 店铺Idjson
        */
        private String shopIdJson;

        /**
        * 海外仓id
        */
        private String warehouseId;

        /**
        * 物流时效（天）
        */
        private Integer logisticsDays;


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
         * 主表(cfg_rule_logistics)id
         */
        @NotBlank(message = "主表(cfg_rule_logistics)id不能为空")
        @Size(max = 19, message = "主表(cfg_rule_logistics)id最大长度不能超过19位")
        private String mainId;

        /**
         * 区域
         */
        private String area;

        /**
         * 店铺类型（all全部店铺，part指定店铺）
         */
        private String type;

        /**
         * 店铺id集合
         */
        private List<String> shopIdList;

        /**
         * 海外仓id
         */
        private String warehouseId;

        /**
         * 物流时效（天）
         */
        private Integer logisticsDays;
    }
}