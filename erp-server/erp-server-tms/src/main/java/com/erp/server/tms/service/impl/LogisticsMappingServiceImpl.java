package com.erp.server.tms.service.impl;


import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.constant.SqlConstants;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.LogisticsMappingDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsMappingEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.LogisticsMappingTypeEnum;
import com.erp.server.tms.mapper.LogisticsMappingMapper;
import com.erp.server.tms.service.LogisticsMappingService;
import com.erp.server.tms.service.LogisticsSaleChannelService;
import com.erp.server.tms.service.TmsCarrierService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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

    @Lazy
    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;
    @Resource
    private TmsCarrierService tmsCarrierService;

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
        List<LogisticsMappingEntity> dbList = this.listDbByChannelIdAndType(channelId, LogisticsMappingTypeEnum.PLATFORM.getCode());
        List<String> updateIdList = updateList.stream().filter(u -> StringUtils.isNotBlank(u.getId())).
                map(LogisticsMappingEntity::getId).collect(Collectors.toList());
        List<String> deleteIdList = dbList.stream().filter(d -> !updateIdList.contains(d.getId())).map(LogisticsMappingEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        return this.saveOrUpdateBatch(updateList);
    }

    @Override
    public List<LogisticsMappingDTO.ViewDTO> listByChannelIdAndType(String channelId, String type) {
        List<LogisticsMappingEntity> dbList = this.listDbByChannelIdAndType(channelId, type);
        return BeanMapperUtils.copyList(LogisticsMappingDTO.ViewDTO.class, dbList);
    }
    @Override
    public List<LogisticsMappingDTO.ViewDTO> listByChannelIdsAndType(List<String> channelIdList, String type) {
        List<LogisticsMappingEntity> list = this.lambdaQuery().in(LogisticsMappingEntity::getLogisticsChannelId, channelIdList).eq(LogisticsMappingEntity::getType,type).list();
        return BeanMapperUtils.copyList(LogisticsMappingDTO.ViewDTO.class, list);
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
        List<LogisticsMappingEntity> list = this.lambdaQuery().eq(LogisticsMappingEntity::getLogisticsChannelId, channelId).list();
        if (CollectionUtils.isNotEmpty(list)) {
//            List<LogisticsMappingEntity> addList = BeanMapperUtils.copyList(LogisticsMappingEntity.class, list);
            list.forEach(obj -> {
                obj.setLogisticsChannelId(addChannelId);
                obj.setId(IdWorker.getIdStr());
            });
            this.saveBatch(list);
        }


    }

    @Override
    public LogisticsSaleChannelEntity getBySalesPlatform(String salesPlatform, String channelId) {
        return baseMapper.getBySalesPlatform(salesPlatform,channelId);
    }

    @Override
    public LogisticsMappingEntity getByLogisticsMappingParam(LogisticsMappingDTO.SearchParamDTO paramDTO) {
        return lambdaQuery().eq(LogisticsMappingEntity::getSalesPlatform,paramDTO.getSalesPlatform())
                .eq(LogisticsMappingEntity::getLogisticsChannelId,paramDTO.getLogisticsChannelId())
                .eq(LogisticsMappingEntity::getPlatformLogisticsChannelId,paramDTO.getLogisticsSaleChannelId())
                .last(SqlConstants.LIMIT_1)
                .one();
    }
    @Override
    public List<LogisticsMappingEntity> listDbByChannelIdAndType(String channelId, String type) {
        return this.lambdaQuery().eq(LogisticsMappingEntity::getLogisticsChannelId, channelId).eq(LogisticsMappingEntity::getType,type).list();

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addWarehouseMapping(String channelId, List<LogisticsMappingDTO.AddDTO> warehouseMappingList) {
        if (CollectionUtils.isEmpty(warehouseMappingList)) {
            return;
        }
        if(warehouseMappingList.stream().anyMatch(v->StringUtils.isBlank(v.getWarehouseId()))){
            throw new ServiceException("仓库映射列表中存在仓库id为空的数据，请检查！");
        }
        warehouseMappingList.forEach(v->v.setType(LogisticsMappingTypeEnum.WAREHOUSE.getCode()));
        List<LogisticsMappingEntity> saveList = BeanMapperUtils.copyList(LogisticsMappingEntity.class, warehouseMappingList);
        saveList.forEach(s -> s.setLogisticsChannelId(channelId));
        this.saveBatch(saveList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void warehouseUpdate(String channelId, List<LogisticsMappingDTO.UpdateDTO> mappingList) {
        if (CollectionUtils.isEmpty(mappingList)) {
            this.lambdaUpdate().eq(LogisticsMappingEntity::getLogisticsChannelId, channelId).eq(LogisticsMappingEntity::getType, LogisticsMappingTypeEnum.WAREHOUSE.getCode()).remove();
            return;
        }
        mappingList.forEach(v -> {
            if (StringUtils.isBlank(v.getWarehouseId())) {
                throw new ServiceException("仓库映射列表中存在仓库id为空的数据，请检查！");
            }
            v.setType(LogisticsMappingTypeEnum.WAREHOUSE.getCode());
        });
        List<LogisticsMappingEntity> updateList = BeanMapperUtils.copyList(LogisticsMappingEntity.class, mappingList);
        updateList.forEach(s -> s.setLogisticsChannelId(channelId));
        List<LogisticsMappingEntity> dbList = this.listDbByChannelIdAndType(channelId, LogisticsMappingTypeEnum.WAREHOUSE.getCode());
        List<String> updateIdList = updateList.stream().filter(u -> StringUtils.isNotBlank(u.getId())).
                map(LogisticsMappingEntity::getId).collect(Collectors.toList());
        List<String> deleteIdList = dbList.stream().filter(d -> !updateIdList.contains(d.getId())).map(LogisticsMappingEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        this.saveOrUpdateBatch(updateList);
    }
}
