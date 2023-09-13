package com.erp.server.bi.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.bi.entity.BiTargetStaffSettingEntity;

import com.common.core.exception.ServiceException;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.erp.server.bi.mapper.BiTargetStaffSettingMapper;
import com.erp.server.bi.service.BiTargetStaffSettingService;
import com.erp.server.bi.service.BiTargetYearService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.bi.dto.BiTargetStaffSettingDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 人员目标设置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BiTargetStaffSettingServiceImpl extends SuperServiceImpl<BiTargetStaffSettingMapper, BiTargetStaffSettingEntity> implements BiTargetStaffSettingService {

    @Autowired
    private BiTargetYearService biTargetYearService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetStaffSettingDTO.AddDTO addDTO) {
        BiTargetYearEntity targetYear = new BiTargetYearEntity();
        BeanMapperUtils.copy(addDTO, targetYear);
        List<BiTargetStaffSettingDTO.CommonDTO> detailList = addDTO.getDetailList();
        // 数据处理
        handleData(targetYear.getYear(), detailList);
        Boolean save = biTargetYearService.save(targetYear);
        if (!save) {
            throw new ServiceException("人员目标设置单保存失败");
        }
        //添加明细
        this.batchAdd(targetYear.getId(), detailList);
        return targetYear.getId();
    }

    /**
     * 添加明细的
     *
     * @param mainId
     * @param detailList
     */
    public void batchAdd(String mainId, List<BiTargetStaffSettingDTO.CommonDTO> detailList) {
        List<BiTargetStaffSettingEntity> entityList = BeanMapperUtils.copyList(BiTargetStaffSettingEntity.class, detailList);
        entityList.forEach(e -> e.setMainId(mainId));
        this.saveBatch(entityList);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BiTargetStaffSettingDTO.UpdateDTO updateDTO) {
        BiTargetYearEntity targetYear = new BiTargetYearEntity();

        BiTargetStaffSettingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "人员目标设置单"));
        BiTargetStaffSettingEntity biTargetStaffSettingEntity = BeanMapperUtils.map(BiTargetStaffSettingEntity.class, updateDTO);

        // 数据处理
        handleData(biTargetStaffSettingEntity);
        log.info("编辑 开始修改人员目标设置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(biTargetStaffSettingEntity);
        if (!save) {
            throw new ServiceException("人员目标设置单保存失败");
        }
        return Boolean.TRUE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(String year, List<BiTargetStaffSettingDTO.CommonDTO> detailList) {
        List<BiTargetStaffSettingDTO.ListDetailDTO> existList = baseMapper.listByYear(year);
        List<String> existStaff = Lists.newArrayList();
        for (BiTargetStaffSettingDTO.CommonDTO item : detailList) {
            BiTargetStaffSettingDTO.ListDetailDTO existDb = existList.stream().filter(e -> !e.getId().equals(item.getId()) &&
                    e.getStaffId().equals(item.getStaffId()) &&
                    e.getMetrics().equals(item.getMetrics()) &&
                    e.getMonth().equals(item.getMonth())).findFirst().orElse(null);
            if (existDb != null) {
                existStaff.add(existDb.getStaffName());
            }
        }
        if (CollectionUtils.isNotEmpty(existStaff)) {
            String existStaffName = existStaff.stream().collect(Collectors.joining(",");
            throw new ServiceException(ApiError.YEAR_METRICS_EXIST, existStaffName);
        }


    }
}
