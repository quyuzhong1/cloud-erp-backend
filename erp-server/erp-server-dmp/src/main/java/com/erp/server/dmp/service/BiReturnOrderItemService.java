package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.BiReturnOrderItemEntity;

import java.util.List;

/**
 * 中台订单退货服务类
 */
public interface BiReturnOrderItemService extends IService<BiReturnOrderItemEntity> {
    /**
     * 添加退货订单商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param biReturnOrderItemEntity 订单商品信息
     * @return java.lang.Boolean
     **/
    Boolean add(BiReturnOrderItemEntity biReturnOrderItemEntity, String platformSign);

    /**
     * 批量添加退货订单商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpOrderInfoEntityList 退货订单商品信息集合
     * @return java.lang.Boolean
     **/
    Boolean batchAdd(List<BiReturnOrderItemEntity> dmpOrderInfoEntityList, String platformSign);

    /**
     * 批量修改退货订单商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpOrderInfoEntityList 退货订单商品信息集合
     * @return java.lang.Boolean
     **/
    Boolean batchUpdate(List<BiReturnOrderItemEntity> dmpOrderInfoEntityList, String platformSign);

    /**
     * 根据退货订单表id查询退货订单商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:04
     * @param returnOrderId 退货订单表id
     * @return com.erp.model.dmp.entity.DmpReturnOrderItemEntity
     **/
    BiReturnOrderItemEntity getOrderByReturnOrderId(String returnOrderId);

    /**
     * 根据退货订单表id删除退货订单商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:05
     * @param returnOrderId 退货订单表id
     * @return java.lang.Boolean
     **/
    Boolean deleteOrderByReturnOrderId(String returnOrderId);

    /**
     * 处理退货详情数据
     *
     * @param itemList
     */
    void checkOrderItem(List<BiReturnOrderItemEntity> itemList, String platformSign);

    /**
     * 采购订单sku拆分
     * @Author Luo_WG
     * @Date 2023/9/13 14:01
     * @param itemEntityList
     * @return java.util.List<com.erp.model.dmp.entity.DmpOrderItemEntity>
     **/
    List<BiReturnOrderItemEntity> splitOrderItem(List<BiReturnOrderItemEntity> itemEntityList, String platformSign);

    List<BiReturnOrderItemEntity> getItemByMainId(String mainId);
}
