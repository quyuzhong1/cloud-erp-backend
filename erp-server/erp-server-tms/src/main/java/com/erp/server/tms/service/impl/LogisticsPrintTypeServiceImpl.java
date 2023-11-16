package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.LogisticsMappingEntity;
import com.erp.model.tms.entity.LogisticsPrintTypeEntity;
import com.erp.server.tms.mapper.LogisticsPrintTypeMapper;
import com.erp.server.tms.service.LogisticsPrintTypeService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsPrintTypeDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 面板打印设置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsPrintTypeServiceImpl extends SuperServiceImpl<LogisticsPrintTypeMapper, LogisticsPrintTypeEntity> implements LogisticsPrintTypeService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(String channelId, List<LogisticsPrintTypeDTO.AddDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        List<LogisticsPrintTypeEntity> printTypeList = BeanMapperUtils.copyList(LogisticsPrintTypeEntity.class, list);
        printTypeList.forEach(p -> p.setLogisticsChannelId(channelId));
        return this.saveBatch(printTypeList);

    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(String channelId, List<LogisticsPrintTypeDTO.UpdateDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        List<LogisticsPrintTypeEntity> updateList = BeanMapperUtils.copyList(LogisticsPrintTypeEntity.class, list);
        updateList.forEach(p -> p.setLogisticsChannelId(channelId));
        List<LogisticsPrintTypeEntity> dbList = this.listDbByChannelId(channelId);
        List<String> updateIdList = updateList.stream().filter(u -> StringUtils.isNotBlank(u.getId())).
                map(LogisticsPrintTypeEntity::getId).collect(Collectors.toList());
        List<String> deleteIdList = dbList.stream().filter(d -> !updateIdList.contains(d.getId())).map(LogisticsPrintTypeEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        return this.saveOrUpdateBatch(updateList);
    }

    @Override
    public List<LogisticsPrintTypeDTO.ViewDTO> listByChannelId(String channelId) {
        List<LogisticsPrintTypeEntity> dbList = this.listDbByChannelId(channelId);
        return BeanMapperUtils.copyList(LogisticsPrintTypeDTO.ViewDTO.class, dbList);
    }


    @Override
    public void removeByChannelIdList(List<String> channelIdList) {
        if (CollectionUtils.isEmpty(channelIdList)) {
            return;
        }
        this.lambdaUpdate().in(LogisticsPrintTypeEntity::getLogisticsChannelId, channelIdList).remove();

    }

    @Override
    public void copy(String channelId, String addChannelId) {
        List<LogisticsPrintTypeEntity> list = listDbByChannelId(channelId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<LogisticsPrintTypeEntity> addList = BeanMapperUtils.copyList(LogisticsPrintTypeEntity.class, list);
            addList.forEach(a -> a.setLogisticsChannelId(addChannelId));
            this.saveBatch(addList);
        }
    }

    public List<LogisticsPrintTypeEntity> listDbByChannelId(String channelId) {
        return this.lambdaQuery().eq(LogisticsPrintTypeEntity::getLogisticsChannelId, channelId).list();
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsPrintTypeEntity logisticsPrintTypeEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
