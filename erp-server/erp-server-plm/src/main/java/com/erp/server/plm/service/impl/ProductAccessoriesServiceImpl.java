package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductAccessoriesEntity;
import com.erp.server.plm.mapper.ProductAccessoriesMapper;
import com.erp.server.plm.service.ProductAccessoriesService;
import org.springframework.stereotype.Service;

/**
 * 产品包装/辅料信息(ProductAccessories)表服务实现类
 *
 * @author yl
 * @since 2023-02-25 12:58:11
 */
@Service
public class ProductAccessoriesServiceImpl extends ServiceImpl<ProductAccessoriesMapper, ProductAccessoriesEntity> implements ProductAccessoriesService {

}
