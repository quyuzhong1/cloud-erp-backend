package com.erp.server.plm.service.impl;/**
 * @author Lambda
 * @Classname LogisticsProductServiceImpl
 * @Description TODO
 * @Date 2023-11-06 12:28
 * @Created by yl
 */

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.service.LogisticsProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-11-06 12:28
 */
@Slf4j
@Service
public class LogisticsProductServiceImpl extends SuperServiceImpl<ProductDetailMapper, ProductDetailEntity>  implements LogisticsProductService {
}
