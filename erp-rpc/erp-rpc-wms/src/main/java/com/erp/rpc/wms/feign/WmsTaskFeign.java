package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.PurchaseStockInDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.model.wms.entity.PurchaseStockInDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/17 15:56
 */
@FeignClient(name = "erp-wms")
public interface WmsTaskFeign {

    /**
     * 根据userIds查询用户集合
     */
    @PostMapping("feign/warehouse/listWarehouseByIds")
    List<WarehouseDTO.UpdateDTO> listWarehouseByIds(@RequestBody List<String> warehouseIds);

    /**
     * 查询所有审核通过并启用的仓库
     */
    @GetMapping("feign/warehouse/listApproveWarehouse")
    List<WarehouseDTO.UpdateDTO> listApproveWarehouse();

    /**
     * 根据采购订单明细ids查询收货明细
     */
    @PostMapping("feign/warehouseReceive/listWarehouseReceiveByPodIds")
    List<WarehouseReceiveDetailEntity> listWarehouseReceiveDetailByPodIds(@RequestBody List<String> purchaseDetailIds);

    /**
     * 根据来源明细ids查询退货明细
     */
    @PostMapping("feign/purchaseReturnOrder/listDetailBySourceDetailIds")
    List<PurchaseReturnOrderDetailEntity> listPurchaseReturnOrderDetailBySourceDetailIds(List<String> sourceDetailIds);

    /**
     * 根据来源明细ids查询入库明细
     */
    @PostMapping("feign/purchaseStockIn/listDetailBySourceDetailIds")
    List<PurchaseStockInDetailEntity> listPurchaseStockInDetailBySourceDetailIds(List<String> sourceDetailIds);

    /**
     * 批量新增入库单
     */
    @PostMapping("feign/purchaseStockIn/batchAddPurchaseStockIn")
    Boolean batchAddPurchaseStockIn(List<PurchaseStockInDTO.AddDTO> resultList);

    /**
     * 批量新增入库单
     */
    @PostMapping("feign/warehouseReceive/addWarehouseReceive")
    String addWarehouseReceive(WarehouseReceiveDTO.AddDTO dto);
}
