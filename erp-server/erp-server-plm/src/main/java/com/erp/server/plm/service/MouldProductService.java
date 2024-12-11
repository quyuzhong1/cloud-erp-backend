package com.erp.server.plm.service;

import com.common.business.service.SuperService;
import com.erp.model.plm.entity.MouldProductEntity;

import java.util.List;

/**
 * <p>
 * 模具 产品 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
public interface MouldProductService extends SuperService<MouldProductEntity> {


    /**
     * 根据模具id查询产品
     * @param detailIdList 模具id
     */
    List<MouldProductEntity> listByMouldDetailIdList(List<String> detailIdList);
}
