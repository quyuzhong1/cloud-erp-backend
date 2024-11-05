package com.erp.server.dmp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.DmpThirdReturnInboundEntity;
import com.erp.server.dmp.mapper.DmpThirdReturnInboundMapper;
import com.erp.server.dmp.service.DmpThirdReturnInboundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
/**
 * <p>
 * 第三方仓退货入库 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-10-18
 */
@Slf4j
@Service
public class DmpThirdReturnInboundServiceImpl extends SuperServiceImpl<DmpThirdReturnInboundMapper, DmpThirdReturnInboundEntity> implements DmpThirdReturnInboundService {

}
