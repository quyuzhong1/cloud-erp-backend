package com.erp.server.sys.service.impl;

import com.erp.model.sys.entity.CfgUserRangeEntity;
import com.erp.server.sys.mapper.CfgUserRangeMapper;
import com.erp.server.sys.service.CfgUserRangeService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 用户区间配置表 服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-12
 */
@Slf4j
@Service
public class CfgUserRangeServiceImpl extends SuperServiceImpl<CfgUserRangeMapper, CfgUserRangeEntity> implements CfgUserRangeService {



}
