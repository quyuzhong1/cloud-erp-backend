package com.erp.server.mrp.service.impl;

import com.erp.model.mrp.entity.SalesInfoEntity;
import com.erp.server.mrp.mapper.SalesInfoMapper;
import com.erp.server.mrp.service.SalesInfoService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 历史销量信息 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class SalesInfoServiceImpl extends SuperServiceImpl<SalesInfoMapper, SalesInfoEntity> implements SalesInfoService {

}
