package com.erp.server.tms.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.LogisticsMappingDTO;
import com.erp.model.tms.entity.LogisticsMappingEntity;
import com.erp.model.tms.entity.ShippingTemplateRefChannelEntity;
import com.erp.server.tms.mapper.LogisticsMappingMapper;
import com.erp.server.tms.service.LogisticsMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public void removeByChannelIdList(List<String> channelIdList) {
        if (CollectionUtils.isEmpty(channelIdList)) {
            return;
        }
        this.lambdaUpdate().in(LogisticsMappingEntity::getLogisticsChannelId, channelIdList).remove();
    }

    @Override
    public void copy(String channelId, String addChannelId) {
        List<LogisticsMappingEntity> list = listDbByChannelId(channelId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<LogisticsMappingEntity> addList = BeanMapperUtils.copyList(LogisticsMappingEntity.class, list);
            addList.forEach(obj ->{
                obj.setLogisticsChannelId(addChannelId);
                obj.setId("");
            });
            this.saveBatch(addList);
        }


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
