package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.SupplierCountDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.erp.server.wms.service.WarehouseReceiveDetailService;
import com.erp.server.wms.service.WarehouseReceiveService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/17 16:01
 */
@RestController
@RequestMapping("feign/warehouseReceive")
public class WarehouseReceiveFeignController {

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Resource
    private WarehouseReceiveService warehouseReceiveService;

    @PostMapping("/listWarehouseReceiveByPodIds")
    public List<WarehouseReceiveDetailEntity> listWarehouseReceiveByPodIds(@RequestBody List<String> purchaseDetailIds) {
        return warehouseReceiveDetailService.listWarehouseReceiveByPodIds(purchaseDetailIds);
    }

    @PostMapping("/addWarehouseReceive")
    public String add(@RequestBody WarehouseReceiveDTO.AddDTO dto) {
        WarehouseReceiveEntity entity = warehouseReceiveService.add(dto);
        return entity.getId();
    }

    /**
     * 根据供应商id集合获取入库单量和入库数量
     * @param dto
     * @return
     */
    @PostMapping("/getReceiveInfoBySupplierIds")
    public List<WarehouseReceiveDTO.SupplierReceiveInfoDTO> getReceiveInfoBySupplierIds(@RequestBody WarehouseReceiveDTO.SupplierReceiveParamDTO dto) {
        return warehouseReceiveService.getReceiveInfoBySupplierIds(dto);
    }

    /**
     * 根据供应商id集合获取入库单量和入库数量
     * @param dto
     * @return
     */
    @PostMapping("/listReceiveBySourceTypeAndIds")
    public List<WarehouseReceiveEntity> listReceiveBySourceTypeAndIds(@RequestBody WarehouseReceiveDTO.SourceParamDTO dto) {
        return warehouseReceiveService.listReceiveBySourceTypeAndIds(dto);
    }

    /**
     * 根据供应商获取当前周期内 已确认的收货单数量
     * @param supplierId
     * @return
     */
    @GetMapping("/countOrderBySupplierId")
    public SupplierCountDTO countOrderBySupplierId(@RequestParam("supplierId") String supplierId){
        return warehouseReceiveService.countOrderBySupplierId(supplierId);
    }
    /***
     * 同销售订单获取 收货详情
     * @param purchaseOrderIds
     * @return
     */
    @PostMapping("/getReceiveListByPurchaseOrderIds")
    public List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> getReceiveListByPurchaseOrderIds(@RequestBody List<String> purchaseOrderIds){
        return warehouseReceiveService.getReceiveListByPurchaseOrderIds(purchaseOrderIds);
    }


    /***
     * 同销售订单获取 收货详情
     * @param purchaseOrderIds
     * @return
     */
    @PostMapping("/getReceiveListByPurchaseOrderIdsAll")
    public List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> getReceiveListByPurchaseOrderIdsAll(@RequestBody List<String> purchaseOrderIds){
        return warehouseReceiveService.getReceiveListByPurchaseOrderIdsAll(purchaseOrderIds);
    }
    /***
     * 清洗入库状态
     * @return
     */
    @PostMapping("/instockStatusCleanJob")
    public void instockStatusCleanJob(){
        warehouseReceiveService.instockStatusCleanJob();;
    }

    /**
     * 根据ids查询收货信息
     * @author will
     * @date 2025/6/12 09:53
     * @param idList
     * @return List<ReceiveSourceDTO>
     */
    @PostMapping("/listReceiveSourceByDetailIds")
    public List<WarehouseReceiveDTO.ReceiveSourceDTO> listReceiveSourceByDetailIds(@RequestBody List<String> idList){
        return warehouseReceiveService.listReceiveSourceByDetailIds(idList);
    }


    /**
     * 根据SKU集合获取收货信息
     * @param dto
     * @return
     */
    @PostMapping("/getReceiveByParams")
    public List<WarehouseReceiveDTO.ReceiveInfoDTO> getReceiveByParams(@RequestBody WarehouseReceiveDTO.ReceiveParamDTO dto) {
        return warehouseReceiveService.getReceiveByParams(dto);
    }
}
