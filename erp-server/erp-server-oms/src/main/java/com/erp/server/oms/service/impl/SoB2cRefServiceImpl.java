package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cRefDTO;
import com.erp.model.oms.entity.SoB2cRefEntity;
import com.erp.model.oms.enums.SoB2cOptionTypeEnum;
import com.erp.server.oms.mapper.SoB2cRefMapper;
import com.erp.server.oms.service.SoB2cRefService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * B2C销售订单合并拆分关联表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cRefServiceImpl extends SuperServiceImpl<SoB2cRefMapper, SoB2cRefEntity> implements SoB2cRefService {


    @Override
    public Boolean add(List<SoB2cRefDTO.AddDTO> refList) {
        if (CollectionUtils.isEmpty(refList)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        List<SoB2cRefEntity> list = BeanMapperUtils.copyList(SoB2cRefEntity.class, refList);
        return this.saveBatch(list);
    }

    @Override
    public Boolean add(String type, String sourceId, String targetId) {
        SoB2cRefDTO.AddDTO addDTO = new SoB2cRefDTO.AddDTO();
        addDTO.setType(type);
        addDTO.setSourceId(sourceId);
        addDTO.setTargetId(targetId);
        return add(Arrays.asList(addDTO));
    }

    @Override
    public List<SoB2cRefEntity> listByTargetId(String id, SoB2cOptionTypeEnum typeEnum) {
        return lambdaQuery()
                .eq(SoB2cRefEntity::getTargetId,id)
                .eq(ObjectUtils.isEmpty(typeEnum),SoB2cRefEntity::getType,typeEnum.getCode())
                .list();
    }
    @Override
    public List<SoB2cRefEntity> listByTargetIds(List<String> ids, SoB2cOptionTypeEnum typeEnum) {
        return lambdaQuery()
                .in(SoB2cRefEntity::getTargetId,ids)
                .eq(ObjectUtils.isEmpty(typeEnum),SoB2cRefEntity::getType,typeEnum.getCode())
                .list();
    }

    @Override
    public Boolean deleteByTargetIds(List<String> mainIds) {
        return lambdaUpdate().in(SoB2cRefEntity::getTargetId,mainIds).remove();

    }

    @Override
    public List<SoB2cRefEntity> listBySourceIds(List<String> sourceIdList, SoB2cOptionTypeEnum typeEnum) {
        return lambdaQuery()
                .in(SoB2cRefEntity::getSourceId,sourceIdList)
                .eq(ObjectUtils.isEmpty(typeEnum),SoB2cRefEntity::getType,typeEnum.getCode())
                .list();
    }

    @Override
    public List<SoB2cRefEntity> listBySourceIdOrTargetId(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery()
                .in(SoB2cRefEntity::getSourceId,ids)
                .or()
                .in(SoB2cRefEntity::getTargetId,ids)
                .list();
    }
}
