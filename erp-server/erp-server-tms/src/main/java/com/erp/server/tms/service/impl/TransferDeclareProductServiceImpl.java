package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.TransferDeclareProductDTO;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.entity.TransferDeclareProductEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.mapper.TransferDeclareProductMapper;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TransferDeclareProductService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 中转报关产品 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-01-27
 */
@Slf4j
@Service
public class TransferDeclareProductServiceImpl extends SuperServiceImpl<TransferDeclareProductMapper, TransferDeclareProductEntity> implements TransferDeclareProductService {
}
