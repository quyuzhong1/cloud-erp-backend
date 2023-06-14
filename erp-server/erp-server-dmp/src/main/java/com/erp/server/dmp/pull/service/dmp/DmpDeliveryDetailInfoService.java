package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.DmpDeliveryDetailInfoEntity;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;

/**
 * 出库详情
 */
public interface DmpDeliveryDetailInfoService extends IService<DmpDeliveryDetailInfoEntity> {
    /**
     * 添加发货详情信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpDeliveryDetailInfoEntity
     * @return java.lang.Boolean
     **/
    String add(DmpDeliveryDetailInfoEntity dmpDeliveryDetailInfoEntity);

    /**
     * 根据单据编号查询发货详情信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param dmpDeliveryDetailInfoEntity
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    DmpDeliveryDetailInfoEntity getDeliveryDetailByBillNo(DmpDeliveryDetailInfoEntity dmpDeliveryDetailInfoEntity);

    /**
     * 根据单据编号查询发货详情信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param orderNo
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    DmpDeliveryDetailInfoEntity getDeliveryDetailOrderNo(String orderNo);

    /**
     * 根据订单编号查询发货详情信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpDeliveryDetailInfoEntity
     * @return java.lang.Boolean
     **/
    Boolean updateDeliveryDetailByBillNo(DmpDeliveryDetailInfoEntity dmpDeliveryDetailInfoEntity);

    /**
     * 校验发货详情信息在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    String checkOrder(DmpDeliveryDetailInfoEntity dmpDeliveryDetailInfoEntity);

    /**
     * 根据平台订单编号查询发货详情信息
     * @param platformOrderId
     * @return
     */
    DmpDeliveryDetailInfoEntity getByPlatformOrderId(String platformOrderId);
}
