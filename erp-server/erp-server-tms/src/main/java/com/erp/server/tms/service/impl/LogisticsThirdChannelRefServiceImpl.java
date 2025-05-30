package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.LogisticsThirdChannelRefEntity;
import com.erp.server.tms.mapper.LogisticsThirdChannelRefMapper;
import com.erp.server.tms.service.LogisticsThirdChannelRefService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 物流-第三方渠道关系表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-05-29
 */
@Slf4j
@Service
public class LogisticsThirdChannelRefServiceImpl extends SuperServiceImpl<LogisticsThirdChannelRefMapper, LogisticsThirdChannelRefEntity> implements LogisticsThirdChannelRefService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsThirdChannelRefDTO.AddDTO addDTO) {
        LogisticsThirdChannelRefEntity logisticsThirdChannelRefEntity = new LogisticsThirdChannelRefEntity();
        BeanMapperUtils.copy(addDTO, logisticsThirdChannelRefEntity);

        // 数据处理
        handleData(logisticsThirdChannelRefEntity);

        log.info("开始新增物流-第三方渠道关系单");
        boolean save = super.save(logisticsThirdChannelRefEntity);
        if(!save) {
            throw new ServiceException("物流-第三方渠道关系单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流-第三方渠道关系单" , logisticsThirdChannelRefEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsThirdChannelRefEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(logisticsThirdChannelRefEntity.getId(), logisticsThirdChannelRefEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsThirdChannelRefDTO.UpdateDTO addOrUpdateDTO) {
        LogisticsThirdChannelRefEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物流-第三方渠道关系单"));
        LogisticsThirdChannelRefEntity logisticsThirdChannelRefEntity =  BeanMapperUtils.map(LogisticsThirdChannelRefEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(logisticsThirdChannelRefEntity);
        log.info("编辑 开始修改物流-第三方渠道关系单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsThirdChannelRefEntity);
        if(!save) {
            throw new ServiceException("物流-第三方渠道关系单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录物流-第三方渠道关系单日志数据，id：【{}】", logisticsThirdChannelRefEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsThirdChannelRefEntity.getId(), "物流-第三方渠道关系单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsThirdChannelRefEntity, null, logisticsThirdChannelRefEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsThirdChannelRefEntity> listByChannelIds(List<String> channelIds) {
        if (CollUtil.isEmpty(channelIds)){
            return Collections.emptyList();
        }
        return this.lambdaQuery()
                .select(LogisticsThirdChannelRefEntity::getIsPushMobile)
                .select(LogisticsThirdChannelRefEntity::getLogisticsChannelId)
                .select(LogisticsThirdChannelRefEntity::getThirdSupplierCode)
                .in(LogisticsThirdChannelRefEntity::getLogisticsChannelId, channelIds).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsThirdChannelRefEntity logisticsThirdChannelRefEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
