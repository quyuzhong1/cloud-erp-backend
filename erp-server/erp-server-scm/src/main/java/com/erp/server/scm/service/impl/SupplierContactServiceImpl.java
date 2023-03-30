package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.SupplierContactDTO;
import com.erp.model.scm.entity.SupplierContactEntity;
import com.erp.server.scm.mapper.SupplierContactMapper;
import com.erp.server.scm.service.SupplierContactService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 供应商联系人表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class SupplierContactServiceImpl extends SuperServiceImpl<SupplierContactMapper, SupplierContactEntity> implements SupplierContactService {


    /**
     * 保存供应商联系信息
     *
     * @param supplierId
     * @param contactList
     * @return void
     * @author yl
     * @date 2023-03-17 16:07
     */
    @Override
    public void saveBatchContact(String supplierId, List<SupplierContactDTO.AddDTO> contactList) {
        if (CollectionUtils.isEmpty(contactList)) {
            return;
        }
        List<SupplierContactEntity> addList = BeanMapper.copyList(contactList, SupplierContactEntity.class);
        addList.forEach(c -> c.setSupplierId(supplierId));
        this.saveBatch(addList);

    }

    /**
     * 检查联系人 是否有多个默认人
     *
     * @param contactList
     * @return void
     * @author yl
     * @date 2023-03-17 16:39
     */

    @Override
    public void checkIsDefault(List<SupplierContactDTO.AddDTO> contactList) {
        long count = contactList.stream().filter(c -> c.getIsDefault()).count();
        if (count > 1) {
            throw new ServiceException(ApiError.ERROR_98003);
        }
    }


    /**
     * 根据供应商id 获取到联系人信息
     *
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.SupplierContactDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-20 10:07
     */
    @Override
    public List<SupplierContactDTO.UpdateDTO> getBySupplierId(String supplierId) {
        List<SupplierContactEntity> list = this.getList(supplierId);
        return BeanMapper.copyList(list, SupplierContactDTO.UpdateDTO.class);

    }


    /**
     * 修改供应商联系人信息
     *
     * @param contactList
     * @param supplierId  供应商id
     * @return void
     * @author yl
     * @date 2023-03-20 11:11
     */
    @Override
    public void updateSupplierContact(List<SupplierContactDTO.UpdateDTO> contactList, String supplierId) {
        if (CollectionUtils.isEmpty(contactList)) {
            return;
        }
        List<SupplierContactEntity> saveOrUpdateList = new ArrayList<>(contactList.size());
        //这是修改的
        List<SupplierContactDTO.UpdateDTO> updateList = contactList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是要添加的
        List<SupplierContactDTO.UpdateDTO> addList = contactList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        //这个是要修改的实体
        List<SupplierContactEntity> updateEntityList = BeanMapper.copyList(updateList, SupplierContactEntity.class);
        //这个是要添加的
        List<SupplierContactEntity> addEntityList = BeanMapper.copyList(addList, SupplierContactEntity.class);
        saveOrUpdateList.addAll(updateEntityList);
        saveOrUpdateList.addAll(addEntityList);
        List<SupplierContactEntity> dbList = this.getList(supplierId);
        List<String> deleteIdList = getDeleteIds(updateList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        this.saveOrUpdateBatch(saveOrUpdateList);


    }

    /**
     * 根据供应商id集合删除供应商联系 人
     *
     * @param supplierIds
     * @return void
     * @author yl
     * @date 2023-03-20 18:44
     */
    @Override
    public void removeBySupplierIds(List<String> supplierIds) {
        if (CollectionUtils.isEmpty(supplierIds)) {
            return;
        }
        LambdaQueryWrapper<SupplierContactEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SupplierContactEntity::getSupplierId, supplierIds);
        this.remove(queryWrapper);
    }


    /**
     * 获取到供应商默认联系人信息
     *
     * @param supplierIdList
     * @return java.util.List<com.erp.model.scm.entity.SupplierContactEntity>
     * @author yl
     * @date 2023-03-29 14:39
     */
    @Override
    public List<SupplierContactEntity> getDefaultBySupplierIdList(List<String> supplierIdList) {
        if (CollectionUtils.isEmpty(supplierIdList)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SupplierContactEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SupplierContactEntity::getSupplierId, supplierIdList);
        queryWrapper.eq(SupplierContactEntity::getIsDefault, true);
        return this.list(queryWrapper);
    }




    /**
     * 获取到要删除的集合
     *
     * @param
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-03-20 11:21
     */
    private List<String> getDeleteIds(List<SupplierContactDTO.UpdateDTO> contactList, List<SupplierContactEntity> dbList) {

        List<String> ids = contactList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SupplierContactDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(SupplierContactEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    /**
     * 根据供应商id 获取到对应数据
     *
     * @param
     * @return java.util.List<com.erp.model.scm.entity.SupplierContactEntity>
     * @author yl
     * @date 2023-03-20 10:08
     */
    private List<SupplierContactEntity> getList(String supplierId) {
        LambdaQueryWrapper<SupplierContactEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SupplierContactEntity::getSupplierId, supplierId);
        return this.list(queryWrapper);
    }
}
