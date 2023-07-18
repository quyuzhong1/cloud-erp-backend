package com.erp.server.oms.service.impl;

import com.erp.model.oms.entity.BankAccountEntity;
import com.erp.server.oms.mapper.BankAccountMapper;
import com.erp.server.oms.service.BankAccountService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * <p>
 * 银行账号 服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-07-04
 */
@Slf4j
@Service
public class BankAccountServiceImpl extends SuperServiceImpl<BankAccountMapper, BankAccountEntity> implements BankAccountService {


    @Override
    public BankAccountEntity findByAccountNo(String bankAccountNo) {
        return lambdaQuery().eq(BankAccountEntity::getBankAccountNo, bankAccountNo).last("limit 1").one();
    }

    @Override
    public List<BankAccountEntity> findByOrgId(String orgId) {
        return lambdaQuery().eq(BankAccountEntity::getOrgId, orgId).list();
    }

    @Override
    public List<BankAccountEntity> findByOrgIdAndAccountNo(String orgId, String bankAccountNo) {
        return lambdaQuery().eq(BankAccountEntity::getOrgId, orgId).eq(BankAccountEntity::getBankAccountNo, bankAccountNo).list();
    }

}
