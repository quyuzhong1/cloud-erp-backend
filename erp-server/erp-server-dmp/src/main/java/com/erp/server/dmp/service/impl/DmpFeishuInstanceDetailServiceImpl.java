package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpFeishuInstanceDetailEntity;
import com.erp.server.dmp.mapper.DmpFeishuInstanceDetailMapper;
import com.erp.server.dmp.service.DmpFeishuInstanceDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpFeishuInstanceDetailDTO;

import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * DMP飞书审批实例详情记录表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-10-30
 */
@Slf4j
@Service
public class DmpFeishuInstanceDetailServiceImpl extends SuperServiceImpl<DmpFeishuInstanceDetailMapper, DmpFeishuInstanceDetailEntity> implements DmpFeishuInstanceDetailService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpFeishuInstanceDetailDTO.AddDTO addDTO) {
        DmpFeishuInstanceDetailEntity dmpFeishuInstanceDetailEntity = new DmpFeishuInstanceDetailEntity();
        BeanMapperUtils.copy(addDTO, dmpFeishuInstanceDetailEntity);

        // 数据处理
        handleData(dmpFeishuInstanceDetailEntity);

        log.info("开始新增DMP飞书审批实例详情记录单");
        boolean save = super.save(dmpFeishuInstanceDetailEntity);
        if (!save) {
            throw new ServiceException("DMP飞书审批实例详情记录单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "DMP飞书审批实例详情记录单", dmpFeishuInstanceDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpFeishuInstanceDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpFeishuInstanceDetailEntity.getId(), dmpFeishuInstanceDetailEntity.getId());
    }

    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpFeishuInstanceDetailDTO.UpdateDTO addOrUpdateDTO) {
        DmpFeishuInstanceDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "DMP飞书审批实例详情记录单"));
        DmpFeishuInstanceDetailEntity dmpFeishuInstanceDetailEntity = BeanMapperUtils.map(DmpFeishuInstanceDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dmpFeishuInstanceDetailEntity);
        log.info("编辑 开始修改DMP飞书审批实例详情记录单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpFeishuInstanceDetailEntity);
        if (!save) {
            throw new ServiceException("DMP飞书审批实例详情记录单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录DMP飞书审批实例详情记录单日志数据，id：【{}】", dmpFeishuInstanceDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpFeishuInstanceDetailEntity.getId(), "DMP飞书审批实例详情记录单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpFeishuInstanceDetailEntity, null, dmpFeishuInstanceDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(DmpFeishuInstanceDetailEntity dmpFeishuInstanceDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
