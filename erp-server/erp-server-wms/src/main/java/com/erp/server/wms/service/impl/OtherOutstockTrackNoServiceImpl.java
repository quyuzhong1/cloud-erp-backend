package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.OtherOutstockTrackNoDTO;
import com.erp.model.wms.entity.OtherOutstockEntity;
import com.erp.model.wms.entity.OtherOutstockTrackNoEntity;
import com.erp.server.wms.mapper.OtherOutstockMapper;
import com.erp.server.wms.mapper.OtherOutstockTrackNoMapper;
import com.erp.server.wms.service.OtherOutstockTrackNoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 其他出库单跟踪号映射表 服务实现类
 * </p>
 *
 * @author system
 * @since 2025-12-16
 */
@Slf4j
@Service
public class OtherOutstockTrackNoServiceImpl extends SuperServiceImpl<OtherOutstockTrackNoMapper, OtherOutstockTrackNoEntity> implements OtherOutstockTrackNoService {

    @Resource
    private OtherOutstockMapper otherOutstockMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdateTrackNo(OtherOutstockTrackNoDTO.BatchUpdateDTO dto) {
        if (dto == null || dto.getTrackNoList() == null || dto.getTrackNoList().isEmpty()) {
            return false;
        }

        for (OtherOutstockTrackNoDTO.UpdateDTO updateDTO : dto.getTrackNoList()) {
            if (CharSequenceUtil.isBlank(updateDTO.getOtherOutstockId())) {
                continue;
            }

            // 通过 other_outstock_id 查询获取 sourceCode
            OtherOutstockEntity otherOutstock = otherOutstockMapper.selectById(updateDTO.getOtherOutstockId());
            String otherOutstockSourceCode = otherOutstock != null ? otherOutstock.getSourceCode() : null;

            // 先删除该出库单的所有跟踪号
            lambdaUpdate()
                    .eq(OtherOutstockTrackNoEntity::getOtherOutstockId, updateDTO.getOtherOutstockId())
                    .remove();

            // 批量新增跟踪号
            if (updateDTO.getTrackNoList() != null && !updateDTO.getTrackNoList().isEmpty()) {
                List<OtherOutstockTrackNoEntity> entities = new ArrayList<>();
                for (String trackNo : updateDTO.getTrackNoList()) {
                    if (CharSequenceUtil.isNotBlank(trackNo)) {
                        OtherOutstockTrackNoEntity entity = new OtherOutstockTrackNoEntity();
                        entity.setId(IdUtil.getSnowflakeNextIdStr());
                        entity.setOtherOutstockId(updateDTO.getOtherOutstockId());
                        entity.setOtherOutstockCode(updateDTO.getOtherOutstockCode());
                        entity.setOtherOutstockSourceCode(otherOutstockSourceCode);
                        entity.setTrackNo(trackNo.trim());
                        entities.add(entity);
                    }
                }
                if (!entities.isEmpty()) {
                    this.saveBatch(entities);
                }
            }
        }

        return true;
    }

    @Override
    public OtherOutstockTrackNoDTO.ViewDTO getTrackNoByOutstockId(String otherOutstockId) {
        if (CharSequenceUtil.isBlank(otherOutstockId)) {
            return null;
        }
        
        List<OtherOutstockTrackNoEntity> entities = baseMapper.listByOutstockId(otherOutstockId);
        if (entities == null || entities.isEmpty()) {
            return null;
        }
        
        OtherOutstockTrackNoDTO.ViewDTO viewDTO = new OtherOutstockTrackNoDTO.ViewDTO();
        viewDTO.setOtherOutstockId(otherOutstockId);
        viewDTO.setOtherOutstockCode(entities.get(0).getOtherOutstockCode());
        viewDTO.setOtherOutstockSourceCode(entities.get(0).getOtherOutstockSourceCode());
        viewDTO.setTrackNoList(entities.stream()
                .map(OtherOutstockTrackNoEntity::getTrackNo)
                .collect(Collectors.toList()));
        
        return viewDTO;
    }

    @Override
    public List<OtherOutstockTrackNoDTO.ViewDTO> batchGetTrackNo(List<String> otherOutstockIds) {
        if (otherOutstockIds == null || otherOutstockIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<OtherOutstockTrackNoEntity> entities = baseMapper.listByOutstockIds(otherOutstockIds);
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }

        // 按 otherOutstockId 分组
        Map<String, List<OtherOutstockTrackNoEntity>> groupedMap = entities.stream()
                .collect(Collectors.groupingBy(OtherOutstockTrackNoEntity::getOtherOutstockId));

        // 转换为 ViewDTO 列表
        List<OtherOutstockTrackNoDTO.ViewDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<OtherOutstockTrackNoEntity>> entry : groupedMap.entrySet()) {
            List<OtherOutstockTrackNoEntity> trackNoList = entry.getValue();
            if (!trackNoList.isEmpty()) {
                OtherOutstockTrackNoDTO.ViewDTO viewDTO = new OtherOutstockTrackNoDTO.ViewDTO();
                OtherOutstockTrackNoEntity firstEntity = trackNoList.get(0);
                viewDTO.setOtherOutstockId(firstEntity.getOtherOutstockId());
                viewDTO.setOtherOutstockCode(firstEntity.getOtherOutstockCode());
                viewDTO.setOtherOutstockSourceCode(firstEntity.getOtherOutstockSourceCode());
                viewDTO.setTrackNoList(trackNoList.stream()
                        .map(OtherOutstockTrackNoEntity::getTrackNo)
                        .collect(Collectors.toList()));
                result.add(viewDTO);
            }
        }
        
        return result;
    }

    @Override
    public Map<String, List<String>> getTrackNoMapByOutstockIds(List<String> otherOutstockIds) {
        if (otherOutstockIds == null || otherOutstockIds.isEmpty()) {
            return new HashMap<>();
        }

        List<OtherOutstockTrackNoEntity> entities = baseMapper.listByOutstockIds(otherOutstockIds);
        
        return entities.stream()
                .collect(Collectors.groupingBy(
                        OtherOutstockTrackNoEntity::getOtherOutstockId,
                        Collectors.mapping(OtherOutstockTrackNoEntity::getTrackNo, Collectors.toList())
                ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByMainIds(List<String> mainIds) {
        if (mainIds == null || mainIds.isEmpty()) {
            return;
        }
        
        lambdaUpdate()
                .in(OtherOutstockTrackNoEntity::getOtherOutstockId, mainIds)
                .remove();
    }
}
