package com.erp.server.oms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cRefDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cRefEntity;
import com.erp.model.oms.enums.SoB2cOptionTypeEnum;
import com.erp.server.oms.mapper.SoB2cRefMapper;
import com.erp.server.oms.service.SoB2cDetailService;
import com.erp.server.oms.service.SoB2cRefService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Resource
    @Lazy
    private SoB2cService soB2cService;

    @Resource
    @Lazy
    private SoB2cDetailService soB2cDetailService;

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
        LambdaQueryWrapper<SoB2cRefEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SoB2cRefEntity::getTargetId,id);
        if (ObjectUtils.isNotEmpty(typeEnum)) {
            queryWrapper.eq(SoB2cRefEntity::getType,typeEnum.getCode());
        }
        return this.list(queryWrapper);
    }

    @Override
    public List<SoB2cRefEntity> listByTargetIds(List<String> targetIdList, SoB2cOptionTypeEnum typeEnum) {
        LambdaQueryWrapper<SoB2cRefEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(SoB2cRefEntity::getTargetId,targetIdList);
        if (ObjectUtils.isNotEmpty(typeEnum)) {
            queryWrapper.eq(SoB2cRefEntity::getType,typeEnum.getCode());
        }
        return this.list(queryWrapper);
    }

    @Override
    public List<SoB2cRefEntity> listSourceByTargetIds(List<String> targetIdList, String typeEnum) {
        if (CollectionUtils.isEmpty(targetIdList)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listSourceByTargetIds(targetIdList, typeEnum);
    }


    @Override
    public Boolean deleteByTargetIds(List<String> mainIds) {
        return lambdaUpdate().in(SoB2cRefEntity::getTargetId,mainIds).remove();

    }

    @Override
    public List<SoB2cRefEntity> listBySourceIds(List<String> sourceIdList, SoB2cOptionTypeEnum typeEnum) {
        if (CollectionUtils.isEmpty(sourceIdList)) {
            return Collections.EMPTY_LIST;
        }
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
        List<SoB2cRefEntity> list1 = lambdaQuery()
                .select(SoB2cRefEntity::getId, SoB2cRefEntity::getSourceDetailId, SoB2cRefEntity::getSourceId, SoB2cRefEntity::getTargetDetailId, SoB2cRefEntity::getTargetId, SoB2cRefEntity::getType)
                .in(SoB2cRefEntity::getSourceId, ids)
                .list();
        List<SoB2cRefEntity> list2 = lambdaQuery()
                .select(SoB2cRefEntity::getId, SoB2cRefEntity::getSourceDetailId, SoB2cRefEntity::getSourceId, SoB2cRefEntity::getTargetDetailId, SoB2cRefEntity::getTargetId, SoB2cRefEntity::getType)
                .in(SoB2cRefEntity::getTargetId, ids)
                .list();
        return Stream.concat(list1.stream(),list2.stream()).distinct().collect(Collectors.toList());
    }

    @Override
    public SoB2cRefDTO.SplitCombinationDTO getSplitCombination(String soId) {
        if(ObjectUtil.isEmpty(soId)){
            return new SoB2cRefDTO.SplitCombinationDTO();
        }
        List<String> allSplitSoIds = this.baseMapper.getAllSplitIds(soId);
        if(CollectionUtils.isEmpty(allSplitSoIds)){
            return new SoB2cRefDTO.SplitCombinationDTO();
        }
        List<SoB2cEntity> soB2cEntityList = soB2cService.listByIds(allSplitSoIds);
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainIds(allSplitSoIds);
        return new SoB2cRefDTO.SplitCombinationDTO(soB2cEntityList,soB2cDetailEntityList);
    }
}
