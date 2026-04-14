package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.QcStandardSkuRefEntity;

import java.util.List;

/**
 * <p>
 * 质检标准关联SKU记录表 服务类
 * </p>
 *
 * @author zdy
 * @since 2026-03-26
 */
public interface QcStandardSkuRefService extends SuperService<QcStandardSkuRefEntity> {

    void updateDetail(List<QcStandardSkuRefEntity> skuRefEntityList, String id);

    List<QcStandardSkuRefEntity> listByMainId(String id);
}
