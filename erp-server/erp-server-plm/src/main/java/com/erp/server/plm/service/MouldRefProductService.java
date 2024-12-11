package com.erp.server.plm.service;

import com.common.business.service.SuperService;
import com.erp.model.plm.entity.MouldRefProductEntity;

import java.util.List;

/**
 * <p>
 * 关联下单产品 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
public interface MouldRefProductService extends SuperService<MouldRefProductEntity> {


    /**
     * 通过明细id查询关联下单产品
     * @param detailIds 明细id
     */
    List<MouldRefProductEntity> listByMouldDetailIdList(List<String> detailIds);
}
