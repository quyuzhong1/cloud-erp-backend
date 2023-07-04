package com.erp.server.oms.service.impl;

import com.erp.model.oms.entity.BankAccountEntity;
import com.erp.server.oms.mapper.BankAccountMapper;
import com.erp.server.oms.service.BankAccountService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
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

}
