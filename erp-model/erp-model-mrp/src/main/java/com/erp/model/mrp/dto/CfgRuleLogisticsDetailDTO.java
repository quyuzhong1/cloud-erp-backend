package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    public static class UpdateDTO extends CommonDTO implements Serializable {

        private static final long serialVersionUID = -5784534922535264779L;
        /**
        * 主键id
        */
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 区域
         */
        @NotBlank(message = "区域不能为空")
        private String area;

        /**
         * 店铺类型（all全部店铺，part指定店铺）
         */
        @NotBlank(message = "店铺类型不能为空")
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
        @NotNull(message = "物流时效（天）不能为空")
        @Min(value = 0,message = "物流时效（天）最小值为0")
        @Max(value = 365,message = "物流时效（天）最大值为365")
        private Integer logisticsDays;
    }
}