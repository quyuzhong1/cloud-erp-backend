package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.server.wms.mapper.PickingDetailMapper;
import com.erp.server.wms.service.PickingDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 拣货明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-11
 */
@Service
public class PickingDetailServiceImpl extends SuperServiceImpl<PickingDetailMapper, PickingDetailEntity> implements PickingDetailService {

}
