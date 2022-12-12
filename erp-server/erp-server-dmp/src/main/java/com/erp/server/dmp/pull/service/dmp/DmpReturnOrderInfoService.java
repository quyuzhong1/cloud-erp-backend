package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.server.dmp.entity.dmp.DmpReturnOrderInfoEntity;

/**
 * 退货订单服务类
 */
public interface DmpReturnOrderInfoService extends IService<DmpReturnOrderInfoEntity> {
    /**
     * 添加退货订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     **/
    String add(DmpReturnOrderInfoEntity dmpOrderInfoEntity);

    /**
     * 根据平台订单id查询退货订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param returnOrderInfoEntity
     * @return com.erp.server.dmp.entity.dmp.DmpOrderInfoEntity
     **/
    DmpReturnOrderInfoEntity getOrderByPlatformOrderId(DmpReturnOrderInfoEntity returnOrderInfoEntity);

    /**
     * 根据平台订单id修改退货订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpReturnOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     **/
    Boolean updateOrderByPlatformOrderId(DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity);

    /**
     * 校验退货订单在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/16 11:18
     * @param returnOrderInfoEntity returnOrderInfoEntity
     * @return java.lang.String
     **/
    String checkOrder(DmpReturnOrderInfoEntity returnOrderInfoEntity);
}
