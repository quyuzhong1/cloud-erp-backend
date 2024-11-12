package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaCalcEntity;
import com.erp.server.mrp.mapper.CfgRuleSalesFormulaCalcMapper;
import com.erp.server.mrp.service.CfgRuleSalesFormulaCalcService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.mrp.service.OperateLogService;
import com.erp.server.mrp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaCalcDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 试算销量公式（规则设置） 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@Service
public class CfgRuleSalesFormulaCalcServiceImpl extends SuperServiceImpl<CfgRuleSalesFormulaCalcMapper, CfgRuleSalesFormulaCalcEntity> implements CfgRuleSalesFormulaCalcService {

}
