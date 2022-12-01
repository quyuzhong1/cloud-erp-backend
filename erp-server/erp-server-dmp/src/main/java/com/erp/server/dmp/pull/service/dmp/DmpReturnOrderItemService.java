package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.server.dmp.entity.dmp.DmpReturnOrderItemEntity;

import java.util.List;

/**
 * 中台订单退货服务类
 */
public interface DmpReturnOrderItemService extends IService<DmpReturnOrderItemEntity> {
    /**
     * 添加退货订单商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpReturnOrderItemEntity 订单商品信息
     * @return java.lang.Boolean
     **/
    Boolean add(DmpReturnOrderItemEntity dmpReturnOrderItemEntity);

    /**
     * 批量添加退货订单商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpOrderInfoEntityList 退货订单商品信息集合
     * @return java.lang.Boolean
     **/
    Boolean batchAdd(List<DmpReturnOrderItemEntity> dmpOrderInfoEntityList);

    /**
     * 根据退货订单表id查询退货订单商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:04
     * @param returnOrderId 退货订单表id
     * @return com.erp.server.dmp.entity.dmp.DmpReturnOrderItemEntity
     **/
    DmpReturnOrderItemEntity getOrderByReturnOrderId(String returnOrderId);

    /**
     * 根据退货订单表id删除退货订单商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:05
     * @param returnOrderId 退货订单表id
     * @return java.lang.Boolean
     **/
    Boolean deleteOrderByReturnOrderId(String returnOrderId);
}
