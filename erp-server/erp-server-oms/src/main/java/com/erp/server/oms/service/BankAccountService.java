package com.erp.server.oms.service;
import com.erp.model.oms.entity.BankAccountEntity;
import com.common.business.service.SuperService;


/**
 * <p>
 * 银行账号 服务类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-07-04
 */
public interface BankAccountService extends SuperService<BankAccountEntity> {

    /**
     * 根据银行账号获取
     * @param bankAccountNo
     * @return
     */
    BankAccountEntity findByAccountNo(String bankAccountNo);

}
