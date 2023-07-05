package com.erp.server.dmp.service.impl;

import com.erp.model.dmp.entity.DmpMachineDetailEntity;
import com.erp.server.dmp.mapper.DmpMachineDetailMapper;
import com.erp.server.dmp.service.DmpMachineDetailService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 加工单明细 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-25
 */
@Slf4j
@Service
public class DmpMachineDetailServiceImpl extends SuperServiceImpl<DmpMachineDetailMapper, DmpMachineDetailEntity> implements DmpMachineDetailService {



}
