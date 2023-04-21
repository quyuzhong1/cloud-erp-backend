package com.erp.server.sys.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.sys.entity.MsgConfig;
import com.erp.server.sys.mapper.MsgConfigMapper;
import com.erp.server.sys.service.MsgConfigService;
import org.springframework.stereotype.Service;

/**
 * @Classname: MsgConfigServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-21  12:01
 * @Author: zhangchunlin
 */
@Service
public class MsgConfigServiceImpl extends SuperServiceImpl<MsgConfigMapper, MsgConfig> implements MsgConfigService {
}