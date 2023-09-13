package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.dmp.entity.BiTargetCategorySettingEntity;
import com.erp.server.dmp.mapper.BiTargetCategorySettingMapper;
import com.erp.server.dmp.service.BiTargetCategorySettingService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.BiTargetCategorySettingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 分类 目标设置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BiTargetCategorySettingServiceImpl extends SuperServiceImpl<BiTargetCategorySettingMapper, BiTargetCategorySettingEntity> implements BiTargetCategorySettingService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetCategorySettingDTO.AddDTO addDTO) {
        BiTargetCategorySettingEntity biTargetCategorySettingEntity = new BiTargetCategorySettingEntity();
        BeanMapperUtils.copy(addDTO, biTargetCategorySettingEntity);

        // 数据处理
        handleData(biTargetCategorySettingEntity);

        log.info("开始新增分类 目标设置单");
        boolean save = super.save(biTargetCategorySettingEntity);
        if(!save) {
            throw new ServiceException("分类 目标设置单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "分类 目标设置单" , biTargetCategorySettingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, biTargetCategorySettingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return biTargetCategorySettingEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BiTargetCategorySettingDTO.UpdateDTO updateDTO) {
        BiTargetCategorySettingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "分类 目标设置单"));
        BiTargetCategorySettingEntity biTargetCategorySettingEntity =  BeanMapperUtils.map(BiTargetCategorySettingEntity.class, updateDTO);

        // 数据处理
        handleData(biTargetCategorySettingEntity);
        log.info("编辑 开始修改分类 目标设置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(biTargetCategorySettingEntity);
        if(!save) {
            throw new ServiceException("分类 目标设置单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录分类 目标设置单日志数据，id：【{}】", biTargetCategorySettingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), biTargetCategorySettingEntity.getId(), "分类 目标设置单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, biTargetCategorySettingEntity, null, biTargetCategorySettingEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(BiTargetCategorySettingEntity biTargetCategorySettingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
