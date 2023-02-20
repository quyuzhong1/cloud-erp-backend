package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductStatusTimeEntity;
import com.erp.server.plm.mapper.ProductStatusTimeMapper;
import com.erp.server.plm.service.ProductStatusTimeService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:56
 */
@Service
public class ProductStatusTimeServiceImpl extends ServiceImpl<ProductStatusTimeMapper, ProductStatusTimeEntity>
        implements ProductStatusTimeService {
}
