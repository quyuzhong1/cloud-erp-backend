package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.entity.DmpOrderItemSplitEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 订单商品信息拆分前表 服务类
 * </p>
 */
public interface DmpOrderItemService extends IService<DmpOrderItemEntity> {

    void initItem();

    /**
     * 添加订单商品详细信息
     *
     * @param dmpOrderInfoEntity 订单商品信息
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     **/
    Boolean add(DmpOrderItemEntity dmpOrderInfoEntity, String platformSign);

    /**
     * 批量添加订单商品详细信息
     *
     * @param dmpOrderInfoEntityList 订单商品信息集合
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     **/
    Boolean batchAdd(List<DmpOrderItemEntity> dmpOrderInfoEntityList, String platformSign);

    /**
     * 根据订单id删除订单详情
     * @param orderIds
     * @return
     */
    Boolean deleteByOrderIds(List<String> orderIds);
}
