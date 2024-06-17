package com.erp.server.dmp.service.impl;


import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpInputDataDmpRelationDTO;
import com.erp.model.dmp.entity.DmpInputDataDmpRelationEntity;
import com.erp.server.dmp.mapper.DmpInputDataDmpRelationMapper;
import com.erp.server.dmp.service.DmpInputDataDmpRelationService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * data表与dmp关联表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-17
 */
@Slf4j
@Service
public class DmpInputDataDmpRelationServiceImpl extends SuperServiceImpl<DmpInputDataDmpRelationMapper, DmpInputDataDmpRelationEntity> implements DmpInputDataDmpRelationService {
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpInputDataDmpRelationDTO.AddDTO addDTO) {
        DmpInputDataDmpRelationEntity dmpInputDataDmpRelationEntity = new DmpInputDataDmpRelationEntity();
        BeanMapperUtils.copy(addDTO, dmpInputDataDmpRelationEntity);

        // 数据处理
        handleData(dmpInputDataDmpRelationEntity);

        log.info("开始新增data表与dmp关联单");
        boolean save = super.save(dmpInputDataDmpRelationEntity);
        if(!save) {
            throw new ServiceException("data表与dmp关联单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "data表与dmp关联单" , dmpInputDataDmpRelationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpInputDataDmpRelationEntity.getId(), dmpInputDataDmpRelationEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpInputDataDmpRelationDTO.UpdateDTO updateDTO) {
        DmpInputDataDmpRelationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "data表与dmp关联单"));
        DmpInputDataDmpRelationEntity dmpInputDataDmpRelationEntity =  BeanMapperUtils.map(DmpInputDataDmpRelationEntity.class, updateDTO);

        // 数据处理
        handleData(dmpInputDataDmpRelationEntity);
        log.info("编辑 开始修改data表与dmp关联单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpInputDataDmpRelationEntity);
        if(!save) {
            throw new ServiceException("data表与dmp关联单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录data表与dmp关联单日志数据，id：【{}】", dmpInputDataDmpRelationEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpInputDataDmpRelationEntity.getId(), "data表与dmp关联单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpInputDataDmpRelationEntity dmpInputDataDmpRelationEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
