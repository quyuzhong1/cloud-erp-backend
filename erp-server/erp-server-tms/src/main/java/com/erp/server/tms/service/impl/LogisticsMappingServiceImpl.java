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
import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsMappingDTO;

import java.util.*;
import java.util.stream.Collectors;

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
    public Boolean update(String channelId, List<LogisticsMappingDTO.UpdateDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        List<LogisticsMappingEntity> updateList = BeanMapperUtils.copyList(LogisticsMappingEntity.class, list);
        updateList.forEach(s -> s.setLogisticsChannelId(channelId));
        List<LogisticsMappingEntity> dbList = this.listDbByChannelId(channelId);
        List<String> updateIdList = updateList.stream().filter(u -> StringUtils.isNotBlank(u.getId())).
                map(LogisticsMappingEntity::getId).collect(Collectors.toList());
        List<String> deleteIdList = dbList.stream().filter(d -> !updateIdList.contains(d.getId())).map(LogisticsMappingEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        return this.saveOrUpdateBatch(updateList);
    }

    @Override
    public List<LogisticsMappingDTO.ViewDTO> listByChannelId(String channelId) {
        List<LogisticsMappingEntity> dbList = this.listDbByChannelId(channelId);
        return BeanMapperUtils.copyList(LogisticsMappingDTO.ViewDTO.class, dbList);
    }

    public List<LogisticsMappingEntity> listDbByChannelId(String channelId) {
        return this.lambdaQuery().eq(LogisticsMappingEntity::getLogisticsChannelId, channelId).list();

    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsMappingEntity logisticsMappingEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
