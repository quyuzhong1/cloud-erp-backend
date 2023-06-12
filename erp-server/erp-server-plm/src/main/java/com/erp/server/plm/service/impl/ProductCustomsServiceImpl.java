package com.erp.server.plm.service.impl;

import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.server.plm.mapper.ProductCustomsMapper;
import com.erp.server.plm.service.ProductCustomsService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-12
 */
@Slf4j
@Service
public class ProductCustomsServiceImpl extends SuperServiceImpl<ProductCustomsMapper, ProductCustomsEntity> implements ProductCustomsService {



}
