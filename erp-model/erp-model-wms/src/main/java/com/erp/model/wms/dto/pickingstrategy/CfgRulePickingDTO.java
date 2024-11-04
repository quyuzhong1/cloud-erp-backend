package com.erp.model.wms.dto.pickingstrategy;

import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.wms.enums.PickingBillTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CfgRulePickingDTO {
    @Getter
    @Setter
    public static class PagingView {
        private String description;
        private String name;
        private Integer priority;
        private String id;
        private Boolean disabled;
        private String updateUserName;
        private LocalDateTime updateTime;
    }
    @Getter
    @Setter
    public static class PagingParam extends SortDTO {
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
        private String description;
        @Valid
        @Size(min = 1, message = "至少存在一条仓位分配规则")
        private List<CfgRuleActionDTO.Add> actions;
        @Valid
        @Size(min = 1, message = "至少存在一条规则条件")
        private List<CfgRuleConditionDTO.Add> conditionList;
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
        private String description;
        @Valid
        @Size(min = 1, message = "至少存在一条仓位分配规则")
        private List<CfgRuleActionDTO.Update> actions;
        @Valid
        @Size(min = 1, message = "至少存在一条规则条件")
        private List<CfgRuleConditionDTO.Update> conditionList;
    }

    @Getter
    @Setter
    public static class View {

        @Dict
        private List<CfgRuleActionDTO.View> actions;
        @Dict
        private List<CfgRuleConditionDTO.View> conditionList;
        private String description;
        private String name;
        private Integer priority;
        private String id;
        private Boolean disabled;
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
}
