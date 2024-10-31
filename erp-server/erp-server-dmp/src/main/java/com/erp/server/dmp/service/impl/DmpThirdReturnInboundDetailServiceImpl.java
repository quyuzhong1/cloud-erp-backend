package com.erp.server.dmp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.DmpThirdReturnInboundDetailEntity;
import com.erp.server.dmp.mapper.DmpThirdReturnInboundDetailMapper;
import com.erp.server.dmp.service.DmpThirdReturnInboundDetailService;
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
public class DmpThirdReturnInboundDetailServiceImpl extends SuperServiceImpl<DmpThirdReturnInboundDetailMapper, DmpThirdReturnInboundDetailEntity> implements DmpThirdReturnInboundDetailService {

}
