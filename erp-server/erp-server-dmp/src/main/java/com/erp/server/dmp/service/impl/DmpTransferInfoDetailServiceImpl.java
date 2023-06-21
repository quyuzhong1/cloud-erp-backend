package com.erp.server.dmp.service.impl;

import com.erp.model.dmp.entity.DmpTransferInfoDetailEntity;
import com.erp.server.dmp.mapper.DmpTransferInfoDetailMapper;
import com.erp.server.dmp.service.DmpTransferInfoDetailService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 直接调拨详情 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-19
 */
@Slf4j
@Service
public class DmpTransferInfoDetailServiceImpl extends SuperServiceImpl<DmpTransferInfoDetailMapper, DmpTransferInfoDetailEntity> implements DmpTransferInfoDetailService {



}
