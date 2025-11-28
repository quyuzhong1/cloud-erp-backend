package com.erp.server.dmp.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.doris.DwdFirstMileShipmentChangeFEntity;
import com.erp.server.dmp.mapper.doris.DwdFirstMileShipmentChangeFMapper;
import com.erp.server.dmp.service.DwdFirstMileShipmentChangeFService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * DWD头程发货签收变更记录(包含期初/调整) 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-11-28
 */
@Slf4j
@Service
public class DwdFirstMileShipmentChangeFServiceImpl extends SuperServiceImpl<DwdFirstMileShipmentChangeFMapper, DwdFirstMileShipmentChangeFEntity> implements DwdFirstMileShipmentChangeFService {

}
