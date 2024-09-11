package com.erp.model.mrp.dto;

import cn.hutool.json.JSONArray;
import com.erp.model.mrp.entity.CfgRuleLogisticsDetailEntity;
import com.erp.model.mrp.entity.CfgRuleLogisticsEntity;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

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
         * 物流方式名称
         */
        private String logisticsMethodName;
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

        /**
         * 物流明细信息
         */
        private List<CfgRuleLogisticsDetailDTO.ViewDTO> detailList;
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
        @Min(value = 0,message = "物流时效（天）最小值为0")
        @Max(value = 365,message = "物流时效（天）最大值为365")
        private Integer logisticsDays;

        /**
        * 发货频率（天）
        */
        @NotNull(message = "发货频率（天）不能为空")
        @Min(value = 0,message = "发货频率（天）最小值为0")
        @Max(value = 365,message = "发货频率（天）最大值为365")
        private Integer logisticsCycleDays;

        /**
         * 物流明细信息
         */
        @Valid
        private List<CfgRuleLogisticsDetailDTO.UpdateDTO> detailList;
    }


    @Getter
    @Setter
    public static class LogisticsResultDTO {
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
        private JSONArray shopIdJson;
        /**
         * 海外仓id
         */
        private String warehouseId;

        public static LogisticsResultDTO buildLogisticsResult(CfgRuleLogisticsDetailEntity detail, CfgRuleLogisticsEntity logistics) {
            LogisticsResultDTO dto = new LogisticsResultDTO();
            dto.setLogisticsDays(detail.getLogisticsDays());
            dto.setLogisticsMethod(logistics.getLogisticsMethod());
            dto.setArea(detail.getArea());
            dto.setType(detail.getType());
            dto.setLogisticsCycleDays(logistics.getLogisticsCycleDays());
            dto.setShopIdJson(detail.getShopIdJson());
            dto.setWarehouseId(detail.getWarehouseId());
            return dto;
        }

        public static LogisticsResultDTO buildLogisticsResult(CfgRuleLogisticsEntity logistics) {
            LogisticsResultDTO dto = new LogisticsResultDTO();
            dto.setLogisticsDays(logistics.getLogisticsDays());
            dto.setLogisticsMethod(logistics.getLogisticsMethod());
            dto.setLogisticsCycleDays(logistics.getLogisticsCycleDays());
            return dto;
        }
    }

}