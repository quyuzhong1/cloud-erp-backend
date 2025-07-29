package com.erp.server.scm.service;

import cn.hutool.json.JSONArray;
import com.common.business.service.SuperService;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.enums.ConfirmTypeEnum;
import com.erp.model.scm.enums.ExecutionStatusEnum;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
public interface PurchaseOrderDetailService extends SuperService<PurchaseOrderDetailEntity> {
    /**
     * @description: 新增采购订单明细
     * @author Will
     * @date: 2023/3/24 10:56
     * @param details
     * @param purchaseOrderId
     */
    void add(List<PurchaseOrderDetailDTO.AddDTO> details, String purchaseOrderId);
    /**
     * @description: 根据采购订单id和skuid查询
     * @author Will
     * @date: 2023/3/27 15:02
     * @param purchaseOrderId 
     * @param skuId 
     * @return PurchaseOrderDetailEntity 
     */
    PurchaseOrderDetailEntity getByPurchaseOrderIdAndSkuId(String purchaseOrderId, String skuId);
    /**
     * @description: 修改采购订单明细
     * @author Will
     * @date: 2023/3/27 15:07
     * @param details
     * @param purchaseOrderId

     */
    void update(List<PurchaseOrderDetailDTO.UpdateDTO> details, String purchaseOrderId);
    /**
     * @description: 根据采购订单id查询
     * @author Will
     * @date: 2023/3/27 15:09
     * @param purchaseOrderId
     * @return List<PurchaseOrderDetailEntity>
     */
    List<PurchaseOrderDetailEntity> listByPurchaseOrderId(String purchaseOrderId);

    /**
     * @description: 根据采购订单ids查询
     * @author Will
     * @date: 2023/3/29 15:58
     * @param purchaseOrderIds
     * @return List<PurchaseOrderDetailEntity>
     */
    List<PurchaseOrderDetailEntity> listByPurchaseOrderIds(List<String> purchaseOrderIds);

    /**
     * @description: 根据订单ids删除明细
     * @author Will
     * @date: 2023/3/27 15:29
     * @param purchaseOrderIds

     */
    void removeByPurchaseOrderIds(List<String> purchaseOrderIds);


    /**
     * 根据明细id查询明细
     * @Author Luo_WG
     * @Date 2023/4/13 14:00
     * @param ids ids
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderDetailEntity>
     **/
    List<PurchaseOrderDetailEntity> listDetailByIds(List<String> ids);

    /**
     * 根据主表Id查询明细
     * @Author Luo_WG
     * @Date 2023/4/20 18:37
     * @param id id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderDetailEntity>
     **/
    List<PurchaseOrderDetailEntity> listPurchaseOrderDetailByOrderId(String id);

    /**
     * @description: 添加产品数据显示
     * @author Will
     * @date: 2023/4/14 10:25
     * @param dto
     * @return ViewProductDTO
     */
    List<PurchaseOrderDetailDTO.ViewProductDTO> viewProduct(PurchaseOrderDetailDTO.ProductSearchParamDTO dto);
    /**
     * @description: 更新订单到货状态
     * @author Will
     * @date: 2023/6/19 15:20
     * @param entity
     * @return Boolean
     */
    Boolean updatePoArrivalStatus(PurchaseOrderDetailEntity entity);
    /**
     * @description: 根据来源明细ids查询
     * @author Will
     * @date: 2023/6/19 15:48
     * @param sourceDetailIds
     * @return List<PurchaseOrderDetailEntity>
     */
    List<PurchaseOrderDetailEntity> listBySourceDetailIds(List<String> sourceDetailIds);

    /**
     * @description: 根据ids更新
     * @author zhangchunlin
     * @date: 2023/6/20 10:33
     * @param executionStatus
     * @param ids
     * @param remark
     *
     */
    void updateArrivalStatusByIds(String executionStatus, List<String> ids, List<PurchaseOrderDetailEntity> purchaseOrderDetailList, String remark);

    /**
     * 根据sku id集合获取最新的一个审核通过的采购订单明细，按采购日期倒序
     * @param skuIds
     * @return List<PurchaseOrderDetailEntity>
     */
    List<PurchaseOrderDetailEntity> getLatest(List<String> skuIds);

    /**
     * 根据sku id集合获取最新的一个审核通过的采购订单明细，按创建日期倒序
     * @param skuIds
     * @return List<PurchaseOrderDetailEntity>
     */
    List<PurchaseOrderDetailEntity> getLatestByCrtTime(List<String> skuIds);

    /**
     * 更新明细金蝶id
     * @Author Luo_WG
     * @Date 2023/7/12 10:22
     * @param list
     * @return void
     **/
    void updateKingdeeDetailId(JSONArray list);
    /**
     * @description: 更新备注
     * @author Will
     * @date: 2023/7/19 15:02
     * @param ids
     * @param remark
     */
    void updateRemarkByIds(List<String> ids, String remark);


    /**
     * 根据sku编号查询采购单id
     * @Author Luo_WG
     * @Date 2023/8/11 11:13
     * @param skuNo
     * @return java.util.List<java.lang.String>
     **/
    List<String> listPoIdBySkuNo(String skuNo);

    /**
     * @description: 更新执行状态
     * @author Will
     * @date: 2024/1/16 17:47
     * @param detailIdList
     * @param typeEnum
     * @param remark
     */
    void purchaseOrderConfirm(List<String> detailIdList, ExecutionStatusEnum typeEnum, String remark, ConfirmTypeEnum confirmType);
    /**
     * @description: 明细自动确认
     * @author Will
     * @date: 2024/1/17 14:09
     * @param detailIdList
     */
    void purchaseOrderAutoConfirm(List<String> detailIdList);
    /**
     * @description: 更新执行状态
     * @author Will
     * @date: 2024/1/27 15:47
     * @param mainIdList
     */
    void updateExecutionStatus(List<String> mainIdList,ExecutionStatusEnum statusEnum);
    /**
     * @description: 根据编码集合和sku编码集合查询
     * @author Will
     * @date: 2024/3/5 16:06
     * @param codeList
     * @param skuNoList
     * @return List<ImportEndReceiveDTO>
     */
    List<PurchaseOrderDetailDTO.ImportEndReceiveDTO> listImportEndReceive(List<String> codeList, List<String> skuNoList);

    /**
     * @description: 结束交货
     * @author Will
     * @date: 2023/3/16 11:35
     * @param ids
     * @param remark
     * @param isValid
     * @return Boolean
     */
    Boolean finishDelivery(List<String> ids, String remark,Boolean isValid);
    /**
     * 查询调整数据
     * @author will
     * @date 2025/7/29 14:09
     * @param adjustParamList
     * @return List<PurchaseOrderAdjustResultDTO>
     */
    List<PurchasePriceChangeDTO.PurchaseOrderAdjustResultDTO> listAdjustPurchaseOrder(List<PurchasePriceChangeDTO.PurchaseOrderAdjustParamDTO> adjustParamList);
}
