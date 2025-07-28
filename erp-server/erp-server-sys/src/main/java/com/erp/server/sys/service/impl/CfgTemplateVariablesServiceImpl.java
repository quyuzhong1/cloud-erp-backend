package com.erp.server.sys.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.sys.entity.CfgTemplateVariablesEntity;
import com.erp.server.sys.mapper.CfgTemplateVariablesMapper;
import com.erp.server.sys.service.CfgTemplateVariablesService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.sys.service.OperateLogService;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.CfgTemplateVariablesDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 模板字段表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-07-24
 */
@Slf4j
@Service
public class CfgTemplateVariablesServiceImpl extends SuperServiceImpl<CfgTemplateVariablesMapper, CfgTemplateVariablesEntity> implements CfgTemplateVariablesService {

    @Override
    public List<CfgTemplateVariablesDTO.VariableGroupDTO> listByTemplateType(CfgTemplateVariablesDTO.TemplateParamDTO dto) {
        return Collections.emptyList();
    }


}
