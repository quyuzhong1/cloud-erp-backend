package com.erp.server.bi.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.bi.entity.BiProductInfoEntity;
import com.erp.model.bi.entity.BiTargetStaffSettingEntity;

import com.common.core.exception.ServiceException;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.server.bi.mapper.BiTargetStaffSettingMapper;
import com.erp.server.bi.service.BiTargetStaffSettingService;
import com.erp.server.bi.service.BiTargetYearService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
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
     * 修改人员目标设置
     *
     * @param mainId
     * @param detailList
     */
    public void batchUpdate(String mainId, List<BiTargetStaffSettingDTO.CommonDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<BiTargetStaffSettingEntity> dbList = this.listBaseByMainId(mainId);
        List<Pair<String, String>> pairList = detailList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        List<BiTargetStaffSettingEntity> saveOrUpdateList = BeanMapperUtils.copyList(BiTargetStaffSettingEntity.class, detailList);
        saveOrUpdateList.stream().forEach(s -> s.setMainId(mainId));
        this.saveOrUpdateBatch(saveOrUpdateList);
    }

    /**
     * 获取到删除的id
     *
     * @param pairList
     * @param dbList
     * @return
     */
    private List<String> getDeleteIds(List<Pair<String, String>> pairList, List<BiTargetStaffSettingEntity> dbList) {
        List<String> ids = pairList.stream().filter(g -> StringUtils.isNotBlank(g.getKey())).
                map(obj -> obj.getKey()).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(BiTargetStaffSettingEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    /**
     * 根据主表id获取对应信息
     *
     * @param mainId
     * @return
     */
    public List<BiTargetStaffSettingEntity> listBaseByMainId(String mainId) {
        return this.lambdaQuery().eq(BiTargetStaffSettingEntity::getMainId, mainId).
                orderByDesc(BiTargetStaffSettingEntity::getId).list();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BiTargetStaffSettingDTO.UpdateDTO updateDTO) {
        String id = updateDTO.getId();
        BiTargetYearEntity oldTargetYear = biTargetYearService.getById(id);
        Optional.ofNullable(oldTargetYear).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "人员目标设置"));
        List<BiTargetStaffSettingDTO.CommonDTO> detailList = updateDTO.getDetailList();
        handleData(updateDTO.getYear(), detailList);
        BiTargetYearEntity targetYear = BeanMapperUtils.map(BiTargetYearEntity.class, updateDTO);
        Boolean result = biTargetYearService.updateById(targetYear);
        if (!result) {
            throw new ServiceException("人员目标设置单保存失败");
        }
        this.batchUpdate(id, detailList);
        return Boolean.TRUE;
    }

    /**
     * 获取到详情信息
     *
     * @param id
     * @return com.erp.model.bi.dto.BiTargetStaffSettingDTO.ViewDTO
     * @author yl
     * @date 2023-09-13 15:17
     */
    @Override
    public BiTargetStaffSettingDTO.ViewDTO view(String id) {
        BiTargetStaffSettingDTO.ViewDTO view = new BiTargetStaffSettingDTO.ViewDTO();
        BiTargetYearEntity targetYear = biTargetYearService.getById(id);
        if (Objects.isNull(targetYear)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "人员目标设置");
        }
        BeanMapperUtils.copy(targetYear, view);
        List<BiTargetStaffSettingEntity> staffSettingDbList = this.listBaseByMainId(id);
        //根据指标分组
        Map<MetricsEnum, List<BiTargetStaffSettingEntity>> map = staffSettingDbList.stream().
                collect(Collectors.groupingBy(BiTargetStaffSettingEntity::getMetrics));
        //详情
        List<BiTargetStaffSettingDTO.DetailDTO> detailList = new ArrayList<>(map.size());
        for (Map.Entry<MetricsEnum, List<BiTargetStaffSettingEntity>> item : map.entrySet()) {
            BiTargetStaffSettingDTO.DetailDTO detail = new BiTargetStaffSettingDTO.DetailDTO();
            MetricsEnum metricsEnum = item.getKey();
            detail.setMetrics(metricsEnum);
            detail.setMetricsName(metricsEnum.getName());
            List<BiTargetStaffSettingEntity> staffSettingList = item.getValue();
            List<BiTargetStaffSettingDTO.CommonDTO> staffSettingResultList = new ArrayList<>(staffSettingList.size());
            for (BiTargetStaffSettingEntity staffSetting : staffSettingList) {


            }

        }
        return null;
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
            String existStaffName = existStaff.stream().collect(Collectors.joining(","));
            throw new ServiceException(ApiError.YEAR_METRICS_EXIST, existStaffName);
        }


    }
}
