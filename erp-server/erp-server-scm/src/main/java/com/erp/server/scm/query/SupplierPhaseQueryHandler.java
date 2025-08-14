package com.erp.server.scm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.SupplierPhaseTabFlagEnum;
import com.erp.server.scm.service.CommonService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 *
 */
@Component
public class SupplierPhaseQueryHandler extends AbstractQueryHandler {

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    /**
     * @description: tabSql拼接
     * @author Will
     * @date: 2025/07/25 14:58
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        //待我审核
        if (SupplierPhaseTabFlagEnum.APPROVE_ING.getCode().equals(value)) {
            super.buildDefaultDTO("sp.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
            //需要审核的业务ids
            List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.SUPPLIER_PHASE.getCode());
            if (CollectionUtils.isNotEmpty(businessIds)) {
                super.buildDefaultDTO("sp.id", businessIds);
            }else{
                //返回空结果
                return this.getQueryEmptySql();
            }
        }
        // 不通过
        if (SupplierPhaseTabFlagEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("sp.approve_status", Collections.singletonList(ApproveStatusEnum.REJECT.getStatus()));
        }
        //已审核
        if (SupplierPhaseTabFlagEnum.APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("sp.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
        }
        return super.getSplicingSQL();
    }

}

