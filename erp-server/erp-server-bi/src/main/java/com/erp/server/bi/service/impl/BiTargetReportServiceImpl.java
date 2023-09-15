package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.entity.BiTargetCategorySettingEntity;
import com.erp.model.bi.entity.BiTargetShopSettingEntity;
import com.erp.model.bi.entity.BiTargetSkuSettingEntity;
import com.erp.model.bi.entity.BiTargetStaffSettingEntity;
import com.erp.model.bi.enums.TargetSearchTypeEnum;
import com.erp.server.bi.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 目标相关报表实现
 * @date 2023/9/14 12:23
 */
@Service
public class BiTargetReportServiceImpl implements BiTargetReportService {

    @Resource
    private BiTargetStaffSettingService biTargetStaffSettingService;

    @Resource
    private BiTargetShopSettingService biTargetShopSettingService;

    @Resource
    private BiTargetCategorySettingService biTargetCategorySettingService;

    @Resource
    private BiTargetSkuSettingService biTargetSkuSettingService;

    @Override
    public LinkedHashMap<String, Object> targetFinish(TargetFinishDTO.ParamDTO dto) {
        if (ObjectUtils.isEmpty(dto.getDate())) {
            dto.setDate(LocalDate.now());
        }
        // 获取年度开始时间和结束时间
        if (TargetSearchTypeEnum.DEPT.equals(dto.getSearchType()) ) {
            //根据指标查询部门目标值
            List<BiTargetStaffSettingEntity> list = biTargetStaffSettingService.listDeptTargetFinish(dto);
            //查询平台销量
        }
        if (TargetSearchTypeEnum.USER.equals(dto.getSearchType())) {
            //根据指标查询人员目标值
            List<BiTargetStaffSettingEntity> list = biTargetStaffSettingService.listUserTargetFinish(dto);
        }
        if (TargetSearchTypeEnum.SHOP.equals(dto.getSearchType())) {
            //根据指标查询店铺目标值
            List<BiTargetShopSettingEntity> list = biTargetShopSettingService.listTargetFinish(dto);
        }
        if (TargetSearchTypeEnum.CATEGORY.equals(dto.getSearchType())) {
            //根据指标查询品类目标值
            List<BiTargetCategorySettingEntity> list = biTargetCategorySettingService.listTargetFinish(dto);
        }
        if (TargetSearchTypeEnum.SKU.equals(dto.getSearchType())) {
            //根据指标查询SKU目标值
            List<BiTargetSkuSettingEntity> list = biTargetSkuSettingService.listTargetFinish(dto);
        }
        return null;
    }
}
