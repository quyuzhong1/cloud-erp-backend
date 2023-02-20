package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductPlanSaleInfoEntity;
import com.erp.server.plm.mapper.ProductPlanSaleInfoMapper;
import com.erp.server.plm.service.ProductPlanSaleInfoService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:54
 */
@Service
public class ProductPlanSaleInfoImpl extends ServiceImpl<ProductPlanSaleInfoMapper, ProductPlanSaleInfoEntity>
        implements ProductPlanSaleInfoService {
}
