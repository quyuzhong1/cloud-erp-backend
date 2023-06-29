package com.erp.server.dmp.service.impl;

import com.erp.model.dmp.entity.DmpShipmentEntity;
import com.erp.server.dmp.mapper.DmpShipmentMapper;
import com.erp.server.dmp.service.DmpShipmentService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * FBA调拨发货 服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@Slf4j
@Service
public class DmpShipmentServiceImpl extends SuperServiceImpl<DmpShipmentMapper, DmpShipmentEntity> implements DmpShipmentService {



}
