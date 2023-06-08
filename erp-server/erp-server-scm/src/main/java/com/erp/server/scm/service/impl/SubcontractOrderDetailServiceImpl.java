package com.erp.server.scm.service.impl;

import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.server.scm.mapper.SubcontractOrderDetailMapper;
import com.erp.server.scm.service.SubcontractOrderDetailService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 委外订单明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@Slf4j
@Service
public class SubcontractOrderDetailServiceImpl extends SuperServiceImpl<SubcontractOrderDetailMapper, SubcontractOrderDetailEntity> implements SubcontractOrderDetailService {



}
