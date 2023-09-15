package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.enums.TargetSearchTypeEnum;
import com.erp.server.bi.service.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;

/**
 * @author Will
 * @version 1.0
 * @description: 目标相关报表实现
 * @date 2023/9/14 12:23
 */
@Service
public class BiTargetReportServiceImpl implements BiTargetReportService {

    private BiTargetStaffSettingService biTargetStaffSettingService;

    private BiTargetShopSettingService biTargetShopSettingService;

    private BiTargetCategorySettingService biTargetCategorySettingService;

    private BiTargetSkuSettingService biTargetSkuSettingService;

    @Override
    public LinkedHashMap<String, Object> targetFinish(TargetFinishDTO.ParamDTO dto) {
        if (ObjectUtils.isEmpty(dto.getDate())) {
            dto.setDate(LocalDate.now());
        }
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getDate().with(TemporalAdjusters.firstDayOfYear())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getDate().with(TemporalAdjusters.lastDayOfYear())), LocalTime.MAX);

        if (TargetSearchTypeEnum.DEPT.equals(dto.getSearchType()) || TargetSearchTypeEnum.USER.equals(dto.getSearchType())) {
            //根据指标查询人员目标值
            //biTargetStaffSettingService.listTargetFinish(dto);
        }
        if (TargetSearchTypeEnum.SHOP.equals(dto.getSearchType())) {
            //根据指标查询店铺目标值
            //biTargetShopSettingService.listTargetFinish(dto);
        }
        if (TargetSearchTypeEnum.CATEGORY.equals(dto.getSearchType())) {
            //根据指标查询品类目标值
            //biTargetCategorySettingService.listTargetFinish(dto);
        }
        if (TargetSearchTypeEnum.SKU.equals(dto.getSearchType())) {
            //根据指标查询SKU目标值
            //biTargetSkuSettingService.listTargetFinish(dto);
        }
        return null;
    }
}
