package com.erp.server.plm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.PilotApplicationRefTaskEntity;
import com.erp.server.plm.mapper.PilotApplicationRefTaskMapper;
import com.erp.server.plm.service.PilotApplicationRefTaskService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.plm.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.PilotApplicationRefTaskDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 试产/量产 关联任务 服务实现类
 * </p>
 *
 * @author tmj
 * @since 2024-08-27
 */
@Slf4j
@Service
public class PilotApplicationRefTaskServiceImpl extends SuperServiceImpl<PilotApplicationRefTaskMapper, PilotApplicationRefTaskEntity> implements PilotApplicationRefTaskService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PilotApplicationRefTaskDTO.AddDTO addDTO) {
        PilotApplicationRefTaskEntity pilotApplicationRefTaskEntity = new PilotApplicationRefTaskEntity();
        BeanMapperUtils.copy(addDTO, pilotApplicationRefTaskEntity);

        log.info("开始新增试产/量产 关联任务");
        boolean save = super.save(pilotApplicationRefTaskEntity);
        if(!save) {
            throw new ServiceException("试产/量产 关联任务保存失败");
        }

        // 操作日志
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(pilotApplicationRefTaskEntity.getId(), pilotApplicationRefTaskEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PilotApplicationRefTaskDTO.UpdateDTO updateDTO) {
        PilotApplicationRefTaskEntity old = super.getById(updateDTO.getId());
        PilotApplicationRefTaskEntity oldEntity = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "试产/量产 关联任务"));
        PilotApplicationRefTaskEntity pilotApplicationRefTaskEntity =  BeanMapperUtils.map(PilotApplicationRefTaskEntity.class, updateDTO);

        log.info("编辑 开始修改试产/量产 关联任务数据，id：【{}】", oldEntity.getId());
        boolean save = super.updateById(pilotApplicationRefTaskEntity);
        if(!save) {
            throw new ServiceException("试产/量产 关联任务保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        return Boolean.TRUE;
    }

}
