package com.erp.server.mrp.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.CfgRuleLogisticsDTO;
import com.erp.model.mrp.dto.CfgRuleLogisticsDetailDTO;
import com.erp.model.mrp.entity.CfgRuleExpireTimeEntity;
import com.erp.model.mrp.entity.CfgRuleLogisticsDetailEntity;
import com.erp.model.mrp.entity.CfgRuleLogisticsEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.ShopAuthTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.server.mrp.mapper.CfgRuleLogisticsMapper;
import com.erp.server.mrp.service.CfgRuleLogisticsDetailService;
import com.erp.server.mrp.service.CfgRuleLogisticsService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 备货物流（规则设置） 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleLogisticsServiceImpl extends SuperServiceImpl<CfgRuleLogisticsMapper, CfgRuleLogisticsEntity> implements CfgRuleLogisticsService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private CfgRuleLogisticsDetailService cfgRuleLogisticsDetailService;


    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheEvict(cacheNames = "cache:mrp:logistics:listByExpireTimeIdList", allEntries = true, beforeInvocation = true)
    public Boolean update(List<CfgRuleLogisticsDTO.UpdateDTO> logisticsList, CfgRuleExpireTimeEntity cfgRuleExpireTime, Boolean isCustom, boolean isOverseas) {
        if (CollectionUtils.isEmpty(logisticsList)) {
            logisticsList = Collections.emptyList();
        }
        List<CfgRuleLogisticsEntity> list = BeanMapperUtils.copyList(CfgRuleLogisticsEntity.class, logisticsList);
        //原物流信息
        List<CfgRuleLogisticsEntity> oldList = listByExpireTimeIdList(Collections.singletonList(cfgRuleExpireTime.getId()),
                isOverseas ? CfgRulePlatformTypeEnum.OVERSEAS.getCode() : CfgRulePlatformTypeEnum.AMAZON.getCode());
        //自定义更新无需删除
        if (Boolean.FALSE.equals(isCustom)) {
            //删除明细
            List<String> deleteIds = getDeleteIds(list, oldList);
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                this.deleteByIdList(deleteIds);
                // 数据处理
                oldList = oldList.stream().filter(obj -> !deleteIds.contains(obj.getId())).collect(Collectors.toList());
            }
        }
        if (CollectionUtils.isEmpty(list)) {
            return  Boolean.TRUE;
        }
        // 数据处理
        handleData(list,cfgRuleExpireTime.getId(),oldList,isCustom);
        log.info("编辑 开始修改时效物流（规则设置）数据，id：【{}】", cfgRuleExpireTime.getId());
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("时效物流（规则设置）保存失败");
        }
        //更新物流明细信息
        list.forEach(obj -> cfgRuleLogisticsDetailService.update(obj.getDetailList(),obj.getId()));

        //店铺
        List<ShopInfoEntity> shopInfoList = isOverseas ? new ArrayList<>() :  FeignQuery.list(ShopInfoEntity.class);

        //仓库
        List<String> warehouseIdList = list.stream().filter(obj -> CollUtil.isNotEmpty(obj.getDetailList())).flatMap(obj -> Stream.of(obj.getDetailList().stream().map(CfgRuleLogisticsDetailDTO.UpdateDTO::getWarehouseId)
                .filter(StrUtil::isNotBlank).toArray(String[]::new))).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = CollectionUtils.isEmpty(warehouseIdList) ? new ArrayList<>() : FeignQuery.getByIds(WarehouseEntity.class, warehouseIdList);

        //日志
        StringBuilder msg = getMsg(list, shopInfoList, isOverseas, warehouseList);
        operateLogService.addModuleOperateLog(msg.toString(), ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), CharSequenceUtil.blankToDefault(cfgRuleExpireTime.getRefId(),cfgRuleExpireTime.getId()) , "备货");
        return Boolean.TRUE;
    }

    /**
     * 获取日志
     * @param list 参数
     * @param shopInfoList 参数
     */
    private static StringBuilder getMsg(List<CfgRuleLogisticsEntity> list, List<ShopInfoEntity> shopInfoList,
                                        boolean isOverseas, List<WarehouseEntity> warehouseList) {
        StringBuilder msg = new StringBuilder();
        //日志
        String title = "本地发FBA：";
        if (isOverseas) {
            title = "本地发海外：";
        }
        msg.append( StrUtil.format("{}<br>",title));

        for (CfgRuleLogisticsEntity logisticsEntity : list) {
            String parentMsg = CharSequenceUtil.format("物流方式【{}】、物流时效【{}】、发货频率【{}】<br>",LogisticsMethodEnum.getName(logisticsEntity.getLogisticsMethod()),logisticsEntity.getLogisticsDays(),logisticsEntity.getLogisticsCycleDays());
            msg.append(parentMsg);
            List<CfgRuleLogisticsDetailDTO.UpdateDTO> detailList = logisticsEntity.getDetailList();
            if (CollectionUtils.isEmpty(detailList)) {
                continue;
            }
            for (CfgRuleLogisticsDetailDTO.UpdateDTO updateDTO : detailList) {
                if (!isOverseas) {
                    //非海外仓
                    String shopNames = CollectionUtils.isEmpty(updateDTO.getShopIdList()) ? "" : shopInfoList.stream().filter(obj -> updateDTO.getShopIdList().contains(obj.getId())).map(ShopInfoEntity::getName).distinct().collect(Collectors.joining(","));
                    String childMsg = CharSequenceUtil.format("•区域【{}】、店铺【{}】、时效【{}】<br>", updateDTO.getArea(),StrUtil.equals(ShopAuthTypeEnum.ENUM_ALL.getCode(),updateDTO.getType()) ? "全部店铺": shopNames, updateDTO.getLogisticsDays());
                    msg.append(childMsg);
                } else {
                    String warehouseName = warehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), updateDTO.getWarehouseId())).map(WarehouseEntity::getName).findFirst().orElse("");
                    //海外仓
                    String childMsg = CharSequenceUtil.format("•海外仓【{}】、时效【{}】<br>",warehouseName, updateDTO.getLogisticsDays());
                    msg.append(childMsg);
                }

                String shopNames = CollectionUtils.isEmpty(shopInfoList) ? "" : shopInfoList.stream().filter(obj -> updateDTO.getShopIdList().contains(obj.getId())).map(ShopInfoEntity::getName).distinct().collect(Collectors.joining(","));
                String childMsg = CharSequenceUtil.format("•区域【{}】、店铺【{}】、时效【{}】<br>", updateDTO.getArea(), CharSequenceUtil.equals(ShopAuthTypeEnum.ENUM_ALL.getCode(),updateDTO.getType()) ? "全部店铺": shopNames, updateDTO.getLogisticsDays());
                msg.append(childMsg);
            }
        }
        return msg;
    }

    @Override
    @Cacheable(cacheNames = "cache:mrp:logistics:listByExpireTimeIdList",keyGenerator = "myKeyGenerator")
    public List<CfgRuleLogisticsEntity> listByExpireTimeIdList (List<String> expireTimeIdList) {
        if (CollectionUtils.isEmpty(expireTimeIdList)) {
            return Collections.emptyList();
        }
       return lambdaQuery().in(CfgRuleLogisticsEntity::getExpireTimeId,expireTimeIdList).orderByAsc(CfgRuleLogisticsEntity::getIndex).list();
    }

    public List<CfgRuleLogisticsEntity> listByExpireTimeIdList (List<String> expireTimeIdList, String platformType) {
        if (CollectionUtils.isEmpty(expireTimeIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(CfgRuleLogisticsEntity::getExpireTimeId,expireTimeIdList)
                .eq(CfgRuleLogisticsEntity::getPlatformType, platformType)
                .orderByAsc(CfgRuleLogisticsEntity::getIndex).list();
    }

    @Override
    public List<CfgRuleLogisticsDTO.ViewDTO> listViewByExpireTimeIdList (List<String> expireTimeIdList) {
        if (CollectionUtils.isEmpty(expireTimeIdList)) {
            return Collections.emptyList();
        }
        List<CfgRuleLogisticsEntity> list = lambdaQuery().in(CfgRuleLogisticsEntity::getExpireTimeId, expireTimeIdList).orderByAsc(CfgRuleLogisticsEntity::getIndex).list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<CfgRuleLogisticsDTO.ViewDTO> cfgLogisticsViewList = BeanMapperUtils.copyList(CfgRuleLogisticsDTO.ViewDTO.class, list);
        //物流配置明细
        List<String> mainIdList = cfgLogisticsViewList.stream().map(CfgRuleLogisticsDTO.ViewDTO::getId).distinct().collect(Collectors.toList());
        List<CfgRuleLogisticsDetailEntity> cfgRuleLogisticsDetailList = cfgRuleLogisticsDetailService.listByMainIdList(mainIdList);
        for (CfgRuleLogisticsDTO.ViewDTO viewDTO : cfgLogisticsViewList) {
            List<CfgRuleLogisticsDetailEntity> detailList = cfgRuleLogisticsDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(), viewDTO.getId())).collect(Collectors.toList());
            viewDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(viewDTO.getLogisticsMethod()));
            if (CollectionUtils.isNotEmpty(detailList)) {
                List<CfgRuleLogisticsDetailDTO.ViewDTO> cfgLogisticsDetailViewList = BeanMapperUtils.copyList(CfgRuleLogisticsDetailDTO.ViewDTO.class, detailList);
                for (CfgRuleLogisticsDetailDTO.ViewDTO detailViewDTO : cfgLogisticsDetailViewList) {
                    List<String> shopIdList = JSONUtil.parseArray(detailViewDTO.getShopIdJson()).stream().filter(ObjectUtil::isNotEmpty)
                            .map(Object::toString).collect(Collectors.toList());
                    detailViewDTO.setShopIdList(shopIdList);
                }
                viewDTO.setDetailList(cfgLogisticsDetailViewList);
            }
        }
        return cfgLogisticsViewList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByExpireTimeId(String expireTimeId) {
        List<CfgRuleLogisticsEntity> cfgRuleLogisticsList = listByExpireTimeIdList(Collections.singletonList(expireTimeId));
        if (CollectionUtils.isEmpty(cfgRuleLogisticsList)) {
            return;
        }
        //根据id删除
        List<String> idList = cfgRuleLogisticsList.stream().map(CfgRuleLogisticsEntity::getId).distinct().collect(Collectors.toList());
        deleteByIdList(idList);
    }

    @Override
    public List<CfgRuleLogisticsDTO.SelectLogisticsDTO> selectLogistics(CfgRuleLogisticsDTO.SelectLogisticsParamDTO paramDTO) {
        List<CfgRuleLogisticsDTO.SelectLogisticsDTO> list = baseMapper.selectLogistics(paramDTO);
        handleSelectLogistics(list);
        return list;
    }

    /**
     * 处理下拉物流信息
     * @author will
     * @date 2024/10/30 11:38
     * @param list
     */
    private void handleSelectLogistics(List<CfgRuleLogisticsDTO.SelectLogisticsDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (CfgRuleLogisticsDTO.SelectLogisticsDTO selectLogisticsDTO : list) {
            //物流方式名称
            selectLogisticsDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(selectLogisticsDTO.getLogisticsMethod()));
        }
    }

    /**
     * 根据id删除
     * @author will
     * @date 2024/8/27 11:37
     * @param idList
     */
    private void deleteByIdList (List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        //删除物流信息
        this.removeByIds(idList);

        //删除物流明细数据
        cfgRuleLogisticsDetailService.deleteByMainIdList(idList);
    }


    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<CfgRuleLogisticsEntity> newList, List<CfgRuleLogisticsEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CfgRuleLogisticsEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(CfgRuleLogisticsEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<CfgRuleLogisticsEntity> list,String expireTimeId,List<CfgRuleLogisticsEntity> oldList,Boolean isCustom) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        String names = list.stream().collect(Collectors.groupingBy(CfgRuleLogisticsEntity::getLogisticsMethod)).entrySet().stream().filter(obj -> obj.getValue().size() > MathUtil.ONE).map(Map.Entry::getKey).distinct().collect(Collectors.joining(","));
        if (CharSequenceUtil.isNotBlank(names)) {
            throw new ServiceException("物流方式【{}】唯一不能添加重复数据",names);
        }
        //排序
        int maxIndex = MathUtil.ZERO;
        if (Boolean.TRUE.equals(isCustom)) {
            maxIndex = oldList.stream().max(Comparator.comparingInt(CfgRuleLogisticsEntity::getIndex)).map(CfgRuleLogisticsEntity::getIndex).orElse(MathUtil.ZERO);
        }
        for (CfgRuleLogisticsEntity  logisticsEntity: list) {
            //排序
            if (ObjectUtil.isEmpty(logisticsEntity.getIndex())) {
                logisticsEntity.setIndex(maxIndex + 1);
            }
            //时效主表id
            logisticsEntity.setExpireTimeId(expireTimeId);
            //相同物流方式赋值id
            CfgRuleLogisticsEntity entity = oldList.stream().filter(obj -> CharSequenceUtil.equals(obj.getLogisticsMethod(), logisticsEntity.getLogisticsMethod())).findFirst().orElse(null);
            if (!ObjectUtils.isEmpty(entity)) {
                logisticsEntity.setId(entity.getId());
                //自定义添加的需要保持原有序号
                if (ObjectUtil.isEmpty(logisticsEntity.getIndex())) {
                    logisticsEntity.setIndex(Boolean.TRUE.equals(isCustom) ? entity.getIndex() : logisticsEntity.getIndex());
                }
            }
            maxIndex ++;
        }
    }
}
