package com.erp.server.wms.wdt;

import com.erp.model.wms.entity.VirtualWarehousePushHandleDetailEntity;

import java.util.List;

/**
 * 旺店通虚拟仓订单创建setting.strategy.VirtualWarehouse.create
 * 旺店通对接地址：https://open.wangdian.cn/qjb/open/apidoc/doc?path=setting.strategy.VirtualWarehouse.create
 *
 * @author hyj
 * @date 2024-06-14
 */
public interface SyncWdtVirtualWarehousePushOrderService {

    /**
     * 保存推送旺店通虚拟仓订单创建任务
     *
     * @param handleDetailList SKU明细列表
     * @param sourceCode       来源单据编号
     * @param operateCode      操作代码: 审核/反审核
     * @return void
     */
    void saveTaskList(List<VirtualWarehousePushHandleDetailEntity> handleDetailList,
                                         String sourceCode, String operateCode, String sourceType);
}
