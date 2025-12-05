package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpFeishuInstanceIdsEntity;
import com.erp.server.dmp.mapper.DmpFeishuInstanceIdsMapper;
import com.erp.server.dmp.service.DmpFeishuInstanceIdsService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpFeishuInstanceIdsDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * DMP飞书变更实例IDS记录 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-10-30
 */
@Slf4j
@Service
public class DmpFeishuInstanceIdsServiceImpl extends SuperServiceImpl<DmpFeishuInstanceIdsMapper, DmpFeishuInstanceIdsEntity> implements DmpFeishuInstanceIdsService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpFeishuInstanceIdsDTO.AddDTO addDTO) {
        DmpFeishuInstanceIdsEntity dmpFeishuInstanceIdsEntity = new DmpFeishuInstanceIdsEntity();
        BeanMapperUtils.copy(addDTO, dmpFeishuInstanceIdsEntity);

        // 数据处理
        handleData(dmpFeishuInstanceIdsEntity);

        log.info("开始新增DMP飞书变更实例IDS记录");
        boolean save = super.save(dmpFeishuInstanceIdsEntity);
        if(!save) {
            throw new ServiceException("DMP飞书变更实例IDS记录保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "DMP飞书变更实例IDS记录" , dmpFeishuInstanceIdsEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpFeishuInstanceIdsEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpFeishuInstanceIdsEntity.getId(), dmpFeishuInstanceIdsEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpFeishuInstanceIdsDTO.UpdateDTO addOrUpdateDTO) {
        DmpFeishuInstanceIdsEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "DMP飞书变更实例IDS记录"));
        DmpFeishuInstanceIdsEntity dmpFeishuInstanceIdsEntity =  BeanMapperUtils.map(DmpFeishuInstanceIdsEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dmpFeishuInstanceIdsEntity);
        log.info("编辑 开始修改DMP飞书变更实例IDS记录数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpFeishuInstanceIdsEntity);
        if(!save) {
            throw new ServiceException("DMP飞书变更实例IDS记录保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录DMP飞书变更实例IDS记录日志数据，id：【{}】", dmpFeishuInstanceIdsEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpFeishuInstanceIdsEntity.getId(), "DMP飞书变更实例IDS记录");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpFeishuInstanceIdsEntity, null, dmpFeishuInstanceIdsEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpFeishuInstanceIdsEntity dmpFeishuInstanceIdsEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
