package com.erp.server.oms.service;
import com.erp.model.oms.entity.BankAccountEntity;
import com.common.business.service.SuperService;

import java.util.List;


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

    /**
     * 根据组织id获取银行账号
     * @param orgId
     * @return
     */
    List<BankAccountEntity> findByOrgId(String orgId);


    /**
     * 根据组织id和银行账号获取
     * @param orgId
     * @param bankAccountNo
     * @return
     */
    List<BankAccountEntity> findByOrgIdAndAccountNo(String orgId, String bankAccountNo);

}
