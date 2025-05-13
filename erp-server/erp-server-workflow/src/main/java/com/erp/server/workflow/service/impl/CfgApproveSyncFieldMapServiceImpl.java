package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.CfgApproveNoticeEntity;
import com.erp.model.workflow.entity.CfgApproveSyncFieldMapEntity;
import com.erp.server.workflow.mapper.CfgApproveSyncFieldMapMapper;
import com.erp.server.workflow.service.CfgApproveSyncFieldMapService;
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
import com.erp.model.workflow.dto.CfgApproveSyncFieldMapDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * ERP审批同步-推送信息配置 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
@Slf4j
@Service
public class CfgApproveSyncFieldMapServiceImpl extends SuperServiceImpl<CfgApproveSyncFieldMapMapper, CfgApproveSyncFieldMapEntity> implements CfgApproveSyncFieldMapService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgApproveSyncFieldMapDTO.AddDTO addDTO) {
        CfgApproveSyncFieldMapEntity cfgApproveSyncFieldMapEntity = new CfgApproveSyncFieldMapEntity();
        BeanMapperUtils.copy(addDTO, cfgApproveSyncFieldMapEntity);

        // 数据处理
        handleData(cfgApproveSyncFieldMapEntity);

        log.info("开始新增ERP审批同步-推送信息配置");
        boolean save = super.save(cfgApproveSyncFieldMapEntity);
        if(!save) {
            throw new ServiceException("ERP审批同步-推送信息配置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "ERP审批同步-推送信息配置" , cfgApproveSyncFieldMapEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgApproveSyncFieldMapEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgApproveSyncFieldMapEntity.getId(), cfgApproveSyncFieldMapEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgApproveSyncFieldMapDTO.UpdateDTO addOrUpdateDTO) {
        CfgApproveSyncFieldMapEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "ERP审批同步-推送信息配置"));
        CfgApproveSyncFieldMapEntity cfgApproveSyncFieldMapEntity =  BeanMapperUtils.map(CfgApproveSyncFieldMapEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgApproveSyncFieldMapEntity);
        log.info("编辑 开始修改ERP审批同步-推送信息配置数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgApproveSyncFieldMapEntity);
        if(!save) {
            throw new ServiceException("ERP审批同步-推送信息配置保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录ERP审批同步-推送信息配置日志数据，id：【{}】", cfgApproveSyncFieldMapEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgApproveSyncFieldMapEntity.getId(), "ERP审批同步-推送信息配置");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgApproveSyncFieldMapEntity, null, cfgApproveSyncFieldMapEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<CfgApproveSyncFieldMapEntity> listByMainIds(List<String> ids) {
        if(CollUtil.isNotEmpty(ids)){
            return lambdaQuery().in(CfgApproveSyncFieldMapEntity::getMainId, ids).list();
        }
        return Collections.emptyList();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(CfgApproveSyncFieldMapEntity cfgApproveSyncFieldMapEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
