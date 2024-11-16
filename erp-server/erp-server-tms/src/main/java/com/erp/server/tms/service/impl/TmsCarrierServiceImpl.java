package com.erp.server.tms.service.impl;


import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.constant.SqlConstants;
import com.erp.model.tms.entity.TmsCarrierEntity;
import com.erp.server.tms.mapper.TmsCarrierMapper;
import com.erp.server.tms.service.TmsCarrierService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
                .map(e -> new BaseDropDownDTO.CommonDTO(e.getCode(), e.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public TmsCarrierEntity getByCodeAndSalesPlatform(String carrierCode, String dictPlatform) {
        return lambdaQuery().eq(TmsCarrierEntity::getSalesPlatform, dictPlatform)
                .eq(TmsCarrierEntity::getCode, carrierCode)
                .last(SqlConstants.LIMIT_1)
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkSaveOrUpdateBatch(List<TmsCarrierEntity> list) {
        List<TmsCarrierEntity> alllist = this.list();

        List<TmsCarrierEntity> saveOrUpdate = new LinkedList<>();

        for (TmsCarrierEntity newEntity : list) {
            alllist.stream()
                    .filter(e -> e.getCode().equalsIgnoreCase(newEntity.getCode()) && e.getSalesPlatform().equalsIgnoreCase(newEntity.getSalesPlatform()))
                    .findFirst().ifPresent(oldEntity -> newEntity.setId(oldEntity.getId()));
            saveOrUpdate.add(newEntity);
        }
        this.saveOrUpdateBatch(saveOrUpdate);
    }
}
