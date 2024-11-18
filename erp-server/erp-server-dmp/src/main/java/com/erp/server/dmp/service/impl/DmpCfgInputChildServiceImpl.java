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
import com.erp.model.dmp.dto.DmpCfgInputChildDTO;
import com.erp.model.dmp.entity.DmpCfgInputChildEntity;
import com.erp.server.dmp.mapper.DmpCfgInputChildMapper;
import com.erp.server.dmp.service.DmpCfgInputChildService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 父子任务关系 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-21
 */
@Slf4j
@Service
public class DmpCfgInputChildServiceImpl extends SuperServiceImpl<DmpCfgInputChildMapper, DmpCfgInputChildEntity> implements DmpCfgInputChildService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgInputChildDTO.AddDTO addDTO) {
        DmpCfgInputChildEntity dmpCfgInputChildEntity = new DmpCfgInputChildEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgInputChildEntity);

        // 数据处理
        handleData(dmpCfgInputChildEntity);

        log.info("开始新增父子任务关系");
        boolean save = super.save(dmpCfgInputChildEntity);
        if(!save) {
            throw new ServiceException("父子任务关系保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "父子任务关系" , dmpCfgInputChildEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgInputChildEntity.getId(), dmpCfgInputChildEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgInputChildDTO.UpdateDTO updateDTO) {
        DmpCfgInputChildEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "父子任务关系"));
        DmpCfgInputChildEntity dmpCfgInputChildEntity =  BeanMapperUtils.map(DmpCfgInputChildEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgInputChildEntity);
        log.info("编辑 开始修改父子任务关系数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgInputChildEntity);
        if(!save) {
            throw new ServiceException("父子任务关系保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录父子任务关系日志数据，id：【{}】", dmpCfgInputChildEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgInputChildEntity.getId(), "父子任务关系");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgInputChildEntity dmpCfgInputChildEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
