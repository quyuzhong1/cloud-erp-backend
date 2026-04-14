package com.erp.server.wms.query;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.server.wms.service.CommonService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * @Author: will
 * @Date: 2026/03/25 09:58
 **/
@Component
public class QcApplicationSrmQueryHandler extends AbstractQueryHandler {

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }


    /**
     * @description: tabSql拼接
     * @author Will
     * @date: 2026/03/25 09:58
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {

        SupplierEntity supplierEntity = commonService.getSupplierEntity();
        //默认查询当前登录人的绑定的供应商数据
        super.buildDefaultDTO("qad.supplier_id", ObjectUtil.isEmpty(supplierEntity) ? "" : supplierEntity.getId());

        // 待提交
        if (ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(value)) {
            super.buildDefaultDTO("qa.approve_status", Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
        }
        //审核中
        if (ApproveStatusEnum.APPROVE_ING.getCode().equals(value)) {
            super.buildDefaultDTO("qa.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getCode()));
        }
        //审核通过
        if (ApproveStatusEnum.APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("qa.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getCode()));
        }
        //不通过
        if (ApproveStatusEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("qa.approve_status", Collections.singletonList(ApproveStatusEnum.REJECT.getStatus()));
        }
        return super.getSplicingSQL();
    }
}