package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.TransferDeclareDeadlineSettingEntity;
import com.erp.server.tms.mapper.TransferDeclareDeadlineSettingMapper;
import com.erp.server.tms.service.TransferDeclareDeadlineSettingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TransferDeclareDeadlineSettingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 截单设置 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-24
 */
@Slf4j
@Service
public class TransferDeclareDeadlineSettingServiceImpl extends SuperServiceImpl<TransferDeclareDeadlineSettingMapper, TransferDeclareDeadlineSettingEntity> implements TransferDeclareDeadlineSettingService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TransferDeclareDeadlineSettingDTO.AddDTO addDTO) {
        TransferDeclareDeadlineSettingEntity transferDeclareDeadlineSettingEntity = new TransferDeclareDeadlineSettingEntity();
        BeanMapperUtils.copy(addDTO, transferDeclareDeadlineSettingEntity);

        // 数据处理
        handleData(transferDeclareDeadlineSettingEntity);

        log.info("开始新增截单设置");
        boolean save = super.save(transferDeclareDeadlineSettingEntity);
        if(!save) {
            throw new ServiceException("截单设置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "截单设置" , transferDeclareDeadlineSettingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, transferDeclareDeadlineSettingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(transferDeclareDeadlineSettingEntity.getId(), transferDeclareDeadlineSettingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TransferDeclareDeadlineSettingDTO.UpdateDTO updateDTO) {
        TransferDeclareDeadlineSettingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "截单设置"));
        TransferDeclareDeadlineSettingEntity transferDeclareDeadlineSettingEntity =  BeanMapperUtils.map(TransferDeclareDeadlineSettingEntity.class, updateDTO);

        // 数据处理
        handleData(transferDeclareDeadlineSettingEntity);
        log.info("编辑 开始修改截单设置数据，id：【{}】", old.getId());
        boolean save = super.updateById(transferDeclareDeadlineSettingEntity);
        if(!save) {
            throw new ServiceException("截单设置保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录截单设置日志数据，id：【{}】", transferDeclareDeadlineSettingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), transferDeclareDeadlineSettingEntity.getId(), "截单设置");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, transferDeclareDeadlineSettingEntity, null, transferDeclareDeadlineSettingEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TransferDeclareDeadlineSettingEntity transferDeclareDeadlineSettingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
