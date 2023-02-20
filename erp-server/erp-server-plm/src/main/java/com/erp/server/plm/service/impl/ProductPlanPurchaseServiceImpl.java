package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductPlanPurchaseEntity;
import com.erp.server.plm.mapper.ProductPlanPurchaseMapper;
import com.erp.server.plm.service.ProductPlanPurchaseService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:52
 */
@Service
public class ProductPlanPurchaseServiceImpl extends ServiceImpl<ProductPlanPurchaseMapper, ProductPlanPurchaseEntity>
        implements ProductPlanPurchaseService {
}
