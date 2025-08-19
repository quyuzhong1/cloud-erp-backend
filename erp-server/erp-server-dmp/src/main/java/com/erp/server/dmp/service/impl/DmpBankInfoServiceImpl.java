package com.erp.server.dmp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.DmpBankInfoEntity;
import com.erp.server.dmp.mapper.DmpBankInfoMapper;
import com.erp.server.dmp.service.DmpBankInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
/**
 * <p>
 * 银行信息表 服务实现类
 * </p>
 *
 * @author wuht
 * @since 2025-07-31
 */
@Slf4j
@Service
public class DmpBankInfoServiceImpl extends SuperServiceImpl<DmpBankInfoMapper, DmpBankInfoEntity> implements DmpBankInfoService {

}