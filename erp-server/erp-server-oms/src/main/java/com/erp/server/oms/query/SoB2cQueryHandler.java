package com.erp.server.oms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.oms.enums.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SoB2cQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }

        if("sku".equals(field)){
            return " EXISTS (SELECT 1 from so_b2c_detail sbd where sbd.main_id = sb2c.id and sbd.sku_no "+ compareCodeSplicingValueSql +" ) ";
        }

        if("platformSpuNo".equals(field)){
            return " exists ( select id from so_b2c_detail where is_deleted = false and main_id = sb2c.id and platform_spu_no "+compareCodeSplicingValueSql+" ) ";
        }

        if("platformSkuNo".equals(field)){
            return " exists ( select id from so_b2c_detail where is_deleted = false and main_id = sb2c.id and platform_sku_no "+compareCodeSplicingValueSql+" ) ";
        }

        if("category".equals(field)){
            return " sb2c.id in ( select so_b2c_id from so_b2c_ref_category  where is_deleted = false and category_id "+compareCodeSplicingValueSql+" ) ";
        }
        if("sb2cd.warehouse_id".equals(field)){
        	return " EXISTS (SELECT 1 from so_b2c_detail sbd where sbd.main_id = sb2c.id and sbd.warehouse_id "+ compareCodeSplicingValueSql +" ) ";
        }
        //标签类型
        if("lable".equals(field)){
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            List<String> valueList = com.common.business.utils.CollectionUtils.convertStrClzToList(value);
            StringBuilder sb = new StringBuilder();
            sb.append(" ( ");
            //是否是第一个，否则需要加连接符
            boolean isFirst = true;
            if(queryConditionEnum.equals(QueryConditionEnum.EQ) || queryConditionEnum.equals(QueryConditionEnum.IN_LIST)){
                for(String valueStr : valueList){
                    if(!isFirst){
                        sb.append(" or ");
                    }
                    if(valueStr.equals("frozen")){
                        sb.append(" (sb2c.label_json ~ 'RISK_CONTROL' or sb2c.label_json ~ 'IN_FROZEN' or sb2c.label_json ~ 'Unfulfillable') ");
                    }
                    if(valueStr.equals("fba")){
                        sb.append(" sb2c.label_json ~ 'AFN' ");
                    }
                    if(valueStr.equals("manual")){
                        sb.append(" sb2c.source_type = 'selfAdd' ");
                    }
                    if(valueStr.equals("intercept")){
                        sb.append(" sb2c.is_intercept = true ");
                    }
                    if(valueStr.equals("split")){
                        sb.append("  exists (select id from so_b2c_ref sbf where sbf.is_deleted = false and type = 'split' and sbf.target_id = sb2c.id) ");
                    }
                    if(valueStr.equals("merge")){
                        sb.append(" exists (select id from so_b2c_ref sbf where sbf.is_deleted = false and type = 'merge' and sbf.target_id = sb2c.id ) ");
                    }
                    if(valueStr.equals("aliexpressTaxed")){
                        sb.append(" (sb2cd.label_json ~ 'U_TAXED' or sb2cd.label_json ~ 'I_TAXED') ");
                        sb.append(" and exists ( select id from so_b2c_detail where is_deleted = false and main_id = sb2c.id and (label_json ~ 'U_TAXED' or label_json ~ 'I_TAXED')) ");
                    }
                    if(valueStr.equals("cainiaoWarehouse")){
                        sb.append(" sb2c.label_json ~ 'cainiaoInternationalWarehouse' ");
                    }
                    if(valueStr.equals("aliexpressAePlus")){
                        sb.append(" sb2c.label_json ~ 'AE_PLUS' ");
                    }
                    if(valueStr.equals("aliexpressUpExpress")){
                        sb.append(" sb2c.label_json ~ 'HBA_UP_EXPRESS' ");
                    }
                    if(valueStr.equals("leadTenTime")){
                        sb.append(" sb2c.label_json ~ 'leadTimeTag#10' ");
                    }
                    if(valueStr.equals("refunded")){
                        sb.append(" sb2c.label_json::json->>'isRefunded' = 'true' ");
                    }
                    isFirst = false;
                }
            }

            if(queryConditionEnum.equals(QueryConditionEnum.NE) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST)){
                for(String valueStr : valueList){
                    if(!isFirst){
                        sb.append(" and ");
                    }
                    if(valueStr.equals("frozen")){
                        sb.append(" (sb2c.label_json !~ 'RISK_CONTROL' and sb2c.label_json !~ 'IN_FROZEN' and sb2c.label_json !~ 'Unfulfillable') ");
                    }
                    if(valueStr.equals("fba")){
                        sb.append(" sb2c.label_json !~ 'AFN' ");
                    }
                    if(valueStr.equals("manual")){
                        sb.append(" sb2c.source_type != 'selfAdd' ");
                    }
                    if(valueStr.equals("intercept")){
                        sb.append(" sb2c.is_intercept = false ");
                    }
                    if(valueStr.equals("split")){
                        sb.append("  exists (select id from so_b2c_ref sbf where sbf.is_deleted = false and type != 'split' and sbf.target_id = sb2c.id) ");
                    }
                    if(valueStr.equals("merge")){
                        sb.append(" exists (select id from so_b2c_ref sbf where sbf.is_deleted = false and type != 'merge' and sbf.target_id = sb2c.id ) ");
                    }
                    if(valueStr.equals("aliexpressTaxed")){
                        sb.append(" ( sb2c.label_json !~ 'U_TAXED' AND sb2c.label_json !~ 'I_TAXED' )  ");
                        sb.append(" and exists ( select id from so_b2c_detail where is_deleted = false and main_id = sb2c.id and (label_json !~ 'U_TAXED' and label_json !~ 'I_TAXED')) ");
                    }
                    if(valueStr.equals("cainiaoWarehouse")){
                        sb.append(" sb2c.label_json !~ 'cainiaoInternationalWarehouse' ");
                    }
                    if(valueStr.equals("aliexpressAePlus")){
                        sb.append(" sb2c.label_json !~ 'AE_PLUS' ");
                    }
                    if(valueStr.equals("aliexpressUpExpress")){
                        sb.append(" sb2c.label_json !~ 'HBA_UP_EXPRESS' ");
                    }
                    if(valueStr.equals("leadTenTime")){
                        sb.append(" sb2c.label_json !~ 'leadTimeTag#10' ");
                    }
                    isFirst = false;
                }
            }
            sb.append(" ) ");
            return sb.toString();
        }
        if("warehouse".equals(field)){
            return " EXISTS (SELECT 1 from so_b2c_detail sbd where sbd.main_id = sb2c.id and sbd.warehouse_id "+ compareCodeSplicingValueSql +" ) ";
        }

        if("deliveryTime".equals(field)){
            compareCodeSplicingValueSql = compareCodeSplicingValueSql.replace("deliveryTime","sout.bill_date");
            return " EXISTS (SELECT 1 from so_outstock sout where sout.so_id = sb2c.id and sout.is_deleted = false and sout.bill_date "+compareCodeSplicingValueSql+" )";
        }

        //是否缺货 （待配货和配货中且sku数量大于可用库存且不是忽略库存计算SKU） 因为需要查询PLM系统和WMS系统，所以无法在这里直接处理
        if("isOutStock".equals(field)){
            Boolean bool = (Boolean) value;
            if(bool){
                super.buildDefaultDTO("sb2c.bill_status", Arrays.asList(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode(),SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode()));
            }else {
                return getQueryAllSql();
            }
        }

        if("orderDeliveryType".equals(field)){
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            List<String> valueList = com.common.business.utils.CollectionUtils.convertStrClzToList(value);
            StringBuilder sb = new StringBuilder();
            //是否是第一个，否则需要加连接符
            boolean isFirst = true;
            sb.append(" ( ");
            if(queryConditionEnum.equals(QueryConditionEnum.EQ) || queryConditionEnum.equals(QueryConditionEnum.IN_LIST) ){
                for(String valueStr : valueList) {
                    if (!isFirst) {
                        sb.append(" or ");
                    }
                    isFirst = false;
                    if (valueStr.equals("platformWarehouseDelivery")) {
                        sb.append(" (sb2c.label_json ~ 'AFN' or sb2c.label_json ~ 'cainiaoInternationalWarehouse' or sb2c.label_json ~ 'WFSFulfilled' or sb2c.label_json ~ '3PLFulfilled' or sb2c.label_json ~ 'fulfillment')");
                    }
                    if (valueStr.equals("transitWarehouseDelivery")) {
                        sb.append(" (sb2c.label_json ~ 'drop_off' or sb2c.label_json ~ 'cross_docking')");
                    }
                    if (valueStr.equals("selfDelivery")) {
                        sb.append(" (sb2c.label_json !~ 'AFN' and sb2c.label_json !~ 'cainiaoInternationalWarehouse' and sb2c.label_json !~ 'WFSFulfilled' and sb2c.label_json !~ '3PLFulfilled' and sb2c.label_json !~ 'fulfillment')");
                    }
                }
            }

            if(queryConditionEnum.equals(QueryConditionEnum.NE) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST) ){
                for(String valueStr : valueList) {
                    if (!isFirst) {
                        sb.append(" and ");
                    }
                    isFirst = false;
                    if (valueStr.equals("platformWarehouseDelivery")) {
                        sb.append(" (sb2c.label_json !~ 'AFN' and sb2c.label_json !~ 'cainiaoInternationalWarehouse' and sb2c.label_json !~ 'WFSFulfilled' and sb2c.label_json !~ '3PLFulfilled' and sb2c.label_json !~ 'fulfillment')");
                    }
                    if (valueStr.equals("transitWarehouseDelivery")) {
                        sb.append(" (sb2c.label_json !~ 'drop_off' and sb2c.label_json !~ 'cross_docking')");
                    }
                    if (valueStr.equals("selfDelivery")) {
                        sb.append(" (sb2c.label_json ~ 'AFN' or sb2c.label_json ~ 'cainiaoInternationalWarehouse' or sb2c.label_json ~ 'WFSFulfilled' or sb2c.label_json ~ '3PLFulfilled' or sb2c.label_json ~ 'fulfillment')");
                    }
                }
            }

            sb.append(" ) ");
            return sb.toString();
        }
        /**
         * B2C订单待处理类型归类,SoB2cWaitHandleTypeEnum枚举
         * 审核不通过（自动）：订单审核状态为审核不通过，不通过原因是自动审核条件不通过或拦截成功后自动不通过
         * 审核不通过（人工）：订单审核状态为审核不通过，不通过原因是人工审核条件不通过
         * 订单反审核：订单审核状态为待提交，待提交原因是人工反审核
         * 仓库规则不通过：订单状态是待配货，待配货原因是仓库规则不通过（注意区分没有走仓库规则的数据）
         * 物流规则不通过：订单状态是待配货，待配货原因是物流规则不通过
         */
        if ("waitHandle".equals(field))  {

            //审核不通过（自动）
            if (SoB2cWaitHandleTypeEnum.APPROVE_REJECT.getCode().equals(value)) {
                super.buildDefaultDTO("sb2c.approve_status", Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                super.buildDefaultDTO("sb2c.abnormal_type", Arrays.asList(SoB2cAbnormalTypeEnum.ENUM_APPROVE_REJECT.getCode()));
            }
            //审核不通过（手动）
            if (SoB2cWaitHandleTypeEnum.MANUAL_REJECT.getCode().equals(value)) {
                super.buildDefaultDTO("sb2c.approve_status", Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                super.buildDefaultDTO("sb2c.abnormal_type", Arrays.asList(SoB2cAbnormalTypeEnum.ENUM_MANUAL_REJECT.getCode()));
            }
            //订单反审核
            if (SoB2cWaitHandleTypeEnum.WAIT_SUBMIT.getCode().equals(value)) {
                super.buildDefaultDTO("sb2c.approve_status", Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
            }
            //仓库规则不通过
            if (SoB2cWaitHandleTypeEnum.WAREHOUSE_RULE_REJECT.getCode().equals(value)) {
                super.buildSplicingSQLDTO("sb2cd.is_match_warehouse_rule", QueryConditionEnum.EQ,Boolean.FALSE, QueryDataTypeEnum.BOOLEAN);
            }
            //物流规则不通过
            if (SoB2cWaitHandleTypeEnum.LOGISTICS_RULE_REJECT.getCode().equals(value)) {
                super.buildSplicingSQLDTO("sb2c.is_match_logistics_rule", QueryConditionEnum.EQ,Boolean.FALSE, QueryDataTypeEnum.BOOLEAN);
                super.buildDefaultDTO("sb2c.abnormal_type", Arrays.asList(SoB2cAbnormalTypeEnum.ENUM_DISTRIBUTION_REJECT.getCode()));
            }
        }
         return null;
    }


    /**
     * @description: tabSql
     * @author Will
     * @date: 2024/2/26 15:55
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        //审核状态
        List<String> approveStatusList = new ArrayList<>(1);
        //付款状态
        List<String> payStatusList = new ArrayList<>(1);
        //单据状态
        List<String> billStatusList = new ArrayList<>(1);

        if(value.equals("all")){
            super.buildSplicingSQLDTO("sb2c.invalid_status", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
        }
        // 待付款
        if (SoB2cTabEnum.ENUM_PAYMENT.getCode().equals(value)) {
            payStatusList.add(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            super.buildSplicingSQLDTO("sb2c.invalid_status", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
        }
        //待处理
        if (SoB2cTabEnum.ENUM_PENDING.getCode().equals(value)) {
            return "sb2c.bill_status != 'frozen' and sb2c.invalid_status = false and sb2c.pay_status = 'paid' and (sb2c.approve_status in ('waitSubmit','reject') or (sb2c.approve_status = 'approve' and sb2c.abnormal_type = 'distributionReject'))";
        }
        //审核中
        if (SoB2cTabEnum.ENUM_APPROVE_ING.getCode().equals(value)) {
            approveStatusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
            super.buildSplicingSQLDTO("sb2c.invalid_status", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
        }
        //待配货
        if (SoB2cTabEnum.ENUM_IN_DISTRIBUTION.getCode().equals(value)) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
            billStatusList.add(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode());
            super.buildSplicingSQLDTO("sb2c.invalid_status", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
        }
        //配货中
        if (SoB2cTabEnum.ENUM_IN_DISTRIBUTION.getCode().equals(value)) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
            billStatusList.add(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            billStatusList.add(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode());
            super.buildSplicingSQLDTO("sb2c.invalid_status", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
        }
        //待发货
        if (SoB2cTabEnum.ENUM_WAIT_SHIPPED.getCode().equals(value)) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
            billStatusList.add(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
            super.buildSplicingSQLDTO("sb2c.invalid_status", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
        }
        //已发货
        if (SoB2cTabEnum.ENUM_SHIPPED.getCode().equals(value)) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
            billStatusList.add(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            super.buildSplicingSQLDTO("sb2c.invalid_status", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
        }
        //冻结中
        if (SoB2cTabEnum.ENUM_FROZEN.getCode().equals(value)) {
            billStatusList.add(SoB2cBillStatusEnum.ENUM_FROZEN.getCode());
            super.buildSplicingSQLDTO("sb2c.invalid_status", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
        }
        //已作废
        if (SoB2cTabEnum.ENUM_INVALID.getCode().equals(value)) {
            super.buildSplicingSQLDTO("sb2c.invalid_status", QueryConditionEnum.EQ,true, QueryDataTypeEnum.BOOLEAN);
        }
        //订单异常
        if (SoB2cTabEnum.ENUM_ORDER_ERROR.getCode().equals(value)) {
            super.buildSplicingSQLDTO("sb2c.sign_order_error", QueryConditionEnum.NE,"", QueryDataTypeEnum.STRING);
            super.buildSplicingSQLDTO("sb2c.invalid_status", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
        }
        if (CollectionUtils.isNotEmpty(approveStatusList)) {
            super.buildDefaultDTO("sb2c.approve_status", approveStatusList);
        }
        if (CollectionUtils.isNotEmpty(billStatusList)) {
            super.buildDefaultDTO("sb2c.bill_status", billStatusList);
        }
        if (CollectionUtils.isNotEmpty(payStatusList)) {
            super.buildDefaultDTO("sb2c.pay_status", payStatusList);
        }
        return super.getSplicingSQL();
    }
}

