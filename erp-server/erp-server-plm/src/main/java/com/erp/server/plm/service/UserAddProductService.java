package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.UserAddProductEntity;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface UserAddProductService extends IService<UserAddProductEntity> {

    boolean userAddProduct(String productId);

    boolean userCancelProduct(String productId);

    List<Map<String, Object>> listByUserId();
}
