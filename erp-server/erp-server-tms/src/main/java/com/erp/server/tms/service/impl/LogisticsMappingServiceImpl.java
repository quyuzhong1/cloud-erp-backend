package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.LogisticsMappingEntity;
import com.erp.server.tms.mapper.LogisticsMappingMapper;
import com.erp.server.tms.service.LogisticsMappingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsMappingDTO;

import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 物流渠道映射表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsMappingServiceImpl extends SuperServiceImpl<LogisticsMappingMapper, LogisticsMappingEntity> implements LogisticsMappingService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(String channelId, List<LogisticsMappingDTO.AddDTO> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return Boolean.FALSE;
        }
        List<LogisticsMappingEntity> saveList = BeanMapperUtils.copyList(LogisticsMappingEntity.class, dtoList);
        saveList.forEach(s -> s.setLogisticsChannelId(channelId));
        return this.saveBatch(saveList);

    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsMappingDTO.UpdateDTO updateDTO) {
        LogisticsMappingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流渠道映射单"));
        LogisticsMappingEntity logisticsMappingEntity = BeanMapperUtils.map(LogisticsMappingEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsMappingEntity);
        log.info("编辑 开始修改物流渠道映射单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsMappingEntity);
        if (!save) {
            throw new ServiceException("物流渠道映射单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录物流渠道映射单日志数据，id：【{}】", logisticsMappingEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsMappingEntity.getId(), "物流渠道映射单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsMappingEntity, null, logisticsMappingEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsMappingEntity logisticsMappingEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
