package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.SupplierAccountDTO;
import com.erp.model.scm.entity.SupplierAccountEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.SupplierAccountMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.SupplierAccountService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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


    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

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
        List<String> bankIdList = addList.stream().map(SupplierAccountEntity::getBankId).collect(Collectors.toList());
        List<BaseIdDTO> bankList = sysUserFeign.getBankList(bankIdList);
        for (SupplierAccountEntity item : addList) {
            String bankName = bankList.stream().filter(b -> b.getId().equals(item.getBankId())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setSupplierId(supplierId);
            item.setBankName(bankName);
        }
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
        //这是要添加的
        List<SupplierAccountDTO.UpdateDTO> addList = bankAccountList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        List<SupplierAccountEntity> saveOrUpdateList = BeanMapper.copyList(bankAccountList, SupplierAccountEntity.class);

        List<String> bankIdList = addList.stream().map(SupplierAccountDTO.UpdateDTO::getBankId).collect(Collectors.toList());
        List<BaseIdDTO> bankList = sysUserFeign.getBankList(bankIdList);
        for (SupplierAccountEntity item : saveOrUpdateList) {
            String bankName = bankList.stream().filter(b -> b.getId().equals(item.getBankId())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setSupplierId(supplierId);
            item.setBankName(bankName);
        }


        //这是要修改
        List<SupplierAccountEntity> updateList = saveOrUpdateList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());

        List<SupplierAccountEntity> dbList = this.getList(supplierId);
        //获取到删除的 账户id
        List<String> deleteIdList = getDeleteIds(bankAccountList, dbList);
        //这是要删除的
        List<SupplierAccountEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }


        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(supplierId, obj.getPayee())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("删除了一个账户【%s】", ModuleTypeEnum.SUPPLIER.getCode(), removePairList, "编辑操作");

        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(supplierId, obj.getPayee())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("添加了一个账户【%s】", ModuleTypeEnum.SUPPLIER.getCode(), addPairList, "编辑操作");

        //修改的
        for (SupplierAccountEntity update : updateList) {
            String id = update.getId();
            SupplierAccountEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                moduleOperateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.SUPPLIER.getCode(), supplierId, "", "");
            }
        }

        this.saveOrUpdateBatch(saveOrUpdateList);

    }


    /**
     * 根据供应商id 集合删除 账户信息
     *
     * @param ids
     * @return void
     * @author yl
     * @date 2023-03-20 18:51
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBySupplierIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        LambdaQueryWrapper<SupplierAccountEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SupplierAccountEntity::getSupplierId, ids);
        this.remove(queryWrapper);

    }


    /**
     * 转化 导入的数据
     *
     * @param supplierId
     * @param accountList
     * @return java.util.List<com.erp.model.scm.entity.SupplierAccountEntity>
     * @author yl
     * @date 2023-03-31 9:11
     */
    @Override
    public List<SupplierAccountEntity> transform(String supplierId, List<SupplierAccountDTO.ImportAddDTO> accountList) {
        if (CollectionUtils.isEmpty(accountList)) {
            return Collections.emptyList();
        }
        List<SupplierAccountEntity> addList = new ArrayList<>(accountList.size());
        for (SupplierAccountDTO.ImportAddDTO item : accountList) {
            SupplierAccountEntity account = new SupplierAccountEntity();
            BeanMapper.copy(item, account);
            account.setSupplierId(supplierId);
            addList.add(account);
        }
        return addList;
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
