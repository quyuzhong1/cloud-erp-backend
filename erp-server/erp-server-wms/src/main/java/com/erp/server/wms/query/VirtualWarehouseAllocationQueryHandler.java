package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.VirtualWarehouseAllocationStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationSyncStatusEnum;
import org.springframework.stereotype.Component;

/**
 * 分货单搜索条件
 *
 * @author hyj
 */
@Component
public class VirtualWarehouseAllocationQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }


    /**
     * @param value
     * @return String
     * @description: tabSql
     */
    public String getTabSql(Object value) {
        // 待提交
        if (VirtualWarehouseAllocationStatusEnum.WAIT_SUBMIT.getCode().equals(value)) {
            super.buildDefaultDTO("vma.status", VirtualWarehouseAllocationStatusEnum.WAIT_SUBMIT.getCode());
        }
        // 已处理
        if (VirtualWarehouseAllocationStatusEnum.HANDLE.getCode().equals(value)) {
            super.buildDefaultDTO("vma.status", VirtualWarehouseAllocationStatusEnum.HANDLE.getCode());
        }
        // 已作废
        if (VirtualWarehouseAllocationStatusEnum.INVALID.getCode().equals(value)) {
            super.buildDefaultDTO("vma.status", VirtualWarehouseAllocationStatusEnum.INVALID.getCode());
        }
        //同步失败
        if ("failedSync".equals(value)) {
            super.buildDefaultDTO("vmad.sync_status", VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode());
        }
        return super.getSplicingSQL();
    }
}
