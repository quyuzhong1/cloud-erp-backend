package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpThirdInboundDTO;
import com.erp.model.dmp.entity.DmpThirdInboundEntity;
import com.erp.model.dmp.entity.DmpThirdInboundListEntity;
import com.erp.server.dmp.mapper.DmpThirdInboundListMapper;
import com.erp.server.dmp.mapper.DmpThirdInboundMapper;
import com.erp.server.dmp.service.DmpThirdInboundListService;
import com.erp.server.dmp.service.DmpThirdInboundService;
import com.erp.server.dmp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * <p>
 * 第三方仓库存 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-08-08
 */
@Slf4j
@Service
public class DmpThirdInboundListServiceImpl extends SuperServiceImpl<DmpThirdInboundListMapper, DmpThirdInboundListEntity> implements DmpThirdInboundListService {

}