package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoB2cCoreDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.server.oms.service.SoB2cCoreService;
import com.erp.server.oms.service.SoB2cDetailService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * b2c扩展类
 * @author will
 * @date 2025/4/24 20:12
 */
@Slf4j
@Service
public class SoB2cCoreServiceImpl implements SoB2cCoreService {

    @Resource
    private SoB2cService soB2cService;

    @Resource
    private SoB2cDetailService soB2cDetailService;

    @Override
    public List<SoB2cCoreDTO.ListRetryOutstockDTO> listRetryOutstock(BaseIdsDTO.IdsDTO dto) {
        List<SoB2cEntity> soB2cList = soB2cService.listByIds(dto.getIds());
        if (CollUtil.isEmpty(soB2cList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainIds(dto.getIds());
        if (CollUtil.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        Map<String, List<SoB2cDetailEntity>> detailMap = soB2cDetailList.stream().collect(Collectors.groupingBy(SoB2cDetailEntity::getMainId));

        List<SoB2cCoreDTO.ListRetryOutstockDTO> list = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : soB2cList) {
            List<SoB2cDetailEntity> thisDetailList = detailMap.get(soB2cEntity.getId());
            if (CollUtil.isEmpty(thisDetailList)) {
                log.error("未找到销售订单明细，订单号：{}", soB2cEntity.getCode());
                continue;
            }
            for (SoB2cDetailEntity soB2cDetailEntity : thisDetailList) {
                SoB2cCoreDTO.ListRetryOutstockDTO listRetryOutstockDTO = new SoB2cCoreDTO.ListRetryOutstockDTO();

            }




        }

        return Collections.emptyList();
    }

    @Override
    public Boolean retryOutstock(SoB2cCoreDTO.RetryOutstockDTO dto) {
        return null;
    }
}
