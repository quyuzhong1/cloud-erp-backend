package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.server.dmp.entity.dmp.DmpDeliveryDetailItemEntity;

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
    Boolean add(DmpDeliveryDetailItemEntity dmpDeliveryDetailItemEntity);

    /**
     * 批量添加发货详情商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpDeliveryDetailItemEntityList 发货详情商品信息
     * @return java.lang.Boolean
     **/
    Boolean batchAdd(List<DmpDeliveryDetailItemEntity> dmpDeliveryDetailItemEntityList);

    /**
     * 根据发货详情商品表id删除发货详情商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:05
     * @param deliveryDetailId 发货详情商品表id
     * @return java.lang.Boolean
     **/
    Boolean deleteDeliveryDetailItemByDetailId(String deliveryDetailId);
}
