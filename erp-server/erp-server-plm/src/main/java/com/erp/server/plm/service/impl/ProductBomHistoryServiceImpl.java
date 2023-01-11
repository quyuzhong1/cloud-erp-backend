package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductBomHistoryEntity;
import com.erp.server.plm.mapper.ProductBomHistoryMapper;
import com.erp.server.plm.service.ProductBomHistoryService;
import org.springframework.stereotype.Service;

/**
 * bom 历史表(ProductBomHistory)表服务实现类
 *
 * @author yl
 * @since 2023-01-11 14:04:50
 */
@Service()
public class ProductBomHistoryServiceImpl extends ServiceImpl<ProductBomHistoryMapper, ProductBomHistoryEntity> implements ProductBomHistoryService {

}
