package com.erp.server.bi.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.bi.entity.BiTargetStaffSettingEntity;

import com.common.core.exception.ServiceException;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.erp.server.bi.mapper.BiTargetStaffSettingMapper;
import com.erp.server.bi.service.BiTargetStaffSettingService;
import com.erp.server.bi.service.BiTargetYearService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.bi.dto.BiTargetStaffSettingDTO;

import java.util.*;

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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetStaffSettingDTO.AddDTO addDTO) {
        BiTargetYearEntity targetYear = new BiTargetYearEntity();
        BeanMapperUtils.copy(addDTO, targetYear);
        List<BiTargetStaffSettingDTO.CommonDTO> detailList = addDTO.getDetailList();

        // 数据处理
        handleData(targetYear.getYear(), detailList);

        log.info("开始新增人员目标设置单");
        boolean save = biTargetYearService.save(targetYear);
        if (!save) {
            throw new ServiceException("人员目标设置单保存失败");
        }

        return biTargetStaffSettingEntity.getId();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BiTargetStaffSettingDTO.UpdateDTO updateDTO) {
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
        for(BiTargetStaffSettingDTO.CommonDTO item : detailList){
            //existList.stream().filter(e->)
        }

        // TODO 验证数据 & 数据赋值

    }
}
