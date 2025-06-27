package com.erp.model.workflow.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 流程配置请求响应实体
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
@Data
@NoArgsConstructor
public class CfgProcessDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 配置编码：purchaseOrder=采购订单，purchaseApplication=采购申请单，purchaseChange=采购变更单，productChange=变更管理，productBomInfo=BOM管理，salesDemand=备货申请单，poInstock=入库单，soOutstock=销售出库单，
         * customerB2bChangeSeller=B2B客户表变更销售员，requisitionApplicationChange=要货申请变更单，ProductLogistics=物流产品信息，transferApplication=调拨申请单，supplier=供应商列表，poReturn=采购退货单，
         * pilotApplication=试产量产单，soDeliveryNoticeChange=发货通知变更单，stocktakingTask=盘点任务，transferInfo=直接调拨单，poReceive=收货单，productDetail=产品管理，projectTask=任务列表，subcontractOrder=委外订单，
         * purchasePrice=采购价目表，purchasePriceChange=采购调价表，soB2c=B2C销售订单，soInfo=B2B销售订单，firstMileDelivery=发货单，customerInfo=B2B客户列表，soChange=B2B销售变更单，deliveryPlan=第三方仓发货计划，
         * requisitionApplication=要货申请，soPrice=销售价目表，soPriceChange=销售调价表，transferIn=分步式调入单，transferOut=分步式调出单
         */
        private String code;

        /**
         * 配置名称
         */
        private String name;

        /**
         * 配置单据
         */
        private String bussinessKey;

        private List<CfgProcessRuleDTO.ViewDTO> processRuleDTOList;
    }

    /**
     * 新增/删除
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddOrUpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 执行条件
         */
        List<CfgProcessRuleDTO.AddOrUpdateDTO> processRuleDTOList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 配置名称
         */
        @NotBlank(message = "配置名称不能为空")
        @Size(max = 100, message = "配置名称最大长度不能超过100位")
        private String name;

        /**
         * 配置单据
         */
        @NotBlank(message = "流程单据不能为空")
        @Size(max = 30, message = "流程单据最大长度不能超过30位")
        private String bussinessKey;

        /**
         * 单据编码
         */
        @Size(max = 30, message = "配置编码最大长度不能超过30位")
        private String code;
    }

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {

        /**
         * 页面高级查询
         *
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;
    }

    @Data
    @NoArgsConstructor
    public static class ProcessViewDTO {
        /**
         * processId
         */
        private String id;

        /**
         * 配置编码
         */
        private String code;

        /**
         * 配置名称
         */
        private String name;

        /**
         * 配置单据：purchaseOrder=采购订单，purchaseApplication=采购申请单，purchaseChange=采购变更单，productChange=变更管理，productBomInfo=BOM管理，salesDemand=备货申请单，poInstock=入库单，
         * soOutstock=销售出库单，customerB2bChangeSeller=B2B客户表变更销售员，requisitionApplicationChange=要货申请变更单，ProductLogistics=物流产品信息，transferApplication=调拨申请单，
         * supplier=供应商列表，poReturn=采购退货单，pilotApplication=试产量产单，soDeliveryNoticeChange=发货通知变更单，stocktakingTask=盘点任务，transferInfo=直接调拨单，poReceive=收货单，
         * productDetail=产品管理，projectTask=任务列表，subcontractOrder=委外订单，purchasePrice=采购价目表，purchasePriceChange=采购调价表，soB2c=B2C销售订单，soInfo=B2B销售订单，
         * firstMileDelivery=发货单，customerInfo=B2B客户列表，soChange=B2B销售变更单，deliveryPlan=第三方仓发货计划，requisitionApplication=要货申请，soPrice=销售价目表，soPriceChange=销售调价表
         */
        private String bussinessKey;
        /**
         * 执行流程
         */
        private String type;
        /**
         * 流程定义id
         */
        private String processDefinitionName;
        /**
         * 启动条件
         */
        private String ruleDesc;
        /**
         * 启用状态
         */
        private Boolean disabled;
        /**
         * 启用状态名称
         */
        private String disabledName;
        /**
         * 执行条件id
         */
        private String ruleId;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 创建人
         */
        private String createUserName;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        /**
         * 更新人
         */
        private String updateUserName;
    }

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型
         */
        private String tabFlag;
        /**
         * 类型
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 创建审批
     */
    @Data
    @NoArgsConstructor
    public static class StartDTO {

        /**
         * 业务类型
         */
        @NotBlank(message = "业务类型不能为空")
        private String businessKey;

        /**
         * 业务发起人
         */
        @NotBlank(message = "发起人不能为空")
        private String userId;

        /**
         * 业务表id
         */
        @NotBlank(message = "业务表id不能为空")
        private String businessId;

        /**
         * 业务单号
         */
        private String businessCode;

        /**
         * 流程配置类型 RuleType
         */
        @NotBlank(message = "业务名称不能为空")
        private String ruleType;

        /**
         * 业务表id
         */
        @NotBlank(message = "流程配置id不能为空")
        private String ruleId;

        /**
         *
         */
        @NotBlank(message = "")
        private Map<String,Object> variablesMap;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessSelectDTO {
        /**
         * 编码
         */
        private String code;
        /**
         * 名称
         */
        private String name;
        /**
         * 是否禁用，true禁用，false启用
         */
        private Boolean disabled;
    }
}