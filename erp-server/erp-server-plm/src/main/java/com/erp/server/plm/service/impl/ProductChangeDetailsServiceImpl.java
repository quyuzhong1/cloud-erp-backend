package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductChangeDetailsEntity;
import com.erp.server.plm.mapper.ProductChangeDetailsMapper;
import com.erp.server.plm.service.ProductChangeDetailsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 变更管理变更实体的信息表(ProductChangeDetails)表服务实现类
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@Service()
public class ProductChangeDetailsServiceImpl extends ServiceImpl<ProductChangeDetailsMapper, ProductChangeDetailsEntity> implements ProductChangeDetailsService {

}
