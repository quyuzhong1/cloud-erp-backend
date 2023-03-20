package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.core.serveice.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.SupplierAccountDTO;
import com.erp.model.scm.entity.SupplierAccountEntity;
import com.erp.server.scm.mapper.SupplierAccountMapper;
import com.erp.server.scm.service.SupplierAccountService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 供应商结算信息 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class SupplierAccountServiceImpl extends SuperServiceImpl<SupplierAccountMapper, SupplierAccountEntity> implements SupplierAccountService {


    /**
     * 批量保存供应商账户信息
     *
     * @param supplierId
     * @param bankAccountList
     * @return void
     * @author yl
     * @date 2023-03-17 15:59
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatchBankAccount(String supplierId, List<SupplierAccountDTO.AddDTO> bankAccountList) {
        if (CollectionUtils.isEmpty(bankAccountList)) {
            return;
        }
        List<SupplierAccountEntity> addList = BeanMapper.copyList(bankAccountList, SupplierAccountEntity.class);
        addList.forEach(a -> a.setSupplierId(supplierId));
        this.saveBatch(addList);

    }

    /**
     * 根据供应商id 获取到账户信息
     *
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.SupplierAccountDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-20 10:12
     */
    @Override
    public List<SupplierAccountDTO.UpdateDTO> getBySupplierId(String supplierId) {
        List<SupplierAccountEntity> list = this.getList(supplierId);
        return BeanMapper.copyList(list, SupplierAccountDTO.UpdateDTO.class);
    }


    /**
     * 更改供应商账户信息
     *
     * @param bankAccountList
     * @param supplierId
     * @return void
     * @author yl
     * @date 2023-03-20 11:28
     */
    @Override
    public void updateAccount(List<SupplierAccountDTO.UpdateDTO> bankAccountList, String supplierId) {
        if (CollectionUtils.isEmpty(bankAccountList)) {
            return;
        }
        List<SupplierAccountEntity> saveOrUpdateList = BeanMapper.copyList(bankAccountList, SupplierAccountEntity.class);

        List<SupplierAccountEntity> dbList = this.getList(supplierId);
        //获取到删除的 账户id
        List<String> deleteIdList = getDeleteIds(bankAccountList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        this.saveOrUpdateBatch(saveOrUpdateList);

    }


    /**
     * 获取要删除的id 集合
     *
     * @param bankAccountList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-03-20 11:33
     */
    private List<String> getDeleteIds(List<SupplierAccountDTO.UpdateDTO> bankAccountList, List<SupplierAccountEntity> dbList) {
        List<String> ids = bankAccountList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SupplierAccountDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(SupplierAccountEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<SupplierAccountEntity> getList(String supplierId) {
        LambdaQueryWrapper<SupplierAccountEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SupplierAccountEntity::getSupplierId, supplierId);
        return this.list(queryWrapper);

    }
}
