package com.erp.server.oms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.model.oms.enums.SoB2cTabEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SoB2cAbnormalQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
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
            return "sb2c.invalid_status = false and sb2c.pay_status = 'paid' and (sb2c.approve_status in ('waitSubmit','reject') or (sb2c.approve_status = 'approve' and sb2c.abnormal_type = 'distributionReject'))";
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

