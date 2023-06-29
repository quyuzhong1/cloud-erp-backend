package com.erp.server.dmp.service.impl;

import com.erp.model.dmp.entity.DmpFbaDeliveryEntity;
import com.erp.server.dmp.mapper.DmpFbaDeliveryMapper;
import com.erp.server.dmp.service.DmpFbaDeliveryService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * FBA发货单 服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@Slf4j
@Service
public class DmpFbaDeliveryServiceImpl extends SuperServiceImpl<DmpFbaDeliveryMapper, DmpFbaDeliveryEntity> implements DmpFbaDeliveryService {



}
