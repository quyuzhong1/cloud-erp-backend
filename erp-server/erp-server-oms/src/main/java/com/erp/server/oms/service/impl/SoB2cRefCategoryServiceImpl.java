package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cRefCategoryDTO;
import com.erp.model.oms.entity.OrderCategoryDetailEntity;
import com.erp.model.oms.entity.SoB2cRefCategoryEntity;
import com.erp.server.oms.mapper.SoB2cRefCategoryMapper;
import com.erp.server.oms.service.OrderCategoryDetailService;
import com.erp.server.oms.service.SoB2cRefCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    @Resource
    private OrderCategoryDetailService orderCategoryDetailService;
    
    @Override
    public Boolean add(List<SoB2cRefCategoryDTO.AddDTO> addList, String mainId) {
        if (CollectionUtils.isEmpty(addList)) {
            return Boolean.TRUE;
        }
        List<SoB2cRefCategoryEntity> soB2cRefCategoryList = BeanMapperUtils.copyList(SoB2cRefCategoryEntity.class, addList);
        handleCategory(soB2cRefCategoryList);

        return this.saveOrUpdateBatch(soB2cRefCategoryList);
    }

    private void handleCategory (List<SoB2cRefCategoryEntity> soB2cRefCategoryList) {
        if (CollectionUtils.isEmpty(soB2cRefCategoryList)) {
            return;
        }
        List<String> categoryIdList = soB2cRefCategoryList.stream().map(SoB2cRefCategoryEntity::getCategoryId).collect(Collectors.toList());
        List<OrderCategoryDetailEntity> orderCategoryDetailList = orderCategoryDetailService.listByIds(categoryIdList);

        for (SoB2cRefCategoryEntity entity : soB2cRefCategoryList) {
        }
    }


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
