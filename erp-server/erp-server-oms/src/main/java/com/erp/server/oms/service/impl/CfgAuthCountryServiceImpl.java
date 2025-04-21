package com.erp.server.oms.service.impl;


import com.common.core.entity.BaseEntity;
import com.erp.model.oms.entity.CfgAuthCountryEntity;
import com.erp.model.oms.entity.CfgAuthRegionEntity;
import com.erp.server.oms.mapper.CfgAuthCountryMapper;
import com.erp.server.oms.service.CfgAuthCountryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.oms.service.CfgAuthRegionService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

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


}
