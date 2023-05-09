package com.erp.server.wms.service.impl;

import com.erp.model.wms.entity.InstockForcastEntity;
import com.erp.server.wms.mapper.InstockForcastMapper;
import com.erp.server.wms.service.InstockForcastService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 入库预报表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-09
 */
@Service
public class InstockForcastServiceImpl extends SuperServiceImpl<InstockForcastMapper, InstockForcastEntity> implements InstockForcastService {

}
