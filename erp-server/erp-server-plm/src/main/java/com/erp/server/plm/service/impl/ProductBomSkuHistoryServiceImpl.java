package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductBomSkuHistoryEntity;
import com.erp.server.plm.mapper.ProductBomSkuHistoryMapper;
import com.erp.server.plm.service.ProductBomSkuHistoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * bom历史表与sku关系表(ProductBomSkuHistory)表服务实现类
 *
 * @author yl
 * @since 2023-01-11 14:04:52
 */
@Service
public class ProductBomSkuHistoryServiceImpl extends ServiceImpl<ProductBomSkuHistoryMapper, ProductBomSkuHistoryEntity> implements ProductBomSkuHistoryService {

}
