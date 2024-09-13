package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.CfgRuleStockingRatioDTO;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;
import com.erp.model.mrp.entity.CfgRuleStockingRatioEntity;
import com.erp.model.mrp.enums.CfgRuleStockingRatioTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleStockingRatioMapper;
import com.erp.server.mrp.service.CfgRuleStockUpService;
import com.erp.server.mrp.service.CfgRuleStockingRatioService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * 备货系数（规则设置） 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleStockingRatioServiceImpl extends SuperServiceImpl<CfgRuleStockingRatioMapper, CfgRuleStockingRatioEntity> implements CfgRuleStockingRatioService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CfgRuleStockUpService cfgRuleStockUpService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<CfgRuleStockingRatioDTO.UpdateDTO> stockingRatioList,String stockUpId,String type,Boolean isCustom) {
        if (CollectionUtils.isEmpty(stockingRatioList)) {
            stockingRatioList = Collections.EMPTY_LIST;
        }
        List<CfgRuleStockingRatioEntity> list = BeanMapperUtils.copyList(CfgRuleStockingRatioEntity.class, stockingRatioList);

        //原物流信息
        List<CfgRuleStockingRatioEntity> oldList = listByStockUpIdListAndType(Arrays.asList(stockUpId),type);
        //自定义更新无需删除
        if (!isCustom) {
            //删除明细
            List<String> deleteIds = getDeleteIds(list, oldList);
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                this.removeByIds(deleteIds);
                // 数据处理
                oldList = oldList.stream().filter(obj -> !deleteIds.contains(obj.getId())).collect(Collectors.toList());
            }
        }

        if (CollectionUtils.isEmpty(list)) {
            return  Boolean.TRUE;
        }
        // 数据处理
        handleData(list,oldList,stockUpId,type,isCustom);
        log.info("编辑 开始修改备货系数（规则设置）数据，id：【{}】",stockUpId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("备货系数（规则设置）保存失败");
        }
        //日志
        addOperateLog(list,stockUpId);
        return Boolean.TRUE;
    }
    /**
     * 添加日志
     * @author will
     * @date 2024/9/12 11:13
     * @param list
     * @param stockUpId
     */
    private void addOperateLog (List<CfgRuleStockingRatioEntity> list,String stockUpId) {
        //备货信息
        CfgRuleStockUpEntity stockUpEntity = cfgRuleStockUpService.getById(stockUpId);
        if (ObjectUtil.isEmpty(stockUpEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"规则设置（备货）");
        }
        Map<String, List<CfgRuleStockingRatioEntity>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getType()));
        //日志
        for (Map.Entry<String, List<CfgRuleStockingRatioEntity>> entry : map.entrySet()) {
            List<CfgRuleStockingRatioEntity> value = entry.getValue();
            StringBuffer msg = new StringBuffer();
            msg.append(StrUtil.format("{}_动态备货系数：<br>",CfgRuleStockingRatioTypeEnum.getName(value.get(0).getType())));
            for (CfgRuleStockingRatioEntity ratioEntity : value) {
                msg.append(StrUtil.format("•序号【{}】、名称【{}】、时间段【{}】、备货系数【{}】<br>", ratioEntity.getIndex(),ratioEntity.getName(),StrUtil.format("{}~{}",ratioEntity.getStartDate(),ratioEntity.getEndDate()),ratioEntity.getStockingRatio()));
            }
            operateLogService.addModuleOperateLog(msg.toString(), ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), StrUtil.blankToDefault(stockUpEntity.getRefId(),stockUpEntity.getId()), "备货");
        }
    }

    @Override
    public List<CfgRuleStockingRatioEntity> listByStockUpIdList (List<String> stockUpIdList) {
        if (CollectionUtils.isEmpty(stockUpIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<CfgRuleStockingRatioEntity> list = lambdaQuery().in(CfgRuleStockingRatioEntity::getStockUpId, stockUpIdList)
                .orderByAsc(CfgRuleStockingRatioEntity::getIndex)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        list.stream().forEach(obj -> obj.setDateList(Arrays.asList(obj.getStartDate(),obj.getEndDate())));
        return list;
    }

    @Override
    public List<CfgRuleStockingRatioEntity> listByStockUpIdListAndType (List<String> stockUpIdList,String type) {
        if (CollectionUtils.isEmpty(stockUpIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<CfgRuleStockingRatioEntity> list = lambdaQuery().in(CfgRuleStockingRatioEntity::getStockUpId, stockUpIdList)
                .eq(CfgRuleStockingRatioEntity::getType,type)
                .orderByAsc(CfgRuleStockingRatioEntity::getIndex)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        list.stream().forEach(obj -> obj.setDateList(Arrays.asList(obj.getStartDate(),obj.getEndDate())));
        return list;
    }

    @Override
    public void deleteByStockUpId(String stockUpId) {
       lambdaUpdate().eq(CfgRuleStockingRatioEntity::getStockUpId,stockUpId).remove();
    }

    @Override
    @Cacheable(cacheNames = "cache:mrp:listByStockUpIdAndType",keyGenerator = "myKeyGenerator")
    public List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> listByStockUpIdAndType(String id, String skuType) {
        List<CfgRuleStockingRatioEntity> cfgRuleStockingRatioList = list(Wrappers.<CfgRuleStockingRatioEntity>lambdaQuery()
                .eq(CfgRuleStockingRatioEntity::getStockUpId, id)
                .eq(CfgRuleStockingRatioEntity::getType, skuType)
        );
        return BeanMapperUtils.copyList(CfgRuleStockingRatioDTO.StockingRatioResultDTO.class, cfgRuleStockingRatioList);
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<CfgRuleStockingRatioEntity> newList, List<CfgRuleStockingRatioEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CfgRuleStockingRatioEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(CfgRuleStockingRatioEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<CfgRuleStockingRatioEntity> list,List<CfgRuleStockingRatioEntity> oldList,String stockUpId,String type,Boolean isCustom) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        String names = list.stream().collect(Collectors.groupingBy(CfgRuleStockingRatioEntity::getName)).entrySet().stream().filter(obj -> obj.getValue().size() > MathUtil.ONE).map(obj -> obj.getKey()).distinct().collect(Collectors.joining(","));
        if (StrUtil.isNotBlank(names)) {
            throw new ServiceException("备货系数名称【{}】唯一不能添加重复数据",names);
        }
        //排序
        Integer maxIndex = MathUtil.ZERO;
        if (isCustom) {
             maxIndex = oldList.stream().max(Comparator.comparingInt(CfgRuleStockingRatioEntity::getIndex)).map(CfgRuleStockingRatioEntity::getIndex).orElse(MathUtil.ZERO);
        }
        for (CfgRuleStockingRatioEntity stockingRatioEntity : list) {
            //排序
            stockingRatioEntity.setIndex(maxIndex + 1);

            //存在相同名称时则赋值id
            CfgRuleStockingRatioEntity entity = oldList.stream().filter(obj -> StrUtil.equals(obj.getName(), stockingRatioEntity.getName())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(entity)) {
                stockingRatioEntity.setId(entity.getId());
                //自定义添加的需要保持原有序号
                stockingRatioEntity.setIndex(isCustom ? entity.getIndex() : stockingRatioEntity.getIndex());
            }
            //主表id
            stockingRatioEntity.setStockUpId(stockUpId);
            //类型
            stockingRatioEntity.setType(type);
            //时间
            List<LocalDate> dateList = stockingRatioEntity.getDateList();
            if (CollectionUtils.isNotEmpty(dateList)) {
                if (CollectionUtils.isEmpty(dateList) || dateList.size() != 2) {
                    throw new ServiceException("时间区间不能为空");
                }
                if (dateList.get(0).isAfter(dateList.get(1))) {
                    throw new ServiceException("开始时间不能大于结束时间");
                }
            }
            stockingRatioEntity.setStartDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(0) : null);
            stockingRatioEntity.setEndDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(1) : null);
            maxIndex ++;
        }
    }

    /**
     * 根据名称id集合查询
     * @author will
     * @date 2024/9/4 15:53
     * @param nameList
     * @param type
     * @return List<CfgRuleStockingRatioEntity>
     */
    private List<CfgRuleStockingRatioEntity> listByNameListAndType (List<String> nameList,String type) {
        if (CollectionUtils.isEmpty(nameList)) {
            return Collections.EMPTY_LIST;
        }
       return lambdaQuery().eq(CfgRuleStockingRatioEntity::getType,type)
                .in(CfgRuleStockingRatioEntity::getName,nameList)
                .list();
    }
}
