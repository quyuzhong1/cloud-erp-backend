package com.erp.server.sys.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.BankDTO;
import com.erp.model.sys.entity.DictBankEntity;
import com.erp.server.sys.mapper.DictBankMapper;
import com.erp.server.sys.service.DictBankService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        List<BankDTO.AddOrUpdateDTO> list = bankList.getList();
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_DICT_BANK_IS_EXIST);
        }
        List<String> bankNameList = list.stream().map(BankDTO.AddOrUpdateDTO::getName).distinct().collect(Collectors.toList());
        List<DictBankEntity> dictBankList = listByBankNameList(bankNameList);
        for (BankDTO.AddOrUpdateDTO bankDTO: list){
            //查询是否有传重复的名称保存
            long count = list.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), bankDTO.getName())).count();
            if (count > 1) {
                throw new ServiceException(ApiError.ERROR_DICT_BANK_IS_EXIST, bankDTO.getName());
            }

            //查询是否存在相同名称
            String oldName = dictBankList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), bankDTO.getName()) && !CharSequenceUtil.equals(obj.getId(),bankDTO.getId())).map(DictBankEntity::getName).findFirst().orElse("");
            if (CharSequenceUtil.isNotBlank(oldName)) {
                throw new ServiceException(ApiError.ERROR_DICT_BANK_IS_EXIST, oldName);
            }
        }

        List<DictBankEntity> addList = BeanMapper.copyList(bankList, DictBankEntity.class);
        return this.saveOrUpdateBatch(addList);
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

    /**
     * 根据银行名称查询
     * @author will
     * @date 2025/9/19 15:43
     * @param bankNameList
     * @return List<DictBankEntity>
     */
    private List<DictBankEntity> listByBankNameList(List<String> bankNameList) {
        LambdaQueryWrapper<DictBankEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DictBankEntity::getName, bankNameList);
        return this.list(queryWrapper);
    }
}
