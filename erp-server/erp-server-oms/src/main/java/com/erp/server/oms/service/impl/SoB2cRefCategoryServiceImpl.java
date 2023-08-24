package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.oms.entity.SoB2cRefCategoryEntity;
import com.erp.server.oms.mapper.SoB2cRefCategoryMapper;
import com.erp.server.oms.service.SoB2cRefCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

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


    @Override
    public List<SoB2cRefCategoryEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SoB2cRefCategoryEntity::getSoB2cId,mainIds).list();
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        return lambdaUpdate().in(SoB2cRefCategoryEntity::getSoB2cId,mainIds).remove();
    }
}
