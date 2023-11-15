package com.erp.server.tms.service.impl;


import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.ShippingRegionCityDTO;
import com.erp.model.tms.entity.ShippingRegionCityEntity;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.erp.model.tms.entity.ShippingTemplateOtherCostEntity;
import com.erp.model.tms.entity.ShippingTemplateRuleEntity;
import com.erp.model.tms.enums.ShippingBillingMethodEnum;
import com.erp.model.tms.enums.ShippingTemplateTypeEnum;
import com.erp.server.tms.mapper.ShippingTemplateRuleMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.ShippingTemplateRuleDTO;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 运费模板渠道关联表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
@Slf4j
@Service
public class ShippingTemplateRuleServiceImpl extends SuperServiceImpl<ShippingTemplateRuleMapper, ShippingTemplateRuleEntity> implements ShippingTemplateRuleService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private ShippingTemplateService shippingTemplateService;

    @Autowired
    private ShippingRegionCityService shippingRegionCityService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(List<ShippingTemplateRuleDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return Boolean.TRUE;
        }
        List<ShippingTemplateRuleEntity> list = BeanMapperUtils.copyList(ShippingTemplateRuleEntity.class, detailList);
        //验证必填信息
        checkPurchasePrice(list,mainId);
        boolean save = this.saveBatch(list);
        if(!save) {
            throw new ServiceException("运费规则单保存失败");
        }
        //新增城市分区
        addOrUpdateShippingRegionCity(list);
        return Boolean.TRUE;
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<ShippingTemplateRuleDTO.UpdateDTO> detailList, String mainId) {
        if (detailList == null) {
            detailList = new ArrayList<>();
        }
        List<ShippingTemplateRuleEntity> list = BeanMapperUtils.copyList(ShippingTemplateRuleEntity.class, detailList);
        //验证必填信息
        checkPurchasePrice(list,mainId);
        //原明细数据
        List<ShippingTemplateRuleEntity> oldList = this.listByMainId(mainId);
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<ShippingTemplateRuleEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getFromCountry())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个起始地区间【%s】", ModuleTypeEnum.SHIPPING_TEMPLATE.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        //新增城市分区
        addOrUpdateShippingRegionCity(list);
        //处理明细id及操作日志
        doOpHandleDetails(list,mainId,Boolean.FALSE);
        //新增或修改
        this.saveOrUpdateBatch(list);
        return Boolean.TRUE;
    }

    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<ShippingTemplateRuleEntity> newList, String shippingTemplateId,Boolean isAdd) {
        //添加操作日志
        List<ShippingTemplateRuleEntity> addList = newList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //新增日志
        if (CollectionUtils.isNotEmpty(addList) && !isAdd) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(shippingTemplateId, obj.getFromCountry())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条模板规则【%s】", ModuleTypeEnum.SHIPPING_TEMPLATE.getCode(), addPairList, "编辑操作");
        }
        //规则id集合
        List<String> ruleIdList = newList.stream().map(ShippingTemplateRuleEntity::getId).collect(Collectors.toList());
        List<ShippingRegionCityEntity> shippingRegionCityList = shippingRegionCityService.listByRuleIdList(ruleIdList);

        for (ShippingTemplateRuleEntity entity : newList) {
            //操作日志
            if (StringUtils.isNotBlank(entity.getId())) {
                //原城市
                List<String> oldCityList = shippingRegionCityList.stream().filter(obj -> obj.getMainId().equals(entity.getId())).map(ShippingRegionCityEntity::getCity).collect(Collectors.toList());

                ShippingTemplateRuleEntity old = this.getById(entity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_SHIPPING_RULE_NOT_EXIST);
                }
                old.setCityList(oldCityList);
                operateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.SHIPPING_TEMPLATE.getCode(),shippingTemplateId,"",String.format("【%s】",old.getFromCountry()));
            }
        }
    }

    @Override
    public List<ShippingTemplateRuleEntity> listByMainId(String mainId) {
      return   lambdaQuery().eq(ShippingTemplateRuleEntity::getMainId,mainId).list();
    }

    @Override
    public void deleteByMainId(String mainId) {
        List<ShippingTemplateRuleEntity> list = listByMainId(mainId);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //删除分区城市数据
        List<String> ids = list.stream().map(ShippingTemplateRuleEntity::getId).collect(Collectors.toList());
        shippingRegionCityService.deleteByRuleIdList(ids);
        //删除分区信息
        this.removeByIds(ids);
    }

    @Override
    public ShippingTemplateRuleEntity getShippingTemplateRule(ShippingTemplateRuleDTO.ViewParamDTO viewParamDTO) {
        return lambdaQuery()
                .eq(StringUtils.isNotEmpty(viewParamDTO.getFromCountry()),ShippingTemplateRuleEntity::getFromCountry,viewParamDTO.getFromCountry())
                .eq(StringUtils.isNotEmpty(viewParamDTO.getToCountry()),ShippingTemplateRuleEntity::getToCountry,viewParamDTO.getToCountry())
                .eq(StringUtils.isNotEmpty(viewParamDTO.getRegion()),ShippingTemplateRuleEntity::getRegion,viewParamDTO.getRegion())
                .eq(StringUtils.isNotEmpty(viewParamDTO.getToWarehouseName()),ShippingTemplateRuleEntity::getToWarehouseName,viewParamDTO.getToWarehouseName())
                .eq(StringUtils.isNotEmpty(viewParamDTO.getMainId()),ShippingTemplateRuleEntity::getMainId,viewParamDTO.getMainId())
                .gt(ShippingTemplateRuleEntity::getEndWeight,viewParamDTO.getWeight())
                .le(ShippingTemplateRuleEntity::getStartWeight,viewParamDTO.getWeight())
                .last("limit 1")
                .one();
    }


    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<ShippingTemplateRuleEntity> newList, List<ShippingTemplateRuleEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(ShippingTemplateRuleEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(ShippingTemplateRuleEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * @description: 验证信息
     * @author Will
     * @date: 2023/11/7 12:21
     * @param detailList
     * @param mainId
     */
    private void checkPurchasePrice(List<ShippingTemplateRuleEntity> detailList,String mainId) {
        ShippingTemplateEntity entity = shippingTemplateService.getById(mainId);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_TEMPLATE_NOT_EXIST);
        }

        //重量验证
        long weightCount = detailList.stream().filter(obj -> MathUtil.compareTo(obj.getStartWeight(), obj.getEndWeight()) > MathUtil.ZERO).count();
        if (weightCount > 0) {
            throw new ServiceException(ApiError.ERROR_RULE_WEIGHT_COMPARE);
        }

        //按国家
        if (ShippingTemplateTypeEnum.ENUM_COUNTRY.getCode().equals(entity.getType())) {
            //必填校验
            long count = detailList.stream().filter(obj -> StrUtil.isBlank(obj.getToCountry())).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_SHIPPING_TO_COUNTRY_NOT_NUll);
            }
            Map<String, List<ShippingTemplateRuleEntity>> map = detailList.stream().collect(Collectors.groupingBy(obj -> obj.getFromCountry().concat(obj.getToCountry())));
            for (Map.Entry<String, List<ShippingTemplateRuleEntity>> entry : map.entrySet()) {
                List<ShippingTemplateRuleEntity> value = entry.getValue();
                if (value.size() > 1) {

                    throw new ServiceException(ApiError.ERROR_SHIPPING_TEMPLATE_RULE_COUNTRY_EXIST,value.get(0).getFromCountry(),value.get(0).getToCountry());
                }
            }
        }
        //按分区
        if (ShippingTemplateTypeEnum.ENUM_REGION.getCode().equals(entity.getType())) {
            //必填校验
            long regionCount = detailList.stream().filter(obj -> StrUtil.isBlank(obj.getRegion())).count();
            if (regionCount > 0) {
                throw new ServiceException(ApiError.ERROR_SHIPPING_REGION_NOT_NULL);
            }
            long cityCount = detailList.stream().filter(obj -> CollectionUtils.isEmpty(obj.getCityList())).count();
            if (cityCount > 0) {
                throw new ServiceException(ApiError.ERROR_SHIPPING_CITY_NOT_NULL);
            }

            Map<String, List<ShippingTemplateRuleEntity>> map = detailList.stream().collect(Collectors.groupingBy(obj -> obj.getFromCountry().concat(obj.getRegion())));
            for (Map.Entry<String, List<ShippingTemplateRuleEntity>> entry : map.entrySet()) {
                List<ShippingTemplateRuleEntity> value = entry.getValue();
                if (value.size() > 1) {
                    throw new ServiceException(ApiError.ERROR_SHIPPING_TEMPLATE_RULE_REGION_EXIST,value.get(0).getFromCountry(),value.get(0).getRegion());
                }
            }
        }
        //按仓库
        if (ShippingTemplateTypeEnum.ENUM_WAREHOUSE.getCode().equals(entity.getType())) {
            Map<String, List<ShippingTemplateRuleEntity>> map = detailList.stream().collect(Collectors.groupingBy(obj -> obj.getFromCountry().concat(obj.getToWarehouseName())));
            //必填校验
            long count = detailList.stream().filter(obj -> StrUtil.isBlank(obj.getToWarehouseName())).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_SHIPPING_WAREHOUSE_NOT_NULL);
            }
            for (Map.Entry<String, List<ShippingTemplateRuleEntity>> entry : map.entrySet()) {
                List<ShippingTemplateRuleEntity> value = entry.getValue();
                if (value.size() > 1) {
                    throw new ServiceException(ApiError.ERROR_SHIPPING_TEMPLATE_RULE_WAREHOUSE_EXIST,value.get(0).getFromCountry(),value.get(0).getToWarehouseName());
                }
            }
        }
        //首重+续重必填校验
        if (ShippingBillingMethodEnum.ENUM_SEVERAL_WEIGHT.getCode().equals(entity.getBillingMethod())) {
            //首重
            long firstWeightCount = detailList.stream().filter(obj -> ObjectUtil.isEmpty(obj.getFirstWeight())).count();
            if (firstWeightCount > 0) {
                throw new ServiceException(ApiError.ERROR_FIRST_WEIGHT_NOT_NULL);
            }
            //首重运费
            long firstWeightShippingCostCount = detailList.stream().filter(obj -> ObjectUtil.isEmpty(obj.getFirstWeightShippingCost())).count();
            if (firstWeightShippingCostCount > 0) {
                throw new ServiceException(ApiError.ERROR_FIRST_WEIGHT_SHIPPING_COST_NOT_NULL);
            }
            //续重单价重量
            long additionalUnitWeightCount = detailList.stream().filter(obj -> ObjectUtil.isEmpty(obj.getAdditionalUnitWeight())).count();
            if (additionalUnitWeightCount > 0) {
                throw new ServiceException(ApiError.ERROR_ADDITIONAL_UNIT_WEIGHT_NOT_NULL);
            }
            //续重单价
            long additionalPriceCount = detailList.stream().filter(obj -> ObjectUtil.isEmpty(obj.getAdditionalPrice())).count();
            if (additionalPriceCount > 0) {
                throw new ServiceException(ApiError.ERROR_ADDITIONAL_PRICE_NOT_NULL);
            }
        }
        //重量段必填校验
        if (ShippingBillingMethodEnum.ENUM_WEIGHT_SEGMENT.getCode().equals(entity.getBillingMethod())) {
            //运费单价
            long shippingPriceCount = detailList.stream().filter(obj -> ObjectUtil.isEmpty(obj.getShippingPrice())).count();
            if (shippingPriceCount > 0) {
                throw new ServiceException(ApiError.ERROR_SHIPPING_PRICE_NOT_NULL);
            }
        }

        //数据赋值
        for (ShippingTemplateRuleEntity ruleEntity : detailList) {
            if (ShippingBillingMethodEnum.ENUM_SEVERAL_WEIGHT.getCode().equals(entity.getBillingMethod())) {
                ruleEntity.setShippingPrice(BigDecimal.ZERO);
            } else {
                ruleEntity.setFirstWeight(BigDecimal.ZERO);
                ruleEntity.setFirstWeightShippingCost(BigDecimal.ZERO);
                ruleEntity.setAdditionalUnitWeight(BigDecimal.ZERO);
                ruleEntity.setAdditionalPrice(BigDecimal.ZERO);
            }
            ruleEntity.setMainId(mainId);
        }
    }

    /**
     * @description: 新增分区城市
     * @author Will
     * @date: 2023/11/8 9:31
     * @param list
     */
    private void addOrUpdateShippingRegionCity(List<ShippingTemplateRuleEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return ;
        }
        List<ShippingRegionCityDTO.AddDTO> addList = new ArrayList<>();
        for (ShippingTemplateRuleEntity ruleEntity : list) {
            if (CollectionUtils.isEmpty(ruleEntity.getCityList())) {
                continue;
            }
            List<String> cityList = ruleEntity.getCityList().stream().distinct().collect(Collectors.toList());
            for (String city : cityList) {
                ShippingRegionCityDTO.AddDTO addDTO = new ShippingRegionCityDTO.AddDTO();
                addDTO.setRegion(ruleEntity.getRegion());
                addDTO.setShippingTemplateRuleId(ruleEntity.getId());
                addDTO.setMainId(ruleEntity.getMainId());
                addDTO.setCity(city);
                addList.add(addDTO);
            }
        }
        if (CollectionUtils.isEmpty(addList)) {
            return;
        }
        shippingRegionCityService.add(addList);
    }
}
