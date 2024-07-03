package com.erp.server.dmp.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.BiRefundItemEntity;

import java.util.List;

/**
 * 退款商品列表服务类
 */
public interface BiRefundItemService extends IService<BiRefundItemEntity> {
    /**
     * 添加退款商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param biRefundItemEntity 退款列表信息
     * @return java.lang.Boolean
     **/
    Boolean add(BiRefundItemEntity biRefundItemEntity, String platformSign);

    /**
     * 批量添加退款商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param biRefundItemEntityList 退款列表信息
     * @return java.lang.Boolean
     **/
    Boolean batchAdd(List<BiRefundItemEntity> biRefundItemEntityList, String platformSign);

    /**
     * 批量修改退款商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param biRefundItemEntityList 退款列表信息
     * @return java.lang.Boolean
     **/
    Boolean batchUpdate(List<BiRefundItemEntity> biRefundItemEntityList, String platformSign);

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
    void checkOrderItem(List<BiRefundItemEntity> itemList, String platformSign);

    /**
     * 采购订单sku拆分
     * @Author Luo_WG
     * @Date 2023/9/13 14:01
     * @param itemEntityList
     * @return java.util.List<com.erp.model.dmp.entity.DmpOrderItemEntity>
     **/
    List<BiRefundItemEntity> splitOrderItem(List<BiRefundItemEntity> itemEntityList, String platformSign);
}
