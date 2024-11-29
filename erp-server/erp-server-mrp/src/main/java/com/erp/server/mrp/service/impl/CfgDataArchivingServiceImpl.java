package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgDataArchivingDTO;
import com.erp.model.mrp.entity.CfgDataArchivingEntity;
import com.erp.server.mrp.mapper.CfgDataArchivingMapper;
import com.erp.server.mrp.service.CfgDataArchivingService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * <p>
 * 归档配置 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-09-26
 */
@Service
public class CfgDataArchivingServiceImpl extends SuperServiceImpl<CfgDataArchivingMapper, CfgDataArchivingEntity> implements CfgDataArchivingService {


    @Override
    public List<CfgDataArchivingEntity> getEffectiveData() {
        return list(Wrappers.<CfgDataArchivingEntity>lambdaQuery()
                .eq(CfgDataArchivingEntity::getDisabled, false)
                .orderByAsc(CfgDataArchivingEntity::getCreateTime)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveData(CfgDataArchivingEntity config) {
        try {
            Class<?> clazz = Class.forName(config.getArchiveFullPath());
            TableName tableName = clazz.getAnnotation(TableName.class);
            baseMapper.moveData(config.getTableName(), tableName.value());
            baseMapper.deleteSource(config.getTableName());
        } catch (ClassNotFoundException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void archiveData(CfgDataArchivingEntity config, String detailId) {
        try {
            Class<?> clazz = Class.forName(config.getArchiveFullPath());
            TableName tableName = clazz.getAnnotation(TableName.class);
            baseMapper.moveDataByRelId(config.getTableName(), tableName.value(), String.format(config.getRefSql(), detailId));
            baseMapper.deleteSourceByRelId(config.getTableName(), String.format(config.getRefSql(), detailId));
        } catch (ClassNotFoundException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void saveData(CfgDataArchivingDTO dto) {
        CfgDataArchivingEntity entity = BeanMapperUtils.map(CfgDataArchivingEntity.class, dto);
        save(entity);
    }

    @Override
    public void dataArchivingSuggestion(String sourceId) {
        baseMapper.moveDeliverySuggest(sourceId);
        if (ObjectUtils.isEmpty(sourceId)) {
            baseMapper.movePurchaseSuggestMerge();
        }
        baseMapper.movePurchaseSuggest(sourceId);
        baseMapper.deleteDeliverySuggest(sourceId);
        baseMapper.deletePurchaseSuggest(sourceId);
        if (ObjectUtils.isEmpty(sourceId)) {
            baseMapper.deletePurchaseSuggestMerge();
        }
    }
}
