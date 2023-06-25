package com.erp.server.dmp.service.impl;

import com.erp.model.dmp.entity.DmpOutInStockDetailEntity;
import com.erp.server.dmp.mapper.DmpOutInStockDetailMapper;
import com.erp.server.dmp.service.DmpOutInStockDetailService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 手工出入库详情表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-25
 */
@Slf4j
@Service
public class DmpOutInStockDetailServiceImpl extends SuperServiceImpl<DmpOutInStockDetailMapper, DmpOutInStockDetailEntity> implements DmpOutInStockDetailService {



}
