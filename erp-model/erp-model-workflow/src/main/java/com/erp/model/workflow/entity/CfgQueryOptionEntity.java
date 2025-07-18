package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 查询option配置表(数大臣单据字段)
 * </p>
 *
 * @author hcg
 * @since 2025-05-15
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_query_option")
public class CfgQueryOptionEntity extends BaseEntity<CfgQueryOptionEntity> {

    /**
    * 字段中文名
    */
    @TableField("condition_field_name")
    private String conditionFieldName;
    /**
    * 请求路径
    */
    @TableField("api_url")
    private String apiUrl;
    /**
    * 请求方法：post,get
    */
    @TableField("request_method")
    private String requestMethod;
    /**
    * 请求参数
    */
    @TableField("param")
    private String param;
    /**
    * 下拉框显示值
    */
    @TableField("select_label")
    private String selectLabel;
    /**
    * 下拉框绑定值
    */
    @TableField("select_value")
    private String selectValue;
    /**
    * 下拉框禁用绑定字段
    */
    @TableField("select_disabled")
    private String selectDisabled;
    /**
    * 查询绑定属性
    */
    @TableField("search_key_field")
    private String searchKeyField;
    /**
    * 接口类型
    */
    @TableField("api_type")
    private String apiType;
    /**
    * 字段类型：checkboxV2=多选,radioV2=单选,input=文本,datetime=日期,number=数值,amount=金额,attachmentV2=附件,fieldList=明细  枚举：CfgQueryOptionFieldTypeEnum
    */
    @TableField("field_type")
    private String fieldType;
    /**
    * 是否必填
    */
    @TableField("is_required")
    private Boolean isRequired;
    /**
    * 配置单据：purchaseOrder=采购订单，purchaseApplication=采购申请单，purchaseChange=采购变更单，productChange=变更管理，productBomInfo=BOM管理，salesDemand=备货申请单，poInstock=入库单，soOutstock=销售出库单，customerB2bChangeSeller=B2B客户表变更销售员，requisitionApplicationChange=要货申请变更单，ProductLogistics=物流产品信息，transferApplication=调拨申请单，supplier=供应商列表，poReturn=采购退货单，pilotApplication=试产量产单，soDeliveryNoticeChange=发货通知变更单，stocktakingTask=盘点任务，transferInfo=直接调拨单，poReceive=收货单，productDetail=产品管理，projectTask=任务列表，subcontractOrder=委外订单，purchasePrice=采购价目表，purchasePriceChange=采购调价表，soB2c=B2C销售订单，soInfo=B2B销售订单，firstMileDelivery=发货单，customerInfo=B2B客户列表，soChange=B2B销售变更单，deliveryPlan=第三方仓发货计划，requisitionApplication=要货申请，soPrice=销售价目表，soPriceChange=销售调价表，transferIn=分步式调入单，transferOut=分步式调出单  枚举：CfgQueryOptionBussinessKeyEnum
    */
    @TableField("bussiness_key")
    private String bussinessKey;
    /**
    * 字段英文名：对应cfg_process_exp中的field，cfg_process_field_map中的sys_field
    */
    @TableField("condition_field")
    private String conditionField;
    /**
    * 字段所属单据类型：table=表头,detail=明细  枚举：CfgQueryOptionFieldBelongsTypeEnum
    */
    @TableField("field_belongs_type")
    private String fieldBelongsType;
    /**
    * 逻辑关系 对应 dict_rule_condition key 多个逗号分割
    */
    @TableField("logic")
    private String logic;

    @TableField("controls")
    private String controls;
    /**
     * 值类型
     */
    @TableField("value_type")
    private String valueType;
    /**
     * 使用类型,CfgQueryOptionUseTypeEnum枚举
     */
    @TableField("use_type")
    private String useType;
    /**
     * 使用类型
     */
    @TableField("parent_id")
    private String parentId;
    /**
     * 使用类型
     */
    @TableField("class_path")
    private String classpath;

    /**
     * 拓展类型
     */
    @TableField("extend_type")
    private String extendType;
    /**
     * 表名
     */
    @TableField("table_name")
    private String tableName;
    /**
     * 使用类型
     */
    @TableField("sys_classify")
    private String sysClassify;
    /**
     * 表名(中文)
     */
    @TableField("table_cn_name")
    private String tableCnName;

    public static final String Condition_Field_Name = "condition_field_name";

    public static final String API_URL = "api_url";

    public static final String REQUEST_METHOD = "request_method";

    public static final String PARAM = "param";

    public static final String SELECT_LABEL = "select_label";

    public static final String SELECT_VALUE = "select_value";

    public static final String SELECT_DISABLED = "select_disabled";

    public static final String SEARCH_KEY_FIELD = "search_key_field";


    public static final String API_TYPE = "api_type";

    public static final String FIELD_TYPE = "field_type";

    public static final String IS_REQUIRED = "is_required";

    public static final String BUSSINESS_KEY = "bussiness_key";

    public static final String Condition_Field = "condition_field";

    public static final String FIELD_BELONGS_TYPE = "field_belongs_type";

    public static final String LOGIC = "logic";

    public static final String CONTROLS = "controls";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
