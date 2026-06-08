package com.erp.server.wms.wdt;

import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
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
    void saveTaskList(List<VirtualWarehousePushHandleDetailEntity> handleDetailList, List<String> transferIdList,
                      String sourceCode, String operateCode, String sourceType);

    /**
     * 新增分货提交前校验旺店通实体仓可用库存。
     * 调入虚拟仓未绑定旺店通（/virtualWarehouse/view thirdMappingList.sysType=wdt）不校验；已绑定且未借调校验分货实体仓库存；
     * 已绑定且借调、借调仓已绑定旺店通则叠加借调仓库存；借调仓未绑定则跳过校验。
     */
    void checkAllocationWdtInventory(VirtualWarehouseAllocationEntity allocationEntity,
                                     List<VirtualWarehouseAllocationDetailEntity> detailEntityList,
                                     List<VirtualWarehouseAllocationDTO.TransferWarehouseDTO> transferWarehouseList);

    String saveWdtInventoryTask(VirtualWarehouseAllocationEntity allocationEntity, List<VirtualWarehouseAllocationDetailEntity> detailEntityList);
}
