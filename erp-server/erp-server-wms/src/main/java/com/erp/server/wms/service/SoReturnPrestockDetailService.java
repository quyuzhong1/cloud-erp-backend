package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.SoReturnPrestockDetailEntity;

import java.util.List;

/**
 * 预入库单详情 Service 接口
 *
 * @author auto
 * @since 2026-06-30
 */
public interface SoReturnPrestockDetailService extends SuperService<SoReturnPrestockDetailEntity> {

    /**
     * 根据主表 ID 查询详情行列表
     *
     * @param mainId 主表 ID
     * @return 详情行列表
     */
    List<SoReturnPrestockDetailEntity> listByMainId(String mainId);

    /**
     * 根据主表 ID 列表批量查询详情行
     *
     * @param mainIds 主表 ID 列表
     * @return 详情行列表
     */
    List<SoReturnPrestockDetailEntity> listByMainIds(List<String> mainIds);

    /**
     * 软删除指定主表下的所有详情行
     *
     * @param mainId 主表 ID
     */
    void deleteByMainId(String mainId);
}
