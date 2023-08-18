package com.erp.server.oms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.server.oms.mapper.SoB2cDetailMapper;
import com.erp.server.oms.service.SoB2cDetailService;
import com.common.business.service.SuperServiceImpl;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * B2C销售订单明细表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cDetailServiceImpl extends SuperServiceImpl<SoB2cDetailMapper, SoB2cDetailEntity> implements SoB2cDetailService {



}
