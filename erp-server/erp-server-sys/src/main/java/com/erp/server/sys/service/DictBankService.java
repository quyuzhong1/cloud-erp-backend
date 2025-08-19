package com.erp.server.sys.service;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.erp.model.sys.dto.BankDTO;
import com.erp.model.sys.entity.DictBankEntity;

import java.util.List;

/**
 * <p>
 * 银行 字典表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
public interface DictBankService extends SuperService<DictBankEntity> {

    
    /**
     * 保存或者修改银行
     * @author yl
     * @date 2023-03-21 16:25
     * @param bankList
     * @return java.lang.Boolean
     */
    Boolean saveOrUpdateBatchBank(ValidList<BankDTO.AddOrUpdateDTO> bankList);

    
    /**
     * 获取银行列表
     * @author yl
     * @date 2023-03-21 16:33
     * @param
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<BankDTO.ViewDTO> getList();

    List<BaseIdDTO> getByIds(List<String> ids);
}
