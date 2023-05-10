package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.server.oms.mapper.SoInfoMapper;
import com.erp.server.oms.service.SoInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 销售订单信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class SoInfoServiceImpl extends SuperServiceImpl<SoInfoMapper, SoInfoEntity> implements SoInfoService {

}
