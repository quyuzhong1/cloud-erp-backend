package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.dmp.entity.BiTargetShopSettingEntity;
import com.erp.server.dmp.mapper.BiTargetShopSettingMapper;
import com.erp.server.dmp.service.BiTargetShopSettingService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.BiTargetShopSettingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 店铺目标设置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BiTargetShopSettingServiceImpl extends SuperServiceImpl<BiTargetShopSettingMapper, BiTargetShopSettingEntity> implements BiTargetShopSettingService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetShopSettingDTO.AddDTO addDTO) {
        BiTargetShopSettingEntity biTargetShopSettingEntity = new BiTargetShopSettingEntity();
        BeanMapperUtils.copy(addDTO, biTargetShopSettingEntity);

        // 数据处理
        handleData(biTargetShopSettingEntity);

        log.info("开始新增店铺目标设置单");
        boolean save = super.save(biTargetShopSettingEntity);
        if(!save) {
            throw new ServiceException("店铺目标设置单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "店铺目标设置单" , biTargetShopSettingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, biTargetShopSettingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return biTargetShopSettingEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BiTargetShopSettingDTO.UpdateDTO updateDTO) {
        BiTargetShopSettingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "店铺目标设置单"));
        BiTargetShopSettingEntity biTargetShopSettingEntity =  BeanMapperUtils.map(BiTargetShopSettingEntity.class, updateDTO);

        // 数据处理
        handleData(biTargetShopSettingEntity);
        log.info("编辑 开始修改店铺目标设置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(biTargetShopSettingEntity);
        if(!save) {
            throw new ServiceException("店铺目标设置单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录店铺目标设置单日志数据，id：【{}】", biTargetShopSettingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), biTargetShopSettingEntity.getId(), "店铺目标设置单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, biTargetShopSettingEntity, null, biTargetShopSettingEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(BiTargetShopSettingEntity biTargetShopSettingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
