package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductPlanSaleEntity;
import com.erp.server.plm.mapper.ProductPlanSaleMapper;
import com.erp.server.plm.service.ProductPlanSaleService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:53
 */
@Service
public class ProductPlanSaleServiceImpl extends ServiceImpl<ProductPlanSaleMapper, ProductPlanSaleEntity>
        implements ProductPlanSaleService {
}
