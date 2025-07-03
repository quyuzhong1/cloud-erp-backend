package com.erp.model.workflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 查询option配置表(数大臣单据字段)请求响应实体
 * </p>
 *
 * @author hcg
 * @since 2025-05-15
 */
@Data
@NoArgsConstructor
public class CfgQueryOptionDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 字段中文名
         */
        private String conditionFieldName;

        /**
         * 请求路径
         */
        private String apiUrl;

        /**
         * 请求方法：post,get
         */
        private String requestMethod;

        /**
         * 请求参数
         */
        private String param;

        /**
         * 下拉框显示值
         */
        private String selectLabel;

        /**
         * 下拉框绑定值
         */
        private String selectValue;

        /**
         * 下拉框禁用绑定字段
         */
        private String selectDisabled;

        /**
         * 查询绑定属性
         */
        private String searchKeyField;

        private String apiType;

        /**
         * 字段类型：checkboxV2=多选,radioV2=单选,input=文本,datetime=日期,number=数值,amount=金额,attachmentV2=附件,fieldList=明细
         */
        private String fieldType;

        /**
         * fieldTypeName
         */
        private String fieldTypeName;

        /**
         * 是否必填
         */
        private Boolean isRequired;

        private String conditionField;

        private String fieldBelongsType;

        /**
         * 明细字段所属单据（非字符串类型的）
         */
        private String sysParentId;

        /**
         * 数据唯一值， fieldBelongsType +conditionField
         */
        private String uniqueCode;
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
         * 字段：对应cfg_process_exp中的field，cfg_process_field_map中的sys_field
         */
        private String conditionFieldName;

        /**
         * 请求路径
         */
        @NotBlank(message = "请求路径不能为空")
        @Size(max = 100, message = "请求路径最大长度不能超过100位")
        private String apiUrl;

        /**
         * 请求方法：post,get
         */
        @NotBlank(message = "请求方法：post,get不能为空")
        @Size(max = 30, message = "请求方法：post,get最大长度不能超过30位")
        private String requestMethod;

        /**
         * 请求参数
         */
        @NotBlank(message = "请求参数不能为空")
        private String param;

        /**
         * 下拉框显示值
         */
        @NotBlank(message = "下拉框显示值不能为空")
        @Size(max = 30, message = "下拉框显示值最大长度不能超过30位")
        private String selectLabel;

        /**
         * 下拉框绑定值
         */
        @NotBlank(message = "下拉框绑定值不能为空")
        @Size(max = 30, message = "下拉框绑定值最大长度不能超过30位")
        private String selectValue;

        /**
         * 下拉框禁用绑定字段
         */
        @NotBlank(message = "下拉框禁用绑定字段不能为空")
        @Size(max = 30, message = "下拉框禁用绑定字段最大长度不能超过30位")
        private String selectDisabled;

        /**
         * 查询绑定属性
         */
        @NotBlank(message = "查询绑定属性不能为空")
        @Size(max = 30, message = "查询绑定属性最大长度不能超过30位")
        private String searchKeyField;

        /**
         * 接口类型
         */
        @NotBlank(message = "接口类型不能为空")
        @Size(max = 30, message = "接口类型最大长度不能超过30位")
        private String apiType;

        /**
         * 字段类型：checkboxV2=多选,radioV2=单选,input=文本,datetime=日期,number=数值,amount=金额,attachmentV2=附件,fieldList=明细
         */
        @NotBlank(message = "字段类型：checkboxV2=多选,radioV2=单选,input=文本,datetime=日期,number=数值,amount=金额,attachmentV2=附件,fieldList=明细不能为空")
        @Size(max = 30, message = "字段类型：checkboxV2=多选,radioV2=单选,input=文本,datetime=日期,number=数值,amount=金额,attachmentV2=附件,fieldList=明细最大长度不能超过30位")
        private String fieldType;

        /**
         * 是否必填
         */
        @NotNull(message = "是否必填不能为空")
        private Boolean isRequired;

        /**
         * 配置单据：purchaseOrder=采购订单，purchaseApplication=采购申请单，purchaseChange=采购变更单，productChange=变更管理，productBomInfo=BOM管理，salesDemand=备货申请单，poInstock=入库单，soOutstock=销售出库单，customerB2bChangeSeller=B2B客户表变更销售员，requisitionApplicationChange=要货申请变更单，ProductLogistics=物流产品信息，transferApplication=调拨申请单，supplier=供应商列表，poReturn=采购退货单，pilotApplication=试产量产单，soDeliveryNoticeChange=发货通知变更单，stocktakingTask=盘点任务，transferInfo=直接调拨单，poReceive=收货单，productDetail=产品管理，projectTask=任务列表，subcontractOrder=委外订单，purchasePrice=采购价目表，purchasePriceChange=采购调价表，soB2c=B2C销售订单，soInfo=B2B销售订单，firstMileDelivery=发货单，customerInfo=B2B客户列表，soChange=B2B销售变更单，deliveryPlan=第三方仓发货计划，requisitionApplication=要货申请，soPrice=销售价目表，soPriceChange=销售调价表，transferIn=分步式调入单，transferOut=分步式调出单
         */
        @NotBlank(message = "配置单据不能为空")
        @Size(max = 30, message = "配置单据最大长度不能超过30位")
        private String bussinessKey;

        /**
         * 字段名
         */
        @NotBlank(message = "字段名不能为空")
        @Size(max = 30, message = "字段名最大长度不能超过30位")
        private String conditionField;

        /**
         * 字段所属单据类型：table=表头,detail=明细
         */
        private String fieldBelongsType;

        /**
         * 逻辑关系 对应 dict_rule_condition key 多个逗号分割
         */
        private String logic;


    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class cfgApproveSyncDropDownDTO {

        private String id;

        private String conditionField;

        private String conditionFieldName;

        private String fieldBelongsType;


    }

    /**
     * 树状结构
     */
    @Data
    @NoArgsConstructor
    public static class TreeDTO {

        /**
         * 条件字段
         */
        private String conditionField;


        /**
         * 逻辑关系
         */
        private String logic;

        /**
         * 逻辑关系名
         */
        private String logicName;


        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        private List<TreeDTO> children;


    }

    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 条件字段 对应dict_rule_condition key
         */
        private String conditionField;

        /**
         * 字段名
         */
        private String conditionFieldName;



        /**
         * 空间 如时间戳 输入框之类
         */
        private String controls;

        /**
         * 对应api url
         */
        private String apiUrl;

        /**
         * 请求方式
         */
        private String requestMethod;


        /**
         * 对应下拉的绑定的字段
         */
        private String label;

        /**
         * 对应下拉的显示中文的名 的字段
         */
        private String value;

        /**
         * json 格式
         */
        private String param;

        private String searchKey;

        private String remoteLabel;

        private String valueType;
    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class MqParamsDTO  {

        private String tableName;

        private String sysClassify;

        /**
         * 使用类型，CfgQueryOptionUseTypeEnum枚举
         */
        private String useType = "allData";
    }
    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class VariablesParamsDTO  {

        private Map<String, Object> variablesMap;

        private String businessKey;
        /**
         * 使用类型，CfgQueryOptionUseTypeEnum枚举
         */
        private String useType = "allData";
    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class GenListDTO  {

        //系统分类
        private String model;
        //表名
        private String tableName;
        //单据类型
        private String bussinessKey;
        //字段所属单据类型
        private String fieldBelongsType;
        /**
         * 使用类型，CfgQueryOptionUseTypeEnum枚举
         */
        private String useType = "allData";
    }
    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class ListByFieldDTO  {

        //单据类型
        private String businessType;

        //条件的字段结合
        private List<String> fieldList;

    }
}