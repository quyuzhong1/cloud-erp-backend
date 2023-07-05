package com.erp.server.dmp.service.impl;

import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.erp.server.dmp.mapper.DmpSkuCostMapper;
import com.erp.server.dmp.service.DmpSkuCostService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * sku bom关系表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
 */
@Slf4j
@Service
public class DmpSkuCostServiceImpl extends SuperServiceImpl<DmpSkuCostMapper, DmpSkuCostEntity> implements DmpSkuCostService {



}
