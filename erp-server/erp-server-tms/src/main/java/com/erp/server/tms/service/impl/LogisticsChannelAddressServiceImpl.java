package com.erp.server.tms.service.impl;


import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.LogisticsChannelAddressDTO;
import com.erp.model.tms.entity.LogisticsChannelAddressEntity;
import com.erp.model.tms.entity.LogisticsMappingEntity;
import com.erp.server.tms.mapper.LogisticsChannelAddressMapper;
import com.erp.server.tms.service.LogisticsChannelAddressService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 渠道地址表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsChannelAddressServiceImpl extends SuperServiceImpl<LogisticsChannelAddressMapper, LogisticsChannelAddressEntity> implements LogisticsChannelAddressService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(String channelId, List<LogisticsChannelAddressDTO.AddDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        List<LogisticsChannelAddressEntity> saveList = BeanMapperUtils.copyList(LogisticsChannelAddressEntity.class, list);
        saveList.forEach(p -> p.setLogisticsChannelId(channelId));

        // 数据处理
        handleData(saveList);
        return this.saveBatch(saveList);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(String channelId, List<LogisticsChannelAddressDTO.UpdateDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        List<LogisticsChannelAddressEntity> updateList = BeanMapperUtils.copyList(LogisticsChannelAddressEntity.class, list);
        updateList.forEach(p -> p.setLogisticsChannelId(channelId));
        // 数据处理
        handleData(updateList);
        List<LogisticsChannelAddressEntity> dbList = this.listDbByChannelId(channelId);
        List<String> updateIdList = updateList.stream().filter(u -> StringUtils.isNotBlank(u.getId())).
                map(LogisticsChannelAddressEntity::getId).collect(Collectors.toList());
        List<String> deleteIdList = dbList.stream().filter(d -> !updateIdList.contains(d.getId())).map(LogisticsChannelAddressEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        return this.saveOrUpdateBatch(updateList);
    }


    @Override
    public List<LogisticsChannelAddressDTO.ViewDTO> listByChannelId(String channelId) {

        return baseMapper.listByChannelId(channelId);
    }

    @Override
    public void removeByChannelIdList(List<String> channelIdList) {
        if (CollectionUtils.isEmpty(channelIdList)) {
            return;
        }
        this.lambdaUpdate().in(LogisticsChannelAddressEntity::getLogisticsChannelId, channelIdList).remove();

    }

    @Override
    public void copy(String channelId, String addChannelId) {
        List<LogisticsChannelAddressEntity> list = listDbByChannelId(channelId);
        if (CollectionUtils.isNotEmpty(list)) {
//            List<LogisticsChannelAddressEntity> addList = BeanMapperUtils.copyList(LogisticsChannelAddressEntity.class, list);
            list.forEach(obj ->{
                obj.setLogisticsChannelId(addChannelId);
                obj.setId(IdWorker.getIdStr());
            });
            this.saveBatch(list);
        }
    }

    public List<LogisticsChannelAddressEntity> listDbByChannelId(String channelId) {
        return this.lambdaQuery().eq(LogisticsChannelAddressEntity::getLogisticsChannelId, channelId).list();
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(List<LogisticsChannelAddressEntity> list) {
        
        List<String> shopIdList = list.stream().map(LogisticsChannelAddressEntity::getShopId).collect(Collectors.toList());
    }
}
