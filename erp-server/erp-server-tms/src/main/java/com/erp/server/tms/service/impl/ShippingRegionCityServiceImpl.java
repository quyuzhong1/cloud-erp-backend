package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.ShippingRegionCityEntity;
import com.erp.server.tms.mapper.ShippingRegionCityMapper;
import com.erp.server.tms.service.ShippingRegionCityService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.ShippingRegionCityDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 运费规则分区城市表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-11-07
 */
@Slf4j
@Service
public class ShippingRegionCityServiceImpl extends SuperServiceImpl<ShippingRegionCityMapper, ShippingRegionCityEntity> implements ShippingRegionCityService {

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(List<ShippingRegionCityDTO.AddDTO> list) {
        List<ShippingRegionCityEntity> shippingRegionCityList = BeanMapperUtils.copyList(ShippingRegionCityEntity.class, list);

        log.info("开始新增运费规则分区城市单");

        //删除原有城市
        List<String> mainIdList = shippingRegionCityList.stream().map(ShippingRegionCityEntity::getMainId).distinct().collect(Collectors.toList());
        deleteByMainIdList(mainIdList);
        //新增城市
        boolean save = this.saveBatch(shippingRegionCityList);
        if(!save) {
            throw new ServiceException("运费规则分区城市单保存失败");
        }
        return save;
    }

    @Override
    public List<ShippingRegionCityEntity> listByRuleId(String shippingTemplateRuleId) {
        return lambdaQuery().eq(ShippingRegionCityEntity::getShippingTemplateRuleId,shippingTemplateRuleId).list();
    }

    @Override
    public void deleteByRuleIdList(List<String> ruleIdList) {
        if (CollectionUtils.isEmpty(ruleIdList)) {
            return;
        }
        lambdaUpdate().in(ShippingRegionCityEntity::getShippingTemplateRuleId,ruleIdList).remove();
    }

    @Override
    public List<ShippingRegionCityEntity> listByRuleIdList(List<String> ruleIdList) {
        if (CollectionUtils.isEmpty(ruleIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(ShippingRegionCityEntity::getShippingTemplateRuleId,ruleIdList).list();
    }

    /**
     * @description: 根据主表id删除
     * @author Will
     * @date: 2023/11/8 9:29
     * @param mainIdList
     */
    private void deleteByMainIdList (List<String> mainIdList) {
        lambdaUpdate().in(ShippingRegionCityEntity::getMainId,mainIdList).remove();
    }
}
