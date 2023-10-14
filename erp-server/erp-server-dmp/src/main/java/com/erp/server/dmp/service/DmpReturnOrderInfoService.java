package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;

import java.util.List;

/**
 * 退货订单服务类
 */
public interface DmpReturnOrderInfoService extends IService<DmpReturnOrderInfoEntity> {
    /**
     * 添加退货订单信息
     *
     * @param dmpOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     **/
    String add(DmpReturnOrderInfoEntity dmpOrderInfoEntity);

    /**
     * 根据平台订单id查询退货订单信息
     *
     * @param returnOrderInfoEntity
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     **/
    DmpReturnOrderInfoEntity getOrderByPlatformOrderId(DmpReturnOrderInfoEntity returnOrderInfoEntity);

    /**
     * 根据订单id查询退货订单信息
     *
     * @param platformOrderId
     * @return com.erp.model.dmp.entity.DmpReturnOrderInfoEntity
     * @Author Luo_WG
     * @Date 2022/12/14 19:10
     **/
    DmpReturnOrderInfoEntity getOrderByOrderId(String platformOrderId);

    /**
     * 根据平台订单id修改退货订单信息
     *
     * @param dmpReturnOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     **/
    Boolean updateOrderByPlatformOrderId(DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity);

    /**
     * 校验退货订单在中台是否存在，存在就修改不存在则新增
     *
     * @param returnOrderInfoEntity returnOrderInfoEntity
     * @return java.lang.String
     * @Author Luo_WG
     * @Date 2022/11/16 11:18
     **/
    String checkOrder(DmpReturnOrderInfoEntity returnOrderInfoEntity);

    /**
     * 清洗退货订单数据
     *
     * @Author Luo_WG
     * @Date 2022/12/14 19:15
     **/
    void cleanReturnOrderTask();

    /**
     * 根据平台编码删除
     *
     * @param codes
     */
    void removeReturnOrderByCode(List<String> codes);
}
