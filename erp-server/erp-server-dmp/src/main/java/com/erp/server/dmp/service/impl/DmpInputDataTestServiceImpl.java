package com.erp.server.dmp.service.impl;


import org.springframework.stereotype.Service;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.DmpInputDataTestEntity;
import com.erp.server.dmp.mapper.DmpInputDataTestMapper;
import com.erp.server.dmp.service.DmpInputDataTestService;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-14
 */
@Slf4j
@Service
public class DmpInputDataTestServiceImpl extends SuperServiceImpl<DmpInputDataTestMapper, DmpInputDataTestEntity> implements DmpInputDataTestService {}
