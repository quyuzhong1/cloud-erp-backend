package com.erp.server.sys.service.impl;


import org.springframework.stereotype.Service;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.sys.entity.SysRefererConfigEntity;
import com.erp.server.sys.mapper.SysRefererConfigMapper;
import com.erp.server.sys.service.SysRefererConfigService;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 第三方系统配置 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-05-06
 */
@Slf4j
@Service
public class SysRefererConfigServiceImpl extends SuperServiceImpl<SysRefererConfigMapper, SysRefererConfigEntity> implements SysRefererConfigService {
    
}
