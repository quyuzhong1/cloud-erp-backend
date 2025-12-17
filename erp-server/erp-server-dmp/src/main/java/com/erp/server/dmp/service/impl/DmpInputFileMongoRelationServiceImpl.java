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
import com.erp.model.dmp.dto.DmpInputFileMongoRelationDTO;
import com.erp.model.dmp.entity.DmpInputFileMongoRelationEntity;
import com.erp.server.dmp.mapper.DmpInputFileMongoRelationMapper;
import com.erp.server.dmp.service.DmpInputFileMongoRelationService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * file与mongo关联表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-19
 */
@Slf4j
@Service
public class DmpInputFileMongoRelationServiceImpl extends SuperServiceImpl<DmpInputFileMongoRelationMapper, DmpInputFileMongoRelationEntity> implements DmpInputFileMongoRelationService {

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpInputFileMongoRelationDTO.AddDTO addDTO) {
        DmpInputFileMongoRelationEntity dmpInputFileMongoRelationEntity = new DmpInputFileMongoRelationEntity();
        BeanMapperUtils.copy(addDTO, dmpInputFileMongoRelationEntity);

        // 数据处理
        handleData(dmpInputFileMongoRelationEntity);

        log.info("开始新增file与mongo关联单");
        boolean save = super.save(dmpInputFileMongoRelationEntity);
        if(!save) {
            throw new ServiceException("file与mongo关联单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "file与mongo关联单" , dmpInputFileMongoRelationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpInputFileMongoRelationEntity.getId(), dmpInputFileMongoRelationEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpInputFileMongoRelationDTO.UpdateDTO updateDTO) {
        DmpInputFileMongoRelationEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "file与mongo关联单"));
        DmpInputFileMongoRelationEntity dmpInputFileMongoRelationEntity =  BeanMapperUtils.map(DmpInputFileMongoRelationEntity.class, updateDTO);

        // 数据处理
        handleData(dmpInputFileMongoRelationEntity);
        log.info("编辑 开始修改file与mongo关联单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpInputFileMongoRelationEntity);
        if(!save) {
            throw new ServiceException("file与mongo关联单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录file与mongo关联单日志数据，id：【{}】", dmpInputFileMongoRelationEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpInputFileMongoRelationEntity.getId(), "file与mongo关联单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpInputFileMongoRelationEntity dmpInputFileMongoRelationEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
