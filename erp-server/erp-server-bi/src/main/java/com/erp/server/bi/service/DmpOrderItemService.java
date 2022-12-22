package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.dmp.entity.DmpOrderItemEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单商品服务类
 * @author Cloud
 */
public interface DmpOrderItemService extends IService<DmpOrderItemEntity> {

    /**
     * 根据sku和订单号计算订单详情中的销售额
     * @param orderIds
     * @param dto
     * @return
     */
    BigDecimal sumSales(List<String> orderIds, BiFilterDTO dto);

    /**
     * 根据sku和订单号统计销售量
     * @param orderIds
     * @param sku
     * @return
     */
    Integer countSalesVolume(List<String> orderIds, List<String> sku);

    /**
     * 根据sku和订单号统计订单数量
     * @param orderIds
     * @param sku
     * @return
     */
    Integer countOrderQuantityBySku(List<String> orderIds, List<String> sku);

    /**
     * @description: 根据订单表ids查询
     * @author Will
     * @date: 2022/12/19 11:44
     * @param orderInfoIds
     * @return List<DmpOrderItemEntity>
     */
    List<DmpOrderItemEntity> listByOrderInfoIds(List<String> orderInfoIds);

    /**
     *
     * @param orderIds
     * @return
     */
    List<DmpOrderItemEntity> listByConditions(List<String> orderIds, Integer newSign);
}
