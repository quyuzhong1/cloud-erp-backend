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
import com.erp.model.dmp.dto.DmpInputMongoDmpRelationDTO;
import com.erp.model.dmp.entity.DmpInputMongoDmpRelationEntity;
import com.erp.server.dmp.mapper.DmpInputMongoDmpRelationMapper;
import com.erp.server.dmp.service.DmpInputMongoDmpRelationService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * mongo与dmp关联表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-19
 */
@Slf4j
@Service
public class DmpInputMongoDmpRelationServiceImpl extends SuperServiceImpl<DmpInputMongoDmpRelationMapper, DmpInputMongoDmpRelationEntity> implements DmpInputMongoDmpRelationService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpInputMongoDmpRelationDTO.AddDTO addDTO) {
        DmpInputMongoDmpRelationEntity dmpInputMongoDmpRelationEntity = new DmpInputMongoDmpRelationEntity();
        BeanMapperUtils.copy(addDTO, dmpInputMongoDmpRelationEntity);

        // 数据处理
        handleData(dmpInputMongoDmpRelationEntity);

        log.info("开始新增mongo与dmp关联单");
        boolean save = super.save(dmpInputMongoDmpRelationEntity);
        if(!save) {
            throw new ServiceException("mongo与dmp关联单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "mongo与dmp关联单" , dmpInputMongoDmpRelationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpInputMongoDmpRelationEntity.getId(), dmpInputMongoDmpRelationEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpInputMongoDmpRelationDTO.UpdateDTO updateDTO) {
        DmpInputMongoDmpRelationEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "mongo与dmp关联单"));
        DmpInputMongoDmpRelationEntity dmpInputMongoDmpRelationEntity =  BeanMapperUtils.map(DmpInputMongoDmpRelationEntity.class, updateDTO);

        // 数据处理
        handleData(dmpInputMongoDmpRelationEntity);
        log.info("编辑 开始修改mongo与dmp关联单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpInputMongoDmpRelationEntity);
        if(!save) {
            throw new ServiceException("mongo与dmp关联单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录mongo与dmp关联单日志数据，id：【{}】", dmpInputMongoDmpRelationEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpInputMongoDmpRelationEntity.getId(), "mongo与dmp关联单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpInputMongoDmpRelationEntity dmpInputMongoDmpRelationEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
