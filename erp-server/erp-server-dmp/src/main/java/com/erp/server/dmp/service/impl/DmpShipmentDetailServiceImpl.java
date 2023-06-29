package com.erp.server.dmp.service.impl;

import com.erp.model.dmp.entity.DmpShipmentDetailEntity;
import com.erp.server.dmp.mapper.DmpShipmentDetailMapper;
import com.erp.server.dmp.service.DmpShipmentDetailService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 调拨发货明细 服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@Slf4j
@Service
public class DmpShipmentDetailServiceImpl extends SuperServiceImpl<DmpShipmentDetailMapper, DmpShipmentDetailEntity> implements DmpShipmentDetailService {



}
