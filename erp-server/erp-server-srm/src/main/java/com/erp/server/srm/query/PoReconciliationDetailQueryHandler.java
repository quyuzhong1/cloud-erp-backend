package com.erp.server.srm.query;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.model.srm.enums.ConfirmStatusEnum;
import com.erp.server.srm.service.CommonService;
import com.erp.server.srm.service.PoReconciliationService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class PoReconciliationDetailQueryHandler extends AbstractQueryHandler {

    @Resource
    private PoReconciliationService poReconciliationService;

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {

        //查询待对账明细
        if("waitReconciliationDetail".equals(field)){
            //对账单
            PoReconciliationEntity entity = poReconciliationService.getById(value.toString());
            if (ObjectUtil.isEmpty(entity)) {
                //返回空结果
                return this.getQueryEmptySql();
            }
            super.buildDefaultDTO("prd.main_id",entity.getId());
            super.buildDefaultDTO("prd.supplier_id",entity.getSupplierId());
            super.buildDefaultDTO("prd.settle_org_id",entity.getSettleOrgId());
            super.buildDefaultDTO("prd.business_status", ConfirmStatusEnum.CONFIRM.getCode());
        }
        return null;
    }
}

