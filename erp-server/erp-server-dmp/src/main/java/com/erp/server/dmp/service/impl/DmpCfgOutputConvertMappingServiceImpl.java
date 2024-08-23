package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpCfgOutputConvertMappingEntity;
import com.erp.server.dmp.mapper.DmpCfgOutputConvertMappingMapper;
import com.erp.server.dmp.service.DmpCfgOutputConvertMappingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpCfgOutputConvertMappingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 推送字段映射表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-15
 */
@Slf4j
@Service
public class DmpCfgOutputConvertMappingServiceImpl extends SuperServiceImpl<DmpCfgOutputConvertMappingMapper, DmpCfgOutputConvertMappingEntity> implements DmpCfgOutputConvertMappingService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgOutputConvertMappingDTO.AddDTO addDTO) {
        DmpCfgOutputConvertMappingEntity dmpCfgOutputConvertMappingEntity = new DmpCfgOutputConvertMappingEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgOutputConvertMappingEntity);

        // 数据处理
        handleData(dmpCfgOutputConvertMappingEntity);

        log.info("开始新增推送字段映射单");
        boolean save = super.save(dmpCfgOutputConvertMappingEntity);
        if(!save) {
            throw new ServiceException("推送字段映射单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "推送字段映射单" , dmpCfgOutputConvertMappingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpCfgOutputConvertMappingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgOutputConvertMappingEntity.getId(), dmpCfgOutputConvertMappingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgOutputConvertMappingDTO.UpdateDTO updateDTO) {
        DmpCfgOutputConvertMappingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "推送字段映射单"));
        DmpCfgOutputConvertMappingEntity dmpCfgOutputConvertMappingEntity =  BeanMapperUtils.map(DmpCfgOutputConvertMappingEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgOutputConvertMappingEntity);
        log.info("编辑 开始修改推送字段映射单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgOutputConvertMappingEntity);
        if(!save) {
            throw new ServiceException("推送字段映射单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录推送字段映射单日志数据，id：【{}】", dmpCfgOutputConvertMappingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgOutputConvertMappingEntity.getId(), "推送字段映射单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpCfgOutputConvertMappingEntity, null, dmpCfgOutputConvertMappingEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgOutputConvertMappingEntity dmpCfgOutputConvertMappingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
