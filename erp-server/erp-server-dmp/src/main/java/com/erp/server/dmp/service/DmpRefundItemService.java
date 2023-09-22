package com.erp.server.dmp.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.DmpRefundItemEntity;

import java.util.List;

/**
 * 退款商品列表服务类
 */
public interface DmpRefundItemService extends IService<DmpRefundItemEntity> {
    /**
     * 添加退款商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpRefundItemEntity 退款列表信息
     * @return java.lang.Boolean
     **/
    Boolean add(DmpRefundItemEntity dmpRefundItemEntity, String platformSign);

    /**
     * 批量添加退款商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpRefundItemEntityList 退款列表信息
     * @return java.lang.Boolean
     **/
    Boolean batchAdd(List<DmpRefundItemEntity> dmpRefundItemEntityList, String platformSign);

    /**
     * 批量修改退款商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpRefundItemEntityList 退款列表信息
     * @return java.lang.Boolean
     **/
    Boolean batchUpdate(List<DmpRefundItemEntity> dmpRefundItemEntityList, String platformSign);

    /**
     * 根据退货订单表id删除退款商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:05
     * @param refundId 退货订单表id
     * @return java.lang.Boolean
     **/
    Boolean deleteRefundItemByRefundId(String refundId);

    /**
     * 处理退款订单详情数据
     * @param itemList
     */
    void checkOrderItem(List<DmpRefundItemEntity> itemList, String platformSign);

    /**
     * 采购订单sku拆分
     * @Author Luo_WG
     * @Date 2023/9/13 14:01
     * @param itemEntityList
     * @return java.util.List<com.erp.model.dmp.entity.DmpOrderItemEntity>
     **/
    List<DmpRefundItemEntity> splitOrderItem(List<DmpRefundItemEntity> itemEntityList, String platformSign);
}
