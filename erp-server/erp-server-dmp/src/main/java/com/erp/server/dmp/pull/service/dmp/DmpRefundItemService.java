package com.erp.server.dmp.pull.service.dmp;


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
    Boolean add(DmpRefundItemEntity dmpRefundItemEntity);

    /**
     * 批量添加退款商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpRefundItemEntityList 退款列表信息
     * @return java.lang.Boolean
     **/
    Boolean batchAdd(List<DmpRefundItemEntity> dmpRefundItemEntityList);

    /**
     * 根据退货订单表id删除退款商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:05
     * @param refundId 退货订单表id
     * @return java.lang.Boolean
     **/
    Boolean deleteRefundItemByRefundId(String refundId);
}
