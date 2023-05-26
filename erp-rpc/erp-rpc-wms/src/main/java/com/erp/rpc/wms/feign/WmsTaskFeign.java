package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.workflow.dto.WorkOptionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/17 15:56
 */
@FeignClient(name = "erp-wms",configuration = {FeignErrorDecoder.class})
public interface WmsTaskFeign {

    /**
     * 根据仓库id
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
    List<PoInstockDetailEntity> listPurchaseStockInDetailBySourceDetailIds(List<String> sourceDetailIds);

    /**
     * 根据来采购订单明细ids查询入库明细
     */
    @PostMapping("feign/purchaseStockIn/listDetailByPodIds")
    List<PoInstockDetailEntity> listPurchaseStockInDetailByPodIds(List<String> PodIds);

    /**
     * 批量新增入库单
     */
    @PostMapping("feign/purchaseStockIn/batchAddPurchaseStockIn")
    Boolean batchAddPurchaseStockIn(List<PoInstockDTO.AddDTO> resultList);

    /**
     * 批量新增入库单
     */
    @PostMapping("feign/warehouseReceive/addWarehouseReceive")
    String addWarehouseReceive(WarehouseReceiveDTO.AddDTO dto);

    /**
     * 批量新增退货单
     */
    @PostMapping("feign/purchaseReturnOrder/addReturnOrder")
    Boolean batchAddReturnOrder(List<PurchaseReturnOrderDTO.AddDTO> dto);

    /**
     * 获取入库数量
     **/
    @PostMapping("feign/purchaseStockIn/getStockInQty")
    List<PoInstockDTO.GetStockInQty> getStockInQty(@RequestBody List<String> ids);

    /**
     * 获取退货数量
     **/
    @PostMapping("feign/purchaseReturnOrder/listReturnOrderDetailByPodIds")
    List<PurchaseReturnOrderDetailEntity> listReturnOrderDetailByPodIds(@RequestBody List<String> ids);

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @PostMapping("feign/wmsWorkOption/getTableNum")
    Integer getTableNum(@RequestBody WorkOptionDTO.TableNumDTO tableNumDTO);

    /**
     * 更新业务单据状态
     */
    @PostMapping("feign/syncKingdee/updateBusinessSyncKingdeeStatus")
    void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, Object> params);

    /**
     * 采购收货审核
     * @Author Luo_WG
     * @Date 2023/5/17 10:51
     * @param baseApproveParamDTO
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/wmsWorkOption/warehouseReceiveApprove")
    Boolean warehouseReceiveApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO);

    /**
     * 采购入库审核
     * @author Will
     * @date: 2023/4/11 20:11
     * @param baseApproveParamDTO
     * @return ApiResult
     */
    @PostMapping("feign/wmsWorkOption/poInstockApprove")
    void poInstockApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO);

    /**
     * 采购退货审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("feign/wmsWorkOption/purchaseReturnOrderApprove")
    Boolean purchaseReturnOrderApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO);

    /**
     * 根据销售 销售订单ids 获取是否有下推的单据
     * @author yl
     * @date 2023-05-25 10:27
     * @param soDetailIdList
     * @return java.lang.Integer
     */
    @PostMapping("feign/soDeliveryNotice/getPushDownBySoDetailIds")
    Integer getPushDownBySoDetailIds(@RequestBody List<String> soDetailIdList);

    /**
     * 关闭关联单据的关闭状态
     * @author yl
     * @date 2023-05-25 19:25
     * @param terminateSoDetailIds
     * @return void
     */
    @PostMapping("feign/soDeliveryNotice/closeBySoDetailIds")
    void closeBySoDetailIds(@RequestBody List<String> terminateSoDetailIds);
}
