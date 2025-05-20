package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.ApproveSyncRecordEntity;
import com.erp.server.workflow.mapper.ApproveSyncRecordMapper;
import com.erp.server.workflow.service.ApproveSyncRecordService;
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
import com.erp.model.workflow.dto.ApproveSyncRecordDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * ERP审批同步-通知配置 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
@Slf4j
@Service
public class ApproveSyncRecordServiceImpl extends SuperServiceImpl<ApproveSyncRecordMapper, ApproveSyncRecordEntity> implements ApproveSyncRecordService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ApproveSyncRecordDTO.AddDTO addDTO) {
        ApproveSyncRecordEntity approveSyncRecordEntity = new ApproveSyncRecordEntity();
        BeanMapperUtils.copy(addDTO, approveSyncRecordEntity);

        // 数据处理
        handleData(approveSyncRecordEntity);

        log.info("开始新增ERP审批同步-通知配置");
        boolean save = super.save(approveSyncRecordEntity);
        if(!save) {
            throw new ServiceException("ERP审批同步-通知配置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "ERP审批同步-通知配置" , approveSyncRecordEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, approveSyncRecordEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(approveSyncRecordEntity.getId(), approveSyncRecordEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ApproveSyncRecordDTO.UpdateDTO addOrUpdateDTO) {
        ApproveSyncRecordEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "ERP审批同步-通知配置"));
        ApproveSyncRecordEntity approveSyncRecordEntity =  BeanMapperUtils.map(ApproveSyncRecordEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(approveSyncRecordEntity);
        log.info("编辑 开始修改ERP审批同步-通知配置数据，id：【{}】", old.getId());
        boolean save = super.updateById(approveSyncRecordEntity);
        if(!save) {
            throw new ServiceException("ERP审批同步-通知配置保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录ERP审批同步-通知配置日志数据，id：【{}】", approveSyncRecordEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), approveSyncRecordEntity.getId(), "ERP审批同步-通知配置");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, approveSyncRecordEntity, null, approveSyncRecordEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ApproveSyncRecordEntity approveSyncRecordEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
