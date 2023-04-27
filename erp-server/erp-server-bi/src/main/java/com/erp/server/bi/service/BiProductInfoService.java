package com.erp.server.bi.service;

import com.common.business.service.SuperService;
import com.erp.model.bi.entity.BiProductInfoEntity;

import java.util.List;

/**
 * <p>
 * 产品信息表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-21
 */
public interface BiProductInfoService extends SuperService<BiProductInfoEntity> {

    /**
     * 获取品牌信息
     * @param brandList
     * @return
     */
    List<BiProductInfoEntity> getbrandList(List<String> brandList);
}
