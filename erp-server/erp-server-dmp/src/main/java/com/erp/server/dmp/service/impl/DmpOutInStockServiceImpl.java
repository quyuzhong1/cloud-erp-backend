package com.erp.server.dmp.service.impl;

import com.erp.model.dmp.entity.DmpOutInStockEntity;
import com.erp.server.dmp.mapper.DmpOutInStockMapper;
import com.erp.server.dmp.service.DmpOutInStockService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 手工出入库待同步数据表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-25
 */
@Slf4j
@Service
public class DmpOutInStockServiceImpl extends SuperServiceImpl<DmpOutInStockMapper, DmpOutInStockEntity> implements DmpOutInStockService {



}
