package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.dmp.entity.BiTargetStaffSettingEntity;
import com.erp.server.dmp.mapper.BiTargetStaffSettingMapper;
import com.erp.server.dmp.service.BiTargetStaffSettingService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.BiTargetStaffSettingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 人员目标设置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BiTargetStaffSettingServiceImpl extends SuperServiceImpl<BiTargetStaffSettingMapper, BiTargetStaffSettingEntity> implements BiTargetStaffSettingService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetStaffSettingDTO.AddDTO addDTO) {
        BiTargetStaffSettingEntity biTargetStaffSettingEntity = new BiTargetStaffSettingEntity();
        BeanMapperUtils.copy(addDTO, biTargetStaffSettingEntity);

        // 数据处理
        handleData(biTargetStaffSettingEntity);

        log.info("开始新增人员目标设置单");
        boolean save = super.save(biTargetStaffSettingEntity);
        if(!save) {
            throw new ServiceException("人员目标设置单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "人员目标设置单" , biTargetStaffSettingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, biTargetStaffSettingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return biTargetStaffSettingEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BiTargetStaffSettingDTO.UpdateDTO updateDTO) {
        BiTargetStaffSettingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "人员目标设置单"));
        BiTargetStaffSettingEntity biTargetStaffSettingEntity =  BeanMapperUtils.map(BiTargetStaffSettingEntity.class, updateDTO);

        // 数据处理
        handleData(biTargetStaffSettingEntity);
        log.info("编辑 开始修改人员目标设置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(biTargetStaffSettingEntity);
        if(!save) {
            throw new ServiceException("人员目标设置单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录人员目标设置单日志数据，id：【{}】", biTargetStaffSettingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), biTargetStaffSettingEntity.getId(), "人员目标设置单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, biTargetStaffSettingEntity, null, biTargetStaffSettingEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(BiTargetStaffSettingEntity biTargetStaffSettingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
