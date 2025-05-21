package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.CfgApproveNoticeEntity;
import com.erp.server.workflow.mapper.CfgApproveNoticeMapper;
import com.erp.server.workflow.service.CfgApproveNoticeService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.workflow.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.CfgApproveNoticeDTO;
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
public class CfgApproveNoticeServiceImpl extends SuperServiceImpl<CfgApproveNoticeMapper, CfgApproveNoticeEntity> implements CfgApproveNoticeService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgApproveNoticeDTO.AddDTO addDTO) {
        CfgApproveNoticeEntity cfgApproveNoticeEntity = new CfgApproveNoticeEntity();
        BeanMapperUtils.copy(addDTO, cfgApproveNoticeEntity);

        // 数据处理
        handleData(cfgApproveNoticeEntity);

        log.info("开始新增ERP审批同步-通知配置");
        boolean save = super.save(cfgApproveNoticeEntity);
        if(!save) {
            throw new ServiceException("ERP审批同步-通知配置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "ERP审批同步-通知配置" , cfgApproveNoticeEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgApproveNoticeEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgApproveNoticeEntity.getId(), cfgApproveNoticeEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgApproveNoticeDTO.UpdateDTO addOrUpdateDTO) {
        CfgApproveNoticeEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "ERP审批同步-通知配置"));
        CfgApproveNoticeEntity cfgApproveNoticeEntity =  BeanMapperUtils.map(CfgApproveNoticeEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgApproveNoticeEntity);
        log.info("编辑 开始修改ERP审批同步-通知配置数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgApproveNoticeEntity);
        if(!save) {
            throw new ServiceException("ERP审批同步-通知配置保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录ERP审批同步-通知配置日志数据，id：【{}】", cfgApproveNoticeEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgApproveNoticeEntity.getId(), "ERP审批同步-通知配置");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgApproveNoticeEntity, null, cfgApproveNoticeEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<CfgApproveNoticeEntity> listByMainIds(List<String> mainIds) {
        if(CollUtil.isNotEmpty(mainIds)){
            return lambdaQuery().in(CfgApproveNoticeEntity::getMainId, mainIds).list();
        }
        return Collections.emptyList();
    }


    @Override
    public CfgApproveNoticeEntity getByNoticeTypeAndMainId(String mainId, String noticeType, Boolean enableStatus) {
        if(StringUtils.isNotBlank(mainId) && StringUtils.isNotBlank(noticeType)){
            return lambdaQuery().eq(CfgApproveNoticeEntity::getMainId, mainId)
                    .eq(CfgApproveNoticeEntity::getNoticeType, noticeType)
                    .eq(CfgApproveNoticeEntity::getEnableStatus, enableStatus)
                    .last(" limit 1 ")
                    .one();
        }
        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgApproveNoticeEntity cfgApproveNoticeEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
