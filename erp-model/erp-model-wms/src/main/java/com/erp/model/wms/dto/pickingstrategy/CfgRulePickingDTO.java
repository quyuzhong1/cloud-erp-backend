package com.erp.model.wms.dto.pickingstrategy;

import cn.hutool.core.collection.CollUtil;
import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.wms.enums.PickingBillTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CfgRulePickingDTO {
    private CfgRulePickingDTO() {
        throw new IllegalStateException("Utility CfgRulePickingDTO class");
    }
    @Getter
    @Setter
    public static class PagingView {
        private String description;
        private String name;
        private Integer priority;
        private String id;
        private Boolean disabled;

        /**
         * 拣货禁用状态 false 未禁用
         */
        private Boolean pickDisabled;
        /**
         * 补货禁用状态 false 未禁用
         */
        private Boolean replenishDisabled;
        /**
         * 出库禁用状态 false 未禁用
         */
        private Boolean outStockDisabled;
        private String updateUserName;
        private LocalDateTime updateTime;
    }
    @Getter
    @Setter
    public static class PagingParam extends SortDTO implements Serializable {
        private static final long serialVersionUID = 1905122041950251207L;
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }
    @Getter
    @Setter
    public static class Add {
        @NotBlank(message = "名字不能为空")
        private String name;
        @NotNull(message = "状态不能为空")
        @Positive(message = "只能输入大于0的整数")
        private Integer priority;
        @NotNull(message = "状态不能为空")
        private Boolean disabled;

        /**
         * 拣货禁用状态 false 未禁用
         */
        private Boolean pickDisabled;
        /**
         * 补货禁用状态 false 未禁用
         */
        private Boolean replenishDisabled;
        /**
         * 出库禁用状态 false 未禁用
         */
        private Boolean outStockDisabled;

        private String description;
        /**
         * 上架仓位
         */
        private String inWarehouseLocation;
        /**
         * 上架仓位名称
         */
        private String inWarehouseLocationName;
        /**
         * 拣货仓位推荐
         */
        @Valid
        private List<CfgRuleActionDTO.Add> pickActions;
        /**
         * 补货仓位推荐
         */
        @Valid
        private List<CfgRuleActionDTO.Add> replenishActions;
        /**
         * 出库仓位推荐
         */
        @Valid
        private List<CfgRuleActionDTO.Add> outStockActions;

        @Valid
        @Size(min = 1, message = "至少存在一条规则条件")
        private List<CfgRuleConditionDTO.Add> conditionList;

        /** 拣货未禁用时，拣货仓位推荐不能为空 */
        @AssertTrue(message = "拣货仓位推荐不能为空")
        public boolean isPickActionsValid() {
            if (Boolean.TRUE.equals(pickDisabled)) {
                return true; // 已禁用，不校验集合
            }
            return CollUtil.isNotEmpty(pickActions);
        }
        @AssertTrue(message = "补货仓位推荐不能为空")
        public boolean isReplenishActionsValid() {
            if (Boolean.TRUE.equals(replenishDisabled)) {
                return true;
            }
            return CollUtil.isNotEmpty(replenishActions);
        }
        @AssertTrue(message = "出库仓位推荐不能为空")
        public boolean isOutStockActionsValid() {
            if (Boolean.TRUE.equals(outStockDisabled)) {
                return true;
            }
            return CollUtil.isNotEmpty(outStockActions);
        }

        @AssertTrue(message = "拣货/补货/出库至少启用一种仓位推荐")
        public boolean isAnyActionTypeEnabled() {
            return !Boolean.TRUE.equals(pickDisabled)
                    || !Boolean.TRUE.equals(replenishDisabled)
                    || !Boolean.TRUE.equals(outStockDisabled);
        }
    }

    @Getter
    @Setter
    public static class Update {
        @NotBlank(message = "id不能为空")
        private String id;
        @NotBlank(message = "名字不能为空")
        private String name;
        @NotNull(message = "状态不能为空")
        @Positive(message = "只能输入大于0的整数")
        private Integer priority;
        @NotNull(message = "状态不能为空")
        private Boolean disabled;

        /**
         * 拣货禁用状态 false 未禁用
         */
        private Boolean pickDisabled;
        /**
         * 补货禁用状态 false 未禁用
         */
        private Boolean replenishDisabled;
        /**
         * 出库禁用状态 false 未禁用
         */
        private Boolean outStockDisabled;

        private String description;
        /**
         * 上架仓位
         */
        private String inWarehouseLocation;
        /**
         * 上架仓位名称
         */
        private String inWarehouseLocationName;
        /**
         * 拣货仓位推荐
         */
        @Valid
        private List<CfgRuleActionDTO.Update> pickActions;
        /**
         * 补货仓位推荐
         */
        @Valid
        private List<CfgRuleActionDTO.Update> replenishActions;
        /**
         * 出库仓位推荐
         */
        @Valid
        private List<CfgRuleActionDTO.Update> outStockActions;

        @Valid
        @Size(min = 1, message = "至少存在一条规则条件")
        private List<CfgRuleConditionDTO.Update> conditionList;


        /** 拣货未禁用时，拣货仓位推荐不能为空 */
        @AssertTrue(message = "拣货仓位推荐不能为空")
        public boolean isPickActionsValid() {
            if (Boolean.TRUE.equals(pickDisabled)) {
                return true; // 已禁用，不校验集合
            }
            return CollUtil.isNotEmpty(pickActions);
        }
        @AssertTrue(message = "补货仓位推荐不能为空")
        public boolean isReplenishActionsValid() {
            if (Boolean.TRUE.equals(replenishDisabled)) {
                return true;
            }
            return CollUtil.isNotEmpty(replenishActions);
        }
        @AssertTrue(message = "出库仓位推荐不能为空")
        public boolean isOutStockActionsValid() {
            if (Boolean.TRUE.equals(outStockDisabled)) {
                return true;
            }
            return CollUtil.isNotEmpty(outStockActions);
        }

        @AssertTrue(message = "拣货/补货/出库至少启用一种仓位推荐")
        public boolean isAnyActionTypeEnabled() {
            return !Boolean.TRUE.equals(pickDisabled)
                    || !Boolean.TRUE.equals(replenishDisabled)
                    || !Boolean.TRUE.equals(outStockDisabled);
        }
    }

    @Getter
    @Setter
    public static class View {
        /**
         * 拣货仓位推荐
         */
        @Dict
        private List<CfgRuleActionDTO.View> pickActions;
        /**
         * 补货仓位推荐
         */
        @Dict
        private List<CfgRuleActionDTO.View> replenishActions;
        /**
         * 出库仓位推荐
         */
        @Dict
        private List<CfgRuleActionDTO.View> outStockActions;

        @Dict
        private List<CfgRuleConditionDTO.View> conditionList;
        private String description;
        private String name;
        private Integer priority;
        private String id;
        private Boolean disabled;

        /**
         * 拣货禁用状态 false 未禁用
         */
        private Boolean pickDisabled;
        /**
         * 补货禁用状态 false 未禁用
         */
        private Boolean replenishDisabled;
        /**
         * 出库禁用状态 false 未禁用
         */
        private Boolean outStockDisabled;
        /**
         * 上架仓位
         */
        private String inWarehouseLocation;
        /**
         * 上架仓位名称
         */
        private String inWarehouseLocationName;
    }

    @Getter
    @Setter
    public static class CfgExecutionDataDTO {
        /**
         * 单据类型
         * @see PickingBillTypeEnum
         */
        private String billType;
        /**
         * B2B客户
         */
        private String customerId;

        /**
         * B2B订单收货国家
         */
        private String countryCode;

        /**
         * 头程目的仓库
         */
        private String deliveryWarehouseId;
        /**
         * 来源单号
         */
        private String sourceCode;
        /**
         * 波次类型
         */
        private String waveType;
        /**
         * sku明细数据
         */
        private List<CfgExecutionDataDetailDTO> details;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CfgExecutionDataDetailDTO {
        /**
         * 拣货仓库id
         */
        private String warehouseId;
        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;
        private String platformSkuNo;
        /**
         * 数量
         */
        private Integer qty;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
    }

    @Getter
    @Setter
    public static class CfgRulePickingInventoryDTO {

        private String ruleId;

        private String warehouseId;

        private String warehouseAreaId;

        private String skuId;

        private String skuNo;

        private String warehouseLocation;

        private Integer qty;

        private Integer priority;

        private LocalDateTime updateTime;

        private Integer index;
    }

    /**
     * 缺货补货仓位推荐入参（SKU 维度）
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplenishShortageItemDTO {
        private String skuId;
        private String skuNo;
        /** 缺货/补货建议数量 */
        private Integer qty;
    }

    /**
     * 缺货补货仓位推荐结果（取货 + 上架）
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplenishLocationSuggestDTO {
        private String skuId;
        private String skuNo;
        private Integer qty;
        private String fromWarehouseArea;
        private String fromWarehouseLocation;
        private String toWarehouseArea;
        private String toWarehouseLocation;
        /** 命中的补货推荐规则 ID */
        private String ruleId;
    }

    /**
     * 出库仓位推荐入参（明细维度）
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutStockItemDTO {
        /** 出库明细 ID，用于回写仓位 */
        private String detailId;
        private String skuId;
        private String skuNo;
        /** 出库数量 */
        private Integer qty;
    }

    /**
     * 出库仓位推荐结果
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutStockLocationSuggestDTO {
        private String detailId;
        private String skuId;
        private String skuNo;
        private Integer qty;
        /** 有库存的推荐仓位 */
        private String stockLocation;
        /** 有库存仓位是否属于拣货区 */
        private Boolean inPickingArea;
        /** 非拣货区时需先移至空仓位 */
        private Boolean needMove;
        /**
         * 出库明细应写入的仓位：拣货区=stockLocation；非拣货区=空字符串
         */
        private String targetLocation;
        /** 命中的出库推荐规则 ID */
        private String ruleId;
    }
}
