package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.wms.entity.FbaShipmentStatusEntity;
import com.erp.server.wms.mapper.FbaShipmentStatusMapper;
import com.erp.server.wms.service.FbaShipmentStatusService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FbaShipmentStatusDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * FBA货件状态信息 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
 */
@Slf4j
@Service
public class FbaShipmentStatusServiceImpl extends SuperServiceImpl<FbaShipmentStatusMapper, FbaShipmentStatusEntity> implements FbaShipmentStatusService {

}
