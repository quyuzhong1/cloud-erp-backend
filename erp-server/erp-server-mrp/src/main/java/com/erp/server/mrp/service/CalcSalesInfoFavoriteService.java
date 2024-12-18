package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.entity.CalcSalesInfoFavoriteEntity;

import java.util.List;

/**
 * <p>
 * 试算关注表 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
public interface CalcSalesInfoFavoriteService extends SuperService<CalcSalesInfoFavoriteEntity> {


    /**
     * 获取当前登录用户关注的模板
     * @param uid 用户id
     */
    List<String> listByUserId(String uid);
}
