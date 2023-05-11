package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.entity.CustomerInfoEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface CustomerInfoService extends SuperService<CustomerInfoEntity> {

    /**
     * 获取到分组的id 集合
     * @author yl
     * @date 2023-05-11 18:10
     * @param
     * @return java.util.List<java.lang.String>
     */
    List<String> listGroup();
}
