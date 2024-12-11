package com.erp.server.plm.service;

import com.common.business.service.SuperService;
import com.erp.model.plm.entity.MouldStoreLocationEntity;

import java.util.List;

/**
 * <p>
 * 模具存放位置 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
public interface MouldStoreLocationService extends SuperService<MouldStoreLocationEntity> {


    /**
     * 根据明细id查询存放位置
     * @param detailIdList 明细id
     */
    List<MouldStoreLocationEntity> listByMouldDetailIdList(List<String> detailIdList);
    /**
     * 根据明细id查询存放位置
     * @param detailId 明细id
     */
    MouldStoreLocationEntity getByMouldDetailId(String detailId);
}
