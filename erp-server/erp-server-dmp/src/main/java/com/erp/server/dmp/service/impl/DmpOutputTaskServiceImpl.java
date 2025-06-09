package com.erp.server.dmp.service.impl;


import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpOutputTaskDTO;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.enums.DmpOutputTaskStatusEnum;
import com.erp.server.dmp.mapper.DmpOutputTaskMapper;
import com.erp.server.dmp.service.DmpOutputTaskService;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 推送任务 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-07-01
 */
@Slf4j
@Service
public class DmpOutputTaskServiceImpl extends SuperServiceImpl<DmpOutputTaskMapper, DmpOutputTaskEntity> implements DmpOutputTaskService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpOutputTaskDTO.AddDTO addDTO) {
        DmpOutputTaskEntity dmpOutputTaskEntity = new DmpOutputTaskEntity();
        BeanMapperUtils.copy(addDTO, dmpOutputTaskEntity);

        // 数据处理
        handleData(dmpOutputTaskEntity);

        log.info("开始新增推送任务");
        boolean save = super.save(dmpOutputTaskEntity);
        if(!save) {
            throw new ServiceException("推送任务保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "推送任务" , dmpOutputTaskEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpOutputTaskEntity.getId(), dmpOutputTaskEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpOutputTaskDTO.UpdateDTO updateDTO) {
        DmpOutputTaskEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "推送任务"));
        DmpOutputTaskEntity dmpOutputTaskEntity =  BeanMapperUtils.map(DmpOutputTaskEntity.class, updateDTO);

        // 数据处理
        handleData(dmpOutputTaskEntity);
        log.info("编辑 开始修改推送任务数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpOutputTaskEntity);
        if(!save) {
            throw new ServiceException("推送任务保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录推送任务日志数据，id：【{}】", dmpOutputTaskEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpOutputTaskEntity.getId(), "推送任务");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpOutputTaskEntity dmpOutputTaskEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Transactional(rollbackFor = Exception.class , propagation = Propagation.REQUIRES_NEW)
    public boolean updateErrorStatus(String id , boolean errorFlag , Integer errorCount , Exception e) {
    	String errorBeforeStatus = "";
    	if(errorFlag) {
    		errorBeforeStatus = getById(id).getStatus() + "@@";
    	}
    	return lambdaUpdate().eq(DmpOutputTaskEntity::getId, id)
				.set(DmpOutputTaskEntity::getErrorCount, errorCount)
				.set(errorFlag , DmpOutputTaskEntity::getStatus, DmpOutputTaskStatusEnum.ERROR.getCode())
				.set(DmpOutputTaskEntity::getUpdateTime, LocalDateTime.now())
				.set(DmpOutputTaskEntity::getErrorMessage,  errorBeforeStatus + "traceId=【" + MDC.get("traceId") + "】" + ExceptionUtil.stacktraceToString(e))
				.update();
	}
}
