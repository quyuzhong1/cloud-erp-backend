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
import com.erp.model.dmp.dto.DmpCfgMqDTO;
import com.erp.model.dmp.entity.DmpCfgMqEntity;
import com.erp.server.dmp.mapper.DmpCfgMqMapper;
import com.erp.server.dmp.service.DmpCfgMqService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 输入输出mq信息 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpCfgMqServiceImpl extends SuperServiceImpl<DmpCfgMqMapper, DmpCfgMqEntity> implements DmpCfgMqService {
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgMqDTO.AddDTO addDTO) {
        DmpCfgMqEntity dmpCfgMqEntity = new DmpCfgMqEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgMqEntity);

        // 数据处理
        handleData(dmpCfgMqEntity);

        log.info("开始新增输入输出mq信息");
        boolean save = super.save(dmpCfgMqEntity);
        if(!save) {
            throw new ServiceException("输入输出mq信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "输入输出mq信息" , dmpCfgMqEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgMqEntity.getId(), dmpCfgMqEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgMqDTO.UpdateDTO updateDTO) {
        DmpCfgMqEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "输入输出mq信息"));
        DmpCfgMqEntity dmpCfgMqEntity =  BeanMapperUtils.map(DmpCfgMqEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgMqEntity);
        log.info("编辑 开始修改输入输出mq信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgMqEntity);
        if(!save) {
            throw new ServiceException("输入输出mq信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录输入输出mq信息日志数据，id：【{}】", dmpCfgMqEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgMqEntity.getId(), "输入输出mq信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgMqEntity dmpCfgMqEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
