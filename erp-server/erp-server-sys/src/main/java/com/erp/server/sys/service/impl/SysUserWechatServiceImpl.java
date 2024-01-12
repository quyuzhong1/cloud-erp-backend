package com.erp.server.sys.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.sys.entity.SysUserWechatEntity;
import com.erp.server.sys.mapper.SysUserWechatMapper;
import com.erp.server.sys.service.CommonService;
import com.erp.server.sys.service.SysUserWechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * <p>
 * 微信用户表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
 */
@Slf4j
@Service
public class SysUserWechatServiceImpl extends SuperServiceImpl<SysUserWechatMapper, SysUserWechatEntity> implements SysUserWechatService {

    @Autowired
    private CommonService commonService;

}
