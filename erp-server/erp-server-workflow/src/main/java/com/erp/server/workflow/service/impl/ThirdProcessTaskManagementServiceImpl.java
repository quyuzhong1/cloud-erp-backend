package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.ThirdProcessTaskManagementEntity;
import com.erp.server.workflow.mapper.ThirdProcessTaskManagementMapper;
import com.erp.server.workflow.service.ThirdProcessTaskManagementService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.workflow.service.OperateLogService;
import com.erp.server.workflow.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ThirdProcessTaskManagementDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2025-05-23
 */
@Slf4j
@Service
public class ThirdProcessTaskManagementServiceImpl extends SuperServiceImpl<ThirdProcessTaskManagementMapper, ThirdProcessTaskManagementEntity> implements ThirdProcessTaskManagementService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ThirdProcessTaskManagementDTO.AddDTO addDTO) {
        ThirdProcessTaskManagementEntity thirdProcessTaskManagementEntity = new ThirdProcessTaskManagementEntity();
        BeanMapperUtils.copy(addDTO, thirdProcessTaskManagementEntity);

        // 数据处理
        handleData(thirdProcessTaskManagementEntity);

        log.info("开始新增");
        boolean save = super.save(thirdProcessTaskManagementEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , thirdProcessTaskManagementEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, thirdProcessTaskManagementEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(thirdProcessTaskManagementEntity.getId(), thirdProcessTaskManagementEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ThirdProcessTaskManagementDTO.UpdateDTO addOrUpdateDTO) {
        ThirdProcessTaskManagementEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        ThirdProcessTaskManagementEntity thirdProcessTaskManagementEntity =  BeanMapperUtils.map(ThirdProcessTaskManagementEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(thirdProcessTaskManagementEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(thirdProcessTaskManagementEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", thirdProcessTaskManagementEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), thirdProcessTaskManagementEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, thirdProcessTaskManagementEntity, null, thirdProcessTaskManagementEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ThirdProcessTaskManagementEntity thirdProcessTaskManagementEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
