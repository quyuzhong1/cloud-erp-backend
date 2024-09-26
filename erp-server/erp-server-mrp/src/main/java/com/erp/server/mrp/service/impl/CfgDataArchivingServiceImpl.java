package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.core.exception.ServiceException;
import com.erp.model.mrp.entity.CfgDataArchivingEntity;
import com.erp.server.mrp.mapper.CfgDataArchivingMapper;
import com.erp.server.mrp.service.CfgDataArchivingService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .eq(CfgDataArchivingEntity::getDisabled, false));
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
}
