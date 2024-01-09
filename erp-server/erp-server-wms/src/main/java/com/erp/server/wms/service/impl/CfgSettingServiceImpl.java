package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.CfgSettingEntity;
import com.erp.server.wms.mapper.CfgSettingMapper;
import com.erp.server.wms.service.CfgSettingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.CfgSettingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 系统配置管理 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-01-08
 */
@Slf4j
@Service
public class CfgSettingServiceImpl extends SuperServiceImpl<CfgSettingMapper, CfgSettingEntity> implements CfgSettingService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgSettingDTO.AddDTO addDTO) {
        CfgSettingEntity cfgSettingEntity = new CfgSettingEntity();
        BeanMapperUtils.copy(addDTO, cfgSettingEntity);

        // 数据处理
        handleData(cfgSettingEntity);

        log.info("开始新增系统配置管理");
        boolean save = super.save(cfgSettingEntity);
        if(!save) {
            throw new ServiceException("系统配置管理保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "系统配置管理" , cfgSettingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgSettingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgSettingEntity.getId(), cfgSettingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgSettingDTO.UpdateDTO updateDTO) {
        CfgSettingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "系统配置管理"));
        CfgSettingEntity cfgSettingEntity =  BeanMapperUtils.map(CfgSettingEntity.class, updateDTO);

        // 数据处理
        handleData(cfgSettingEntity);
        log.info("编辑 开始修改系统配置管理数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgSettingEntity);
        if(!save) {
            throw new ServiceException("系统配置管理保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录系统配置管理日志数据，id：【{}】", cfgSettingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), cfgSettingEntity.getId(), "系统配置管理");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgSettingEntity, null, cfgSettingEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgSettingEntity cfgSettingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
