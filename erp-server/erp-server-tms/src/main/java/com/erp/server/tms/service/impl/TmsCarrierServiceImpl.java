package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.TmsCarrierEntity;
import com.erp.server.tms.mapper.TmsCarrierMapper;
import com.erp.server.tms.service.TmsCarrierService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TmsCarrierDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 承运商 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-07-04
 */
@Slf4j
@Service
public class TmsCarrierServiceImpl extends SuperServiceImpl<TmsCarrierMapper, TmsCarrierEntity> implements TmsCarrierService {
    @Override
    public List<BaseDropDownDTO.CommonDTO> listBySalesPlatform(String salesPlatform) {
        List<TmsCarrierEntity> list = lambdaQuery()
                .eq(StringUtils.isNotBlank(salesPlatform), TmsCarrierEntity::getSalesPlatform, salesPlatform)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(e-> new BaseDropDownDTO.CommonDTO(e.getCode(), e.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public TmsCarrierEntity getByCodeAndSalesPlatform(String carrierCode, String dictPlatform) {
        return lambdaQuery().eq(TmsCarrierEntity::getSalesPlatform, dictPlatform)
                .eq(TmsCarrierEntity::getCode, carrierCode)
                .last("LIMIT 1")
                .one();
    }
}
