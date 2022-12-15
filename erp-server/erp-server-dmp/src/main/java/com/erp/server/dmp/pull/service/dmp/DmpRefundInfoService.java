package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;

/**
 * 退款列表服务类
 */
public interface DmpRefundInfoService extends IService<DmpRefundInfoEntity> {
    /**
     * 添加退款列表信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpRefundInfoEntity 退款列表信息
     * @return java.lang.Boolean
     **/
    String add(DmpRefundInfoEntity dmpRefundInfoEntity);

    /**
     * 根据平台订单id查询退款信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param returnOrderInfoEntity
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    DmpRefundInfoEntity getRefundByPlatformOrderId(DmpRefundInfoEntity returnOrderInfoEntity);

    /**
     * 根据平台订单id修改退款信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpRefundInfoEntity 退款列表信息
     * @return java.lang.Boolean
     **/
    Boolean updateRefundByPlatformOrderId(DmpRefundInfoEntity dmpRefundInfoEntity);

    /**
     * 校验退款数据在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/16 11:18
     * @param returnOrderInfoEntity returnOrderInfoEntity
     * @return java.lang.String
     **/
    String checkOrder(DmpRefundInfoEntity returnOrderInfoEntity);

    /**
     * 清洗退款数据
     * @Author Luo_WG
     * @Date 2022/12/14 19:15
     **/
    void cleanRefundTask();
}
