package com.erp.server.oms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.erp.model.oms.entity.SoB2cRefCategoryEntity;
import com.erp.server.oms.mapper.SoB2cRefCategoryMapper;
import com.erp.server.oms.service.SoB2cRefCategoryService;
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
 * B2C销售订单分类表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cRefCategoryServiceImpl extends SuperServiceImpl<SoB2cRefCategoryMapper, SoB2cRefCategoryEntity> implements SoB2cRefCategoryService {



}
