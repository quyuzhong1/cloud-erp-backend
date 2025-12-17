package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.ShippingRegionCityEntity;
import com.erp.model.tms.entity.ShippingTemplateCostSettingEntity;
import com.erp.model.tms.entity.ShippingTemplateOtherCostEntity;
import com.erp.server.tms.mapper.ShippingTemplateCostSettingMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.ShippingTemplateCostSettingDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 运费模板其他费用选值表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
@Slf4j
@Service
public class ShippingTemplateCostSettingServiceImpl extends SuperServiceImpl<ShippingTemplateCostSettingMapper, ShippingTemplateCostSettingEntity> implements ShippingTemplateCostSettingService {

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(List<ShippingTemplateCostSettingDTO.AddDTO> list,List<String> otherCostIdList) {

        //删除原有计算方式
        deleteByOtherCostIdList(otherCostIdList);

        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }

        List<ShippingTemplateCostSettingEntity> costSettingList = BeanMapperUtils.copyList(ShippingTemplateCostSettingEntity.class, list);

        log.info("开始新增其他费用计算方式选值");
        //数据格式化
        handleData(costSettingList);

        //新增计算方式
        boolean save = this.saveBatch(costSettingList);
        if(!save) {
            throw new ServiceException("其他费用计算方式选值");
        }
        return save;
    }

    @Override
    public List<ShippingTemplateCostSettingEntity> listByOtherCostIds(List<String> otherCostIdList) {
        if (CollectionUtils.isEmpty(otherCostIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(ShippingTemplateCostSettingEntity::getOtherCostId,otherCostIdList).list();
    }

    @Override
    public void deleteByOtherCostIds(List<String> otherCostIdList) {
        lambdaUpdate().in(ShippingTemplateCostSettingEntity::getOtherCostId,otherCostIdList).remove();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<ShippingTemplateCostSettingEntity> costSettingList) {

    }

    /**
     * @description: 根据主表id删除
     * @author Will
     * @date: 2023/11/8 9:29
     * @param otherCostIdList
     */
    private void deleteByOtherCostIdList (List<String> otherCostIdList) {
        lambdaUpdate().in(ShippingTemplateCostSettingEntity::getOtherCostId,otherCostIdList).remove();
    }
}
