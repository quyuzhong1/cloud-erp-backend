package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.oms.entity.CfgAuthCountryEntity;
import com.erp.model.oms.entity.CfgAuthRegionEntity;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.server.oms.mapper.CfgAuthCountryMapper;
import com.erp.server.oms.service.CfgAuthCountryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.CfgAuthRegionService;
import com.erp.server.oms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.CfgAuthDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 授权国家配置 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-04-21
 */
@Slf4j
@Service
public class CfgAuthCountryServiceImpl extends SuperServiceImpl<CfgAuthCountryMapper, CfgAuthCountryEntity> implements CfgAuthCountryService {

    @Resource
    private CfgAuthRegionService cfgAuthRegionService;

    @Override
    public List<CfgAuthDTO.ViewDTO> listByDictPlatform(String dictPlatform) {
        List<CfgAuthRegionEntity> list = cfgAuthRegionService.lambdaQuery()
                .eq(CfgAuthRegionEntity::getRegion, dictPlatform)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<String> mainIds = list.stream().map(BaseEntity::getId).collect(Collectors.toList());
        Map<String, List<CfgAuthCountryEntity>> countryMap = lambdaQuery()
                .eq(CfgAuthCountryEntity::getMainId, mainIds)
                .list()
                .stream()
                .collect(Collectors.groupingBy(CfgAuthCountryEntity::getMainId));
        if (countryMap.isEmpty()) {
            return Collections.emptyList();
        }
        return fillData(list, countryMap);
    }

    private List<CfgAuthDTO.ViewDTO> fillData(List<CfgAuthRegionEntity> list, Map<String, List<CfgAuthCountryEntity>> countryMap) {
        return list.stream()
                .map(e-> new CfgAuthDTO.ViewDTO(e, countryMap.getOrDefault(e.getId(), Collections.emptyList())))
                .sorted(Comparator.comparing(CfgAuthDTO.ViewDTO::getIndex))
                .collect(Collectors.toList());

    }
}
