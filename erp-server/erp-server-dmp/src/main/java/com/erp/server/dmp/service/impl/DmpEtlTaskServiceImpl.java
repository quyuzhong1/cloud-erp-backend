package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpEtlTaskEntity;
import com.erp.server.dmp.mapper.DmpEtlTaskMapper;
import com.erp.server.dmp.service.DmpEtlTaskService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpEtlTaskDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * etl任务 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-07-21
 */
@Slf4j
@Service
public class DmpEtlTaskServiceImpl extends SuperServiceImpl<DmpEtlTaskMapper, DmpEtlTaskEntity> implements DmpEtlTaskService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpEtlTaskDTO.AddDTO addDTO) {
        DmpEtlTaskEntity dmpEtlTaskEntity = new DmpEtlTaskEntity();
        BeanMapperUtils.copy(addDTO, dmpEtlTaskEntity);

        // 数据处理
        handleData(dmpEtlTaskEntity);

        log.info("开始新增etl任务");
        boolean save = super.save(dmpEtlTaskEntity);
        if(!save) {
            throw new ServiceException("etl任务保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "etl任务" , dmpEtlTaskEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpEtlTaskEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpEtlTaskEntity.getId(), dmpEtlTaskEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpEtlTaskDTO.UpdateDTO addOrUpdateDTO) {
        DmpEtlTaskEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "etl任务"));
        DmpEtlTaskEntity dmpEtlTaskEntity =  BeanMapperUtils.map(DmpEtlTaskEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dmpEtlTaskEntity);
        log.info("编辑 开始修改etl任务数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpEtlTaskEntity);
        if(!save) {
            throw new ServiceException("etl任务保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录etl任务日志数据，id：【{}】", dmpEtlTaskEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpEtlTaskEntity.getId(), "etl任务");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpEtlTaskEntity, null, dmpEtlTaskEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpEtlTaskEntity dmpEtlTaskEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
