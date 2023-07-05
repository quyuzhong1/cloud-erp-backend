package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpFbaDeliveryEntity;
import com.common.business.service.SuperService;


/**
 * <p>
 * FBA发货单 服务类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
public interface DmpFbaDeliveryService extends SuperService<DmpFbaDeliveryEntity> {


    /**
     * 校验FBA发货单在中台是否存在，存在就修改不存在则新增
     * @param dmpFbaDeliveryEntity
     * @return
     */
    void checkDelivery(DmpFbaDeliveryEntity dmpFbaDeliveryEntity);

    /**
     * 新增
     */
    Boolean add(DmpFbaDeliveryEntity entity);


}
