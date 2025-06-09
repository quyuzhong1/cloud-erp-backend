package com.erp.server.scm.service.impl;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.dto.SupplierRefUserDTO;
import com.erp.model.scm.entity.SupplierRefUserEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.vo.SupplierRefUserVO;
import com.erp.server.scm.mapper.SupplierRefUserMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.SupplierRefUserService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static cn.hutool.core.text.CharSequenceUtil.format;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-01-05
 */
@Slf4j
@Service
public class SupplierRefUserServiceImpl extends SuperServiceImpl<SupplierRefUserMapper, SupplierRefUserEntity> implements SupplierRefUserService {
    @Autowired
    private ModuleOperateLogService moduleOperateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SupplierRefUserDTO.AddDTO addDTO) {
        SupplierRefUserEntity supplierRefUserEntity = new SupplierRefUserEntity();
        BeanMapperUtils.copy(addDTO, supplierRefUserEntity);

        // 数据处理
        handleData(supplierRefUserEntity);

        log.info("开始新增");
        boolean save = super.save(supplierRefUserEntity);
        if (!save) {
            throw new ServiceException("保存失败");
        }
        // 操作日志
        String msg = format("供应商协同用户【{}】新增【{}】关系id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "", supplierRefUserEntity.getId());
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUPPLIER_REF_USER.getCode(), supplierRefUserEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(supplierRefUserEntity.getId(), supplierRefUserEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SupplierRefUserDTO.UpdateDTO updateDTO) {
        SupplierRefUserEntity old = super.getById(updateDTO.getId());
        SupplierRefUserEntity oldEntity = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        SupplierRefUserEntity supplierRefUserEntity = BeanMapperUtils.map(SupplierRefUserEntity.class, updateDTO);

        // 数据处理
        handleData(supplierRefUserEntity);
        log.info("编辑 开始修改数据，id：【{}】", oldEntity.getId());
        boolean save = super.updateById(supplierRefUserEntity);
        if (!save) {
            throw new ServiceException("保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录日志数据，id：【{}】", supplierRefUserEntity.getId());
        String msg = format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), supplierRefUserEntity.getId(), "");
        moduleOperateLogService.addModuleOperateLogByObj(old, supplierRefUserEntity, ModuleTypeEnum.SUPPLIER_REF_USER.getCode(), supplierRefUserEntity.getId(), "", msg);
        return Boolean.TRUE;
    }

    @Override
    public List<SupplierRefUserVO> getUserIdsBySupplierIds(List<String> supplierIds, Boolean isSuper) {
        return baseMapper.getUserIdsBySupplierIds(supplierIds, isSuper);
    }

    @Override
    public SupplierRefUserEntity getSupplierRelUserByUid(String uid) {
        List<SupplierRefUserEntity> list = lambdaQuery().eq(SupplierRefUserEntity::getUid, uid).eq(SupplierRefUserEntity::getIsDeleted, false).list();
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void deleteRefByUids(List<String> uids) {
        if (CollectionUtils.isNotEmpty(uids)){
            lambdaUpdate().in(SupplierRefUserEntity::getUid,uids)
                    .set(SupplierRefUserEntity::getIsDeleted,true)
                    .update();
        }
    }

    @Override
    public List<SupplierRefUserVO> getSupplierRefByUids(List<String> uids) {
        if (CollectionUtils.isEmpty(uids)) return Collections.emptyList();
        return baseMapper.getSupplierRefByUids(uids);
    }

    @Override
    public List<SupplierRefUserVO> getSupplierRefBySupplierIds(List<String> supplierIds) {
        if (CollectionUtils.isEmpty(supplierIds)) return Collections.emptyList();
        return baseMapper.getSupplierRefBySupplierIds(supplierIds);
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(SupplierRefUserEntity supplierRefUserEntity) {
        if(Objects.isNull(supplierRefUserEntity)) throw new ServiceException("供应商关系不能为空");
        if(StringUtils.isEmpty(supplierRefUserEntity.getUid())) throw new ServiceException("用户ID不能为空");
        if(StringUtils.isEmpty(supplierRefUserEntity.getSupplierId())) throw new ServiceException("供应商ID不能为空");
        //一个用户只能存在一个供应商授权
        int count = lambdaQuery()
                .eq(SupplierRefUserEntity::getSupplierId, supplierRefUserEntity.getSupplierId())
                .eq(SupplierRefUserEntity::getUid, supplierRefUserEntity.getUid())
                .eq(SupplierRefUserEntity::getIsDeleted, false)
                .ne(StringUtils.isNotEmpty(supplierRefUserEntity.getId()), SupplierRefUserEntity::getId, supplierRefUserEntity.getId())
                .count();
        Assert.isTrue(count < 1, "用户供应商关系已存在！");
    }
}
