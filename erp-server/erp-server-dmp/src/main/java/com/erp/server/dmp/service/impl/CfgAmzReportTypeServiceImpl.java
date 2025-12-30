package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.model.dmp.entity.CfgAmzReportTypeEntity;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.server.dmp.mapper.CfgAmzReportTypeMapper;
import com.erp.server.dmp.service.CfgAmzReportTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 亚马逊报告类型配置 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
 */
@Slf4j
@Service
public class CfgAmzReportTypeServiceImpl extends SuperServiceImpl<CfgAmzReportTypeMapper, CfgAmzReportTypeEntity> implements CfgAmzReportTypeService {

    @Override
    public List<CfgAmzReportTypeEntity> findActive(List<String> subscribedTypeList) {
        return this.lambdaQuery()
                .eq(CfgAmzReportTypeEntity::getDisabled, false)
                .in(!CollectionUtils.isEmpty(subscribedTypeList), CfgAmzReportTypeEntity::getSubscribedType, subscribedTypeList)
                .list();
    }

    @Override
    public Map<String, List<CfgAmzReportTypeEntity>> mapByReportGroup() {
        List<CfgAmzReportTypeEntity> list = this.findActive(null);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream()
                .collect(Collectors.groupingBy(CfgAmzReportTypeEntity::getReportGroup));
    }

    @Override
    public CfgAmzReportTypeEntity getByRecordType(String reportType) {
        return lambdaQuery()
                .eq(CfgAmzReportTypeEntity::getReportType, reportType)
                .eq(CfgAmzReportTypeEntity::getDisabled, false)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public boolean checkCountryList(CfgAmzReportTypeEntity config, String marketplace) {
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByMarketplaceId(marketplace);
        if (null == marketplaceEnum) {
            String msg = StrUtil.format("未找到Marketplace枚举类型, marketplaceId={}", marketplace);
            throw new ServiceException(msg);
        }
        return config.getCountryList().contains(marketplaceEnum.getCountryCode());
    }

    @Override
    public CfgAmzReportTypeEntity checkCountryAndGetByRecordType(AmzReportTaskEntity entity) {
        CfgAmzReportTypeEntity config = getByRecordType(entity.getReportType());
        if (null == config) {
            throw new ServiceException("未找到报告类型配置：recordType=" + entity.getReportType());
        }
        boolean allow = this.checkCountryList(config, entity.getMarketplaceIds().split(",")[0]);
        if (allow){
            // 允许执行
            return config;
        } else {
            // 不允许执行返回 null
            return null;
        }
    }
}
