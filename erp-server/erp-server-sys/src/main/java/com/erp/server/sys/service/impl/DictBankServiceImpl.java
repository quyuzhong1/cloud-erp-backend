package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.BankDTO;
import com.erp.model.sys.entity.DictBankEntity;
import com.erp.server.sys.mapper.DictBankMapper;
import com.erp.server.sys.service.DictBankService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 银行 字典表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Service
public class DictBankServiceImpl extends SuperServiceImpl<DictBankMapper, DictBankEntity> implements DictBankService {


    /**
     * 保存或者修改银行
     *
     * @param bankList
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-21 16:25
     */
    @Override
    public Boolean saveOrUpdateBatchBank(ValidList<BankDTO.AddOrUpdateDTO> bankList) {
        if (CollectionUtils.isNotEmpty(bankList)) {
            List<DictBankEntity> addList = BeanMapper.copyList(bankList, DictBankEntity.class);
            return this.saveOrUpdateBatch(addList);
        }
        return true;
    }


    /**
     * 获取银行列表
     *
     * @param
     * @return
     * @author yl
     * @date 2023-03-21 16:33
     */
    @Override
    public List<BankDTO.ViewDTO> getList() {
        LambdaQueryWrapper<DictBankEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(DictBankEntity::getId, DictBankEntity::getName,DictBankEntity::getDisabled,DictBankEntity::getBankNo);
        queryWrapper.eq(DictBankEntity::getDisabled,false);
        List<DictBankEntity> list = this.list(queryWrapper);
        return BeanMapper.copyList(list, BankDTO.ViewDTO.class);
    }

    @Override
    public List<BaseIdDTO> getByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return BeanMapper.copyList(this.list(), BaseIdDTO.class);
        }
        List<DictBankEntity> list = this.listByIds(ids);
        return BeanMapper.copyList(list, BaseIdDTO.class);
    }
}
