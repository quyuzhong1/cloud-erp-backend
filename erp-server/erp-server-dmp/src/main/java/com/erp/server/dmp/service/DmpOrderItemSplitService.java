package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.dto.SplitSkuDTO;
import com.erp.model.dmp.entity.DmpBomEntity;
import com.erp.model.dmp.entity.DmpOrderItemSplitEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.NewProductDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 订单商品服务类
 */
public interface DmpOrderItemSplitService extends IService<DmpOrderItemSplitEntity> {
    /**
     * 添加订单商品详细信息
     *
     * @param dmpOrderInfoEntity 订单商品信息
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     **/
    Boolean add(DmpOrderItemSplitEntity dmpOrderInfoEntity, String platformSign);

    /**
     * 批量添加订单商品详细信息
     *
     * @param dmpOrderInfoEntityList 订单商品信息集合
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     **/
    Boolean batchAdd(List<DmpOrderItemSplitEntity> dmpOrderInfoEntityList, String platformSign);

    /**
     * 批量修改订单商品详细信息
     *
     * @param dmpOrderInfoEntityList 订单商品信息集合
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     **/
    Boolean batchUpdate(List<DmpOrderItemSplitEntity> dmpOrderInfoEntityList, String platformSign);

    /**
     * 根据erp平台商品id查询订单商品信息
     *
     * @param erpOrderItemId erp平台商品id
     * @return com.erp.model.dmp.entity.DmpOrderItemEntity
     * @Author Luo_WG
     * @Date 2022/11/14 22:11
     **/
    DmpOrderItemSplitEntity getByErpOrderItemId(String erpOrderItemId);

    /**
     * 根据订单表id查询订单商品信息
     *
     * @param orderId 订单表id
     * @return java.util.List<com.erp.model.dmp.entity.DmpOrderItemEntity>
     * @Author Luo_WG
     * @Date 2022/12/14 16:10
     **/
    List<DmpOrderItemSplitEntity> getByOrderId(String orderId);

    /**
     * 根据erp平台商品id修改订单商品信息
     *
     * @param dmpOrderItemSplitEntity 订单商品信息
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     **/
    Boolean updateOrderItemByErpOrderItemId(DmpOrderItemSplitEntity dmpOrderItemSplitEntity);


    /**
     * 同步PLM的到货时间更新新老品
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    void updateNewSign(Map<String, List<NewProductDTO>> dto);

    /**
     * 同步PLM的到货时间更新新老品
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    void getProductListing(Map<String, List<NewProductDTO>> dto);

    /**
     * sku拆分
     *
     * @param itemEntityList
     * @return java.util.List<com.erp.model.dmp.entity.DmpOrderItemEntity>
     * @Author Luo_WG
     * @Date 2023/9/13 14:01
     **/
    List<DmpOrderItemSplitEntity> splitOrderItem(List<DmpOrderItemSplitEntity> itemEntityList, String platformSign);

    /**
     * sku拆分
     *
     * @param splitSkuDTO
     * @param machining
     * @param allBomList
     * @return
     */
    List<SplitSkuDTO> splitSku(SplitSkuDTO splitSkuDTO, List<DmpBomEntity> machining, List<BomChildrenSkuDTO> allBomList);

    /**
     * 根据订单id删除订单详情
     * @param orderIds
     * @return
     */
    Boolean deleteByOrderIds(List<String> orderIds);

}