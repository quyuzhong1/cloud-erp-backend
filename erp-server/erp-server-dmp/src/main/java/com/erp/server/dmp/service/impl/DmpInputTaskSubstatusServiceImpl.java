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
import com.erp.model.dmp.dto.DmpInputTaskSubstatusDTO;
import com.erp.model.dmp.entity.DmpInputTaskSubstatusEntity;
import com.erp.server.dmp.mapper.DmpInputTaskSubstatusMapper;
import com.erp.server.dmp.service.DmpInputTaskSubstatusService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 拉取任务子状态 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpInputTaskSubstatusServiceImpl extends SuperServiceImpl<DmpInputTaskSubstatusMapper, DmpInputTaskSubstatusEntity> implements DmpInputTaskSubstatusService {
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpInputTaskSubstatusDTO.AddDTO addDTO) {
        DmpInputTaskSubstatusEntity dmpInputTaskSubstatusEntity = new DmpInputTaskSubstatusEntity();
        BeanMapperUtils.copy(addDTO, dmpInputTaskSubstatusEntity);

        // 数据处理
        handleData(dmpInputTaskSubstatusEntity);

        log.info("开始新增拉取任务子状态");
        boolean save = super.save(dmpInputTaskSubstatusEntity);
        if(!save) {
            throw new ServiceException("拉取任务子状态保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "拉取任务子状态" , dmpInputTaskSubstatusEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpInputTaskSubstatusEntity.getId(), dmpInputTaskSubstatusEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpInputTaskSubstatusDTO.UpdateDTO updateDTO) {
        DmpInputTaskSubstatusEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "拉取任务子状态"));
        DmpInputTaskSubstatusEntity dmpInputTaskSubstatusEntity =  BeanMapperUtils.map(DmpInputTaskSubstatusEntity.class, updateDTO);

        // 数据处理
        handleData(dmpInputTaskSubstatusEntity);
        log.info("编辑 开始修改拉取任务子状态数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpInputTaskSubstatusEntity);
        if(!save) {
            throw new ServiceException("拉取任务子状态保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录拉取任务子状态日志数据，id：【{}】", dmpInputTaskSubstatusEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpInputTaskSubstatusEntity.getId(), "拉取任务子状态");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpInputTaskSubstatusEntity dmpInputTaskSubstatusEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
