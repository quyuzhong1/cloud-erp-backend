package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.DmpDeliveryDetailItemEntity;

import java.util.List;

/**
 * 发货详情商品信息
 */
public interface DmpDeliveryDetailItemService extends IService<DmpDeliveryDetailItemEntity> {
    /**
     * 添加发货详情商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpDeliveryDetailItemEntity 退款列表信息
     * @return java.lang.Boolean
     **/
    Boolean add(DmpDeliveryDetailItemEntity dmpDeliveryDetailItemEntity, String platformSign);

    /**
     * 批量添加发货详情商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpDeliveryDetailItemEntityList 发货详情商品信息
     * @return java.lang.Boolean
     **/
    Boolean batchAdd(List<DmpDeliveryDetailItemEntity> dmpDeliveryDetailItemEntityList, String platformSign);

    /**
     * 根据发货详情商品表id删除发货详情商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:05
     * @param deliveryDetailId 发货详情商品表id
     * @return java.lang.Boolean
     **/
    Boolean deleteDeliveryDetailItemByDetailId(String deliveryDetailId);

    /**
     * 拆分sku
     * @Author Luo_WG
     * @Date 2023/9/22 16:41
     * @param itemEntityList
     * @param platformSign
     * @return java.util.List<com.erp.model.dmp.entity.DmpDeliveryDetailItemEntity>
     **/
    List<DmpDeliveryDetailItemEntity> splitOrderItem(List<DmpDeliveryDetailItemEntity> itemEntityList, String platformSign);

    /**
     * 根据主表获取明细列表
     * @param mainId
     * @return
     */
    List<DmpDeliveryDetailItemEntity> getItemByMainId(String mainId);
}
