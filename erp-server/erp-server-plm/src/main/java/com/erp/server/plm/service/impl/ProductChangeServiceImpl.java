package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.erp.server.plm.mapper.ProductChangeMapper;
import com.erp.server.plm.service.ProductChangeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 变更信息表(ProductChange)表服务实现类
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@Service
public class ProductChangeServiceImpl extends ServiceImpl<ProductChangeMapper, ProductChangeEntity> implements ProductChangeService {

}
