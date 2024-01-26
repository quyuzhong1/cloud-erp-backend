package com.erp.server.srm.query;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.srm.enums.DeliveryOrderEnum;
import com.erp.model.srm.enums.PoReconciliationEnum;
import com.erp.server.srm.service.CommonService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class PoReconciliationQueryHandler extends AbstractQueryHandler {

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {

        //选项卡
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }

    /**
     * @description: tabSql拼接
     * @author Will
     * @date: 2024/1/18 19:58
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {

        SupplierEntity supplierEntity = commonService.getSupplierEntity();
        //默认查询当前登录人的绑定的供应商数据
        super.buildDefaultDTO("pr.supplier_id", ObjectUtil.isEmpty(supplierEntity) ? "" : supplierEntity.getId());

        //待供方确认
        if(PoReconciliationEnum.TabFlagEnum.TO_BE_SUPPLIER_CONFIRM.getCode().equals(value)){
            super.buildDefaultDTO("pr.status", PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_SUPPLIER_CONFIRM.getCode());
        }
        //待采方确认
        if(PoReconciliationEnum.TabFlagEnum.TO_BE_CONFIRM.getCode().equals(value)){
            super.buildDefaultDTO("pr.status", PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_PURCHASE_CONFIRM.getCode());
        }
        if(PoReconciliationEnum.TabFlagEnum.CONFIRM.getCode().equals(value)){
            super.buildDefaultDTO("pr.status", PoReconciliationEnum.PoReconciliationStatusEnum.CONFIRM.getCode());
        }
        if(PoReconciliationEnum.TabFlagEnum.RECEIVED.getCode().equals(value)){
            super.buildDefaultDTO("pr.status", PoReconciliationEnum.PoReconciliationStatusEnum.RECEIVED.getCode());
        }
        return super.getSplicingSQL();
    }
}

