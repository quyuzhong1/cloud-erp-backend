package com.erp.server.mrp.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
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
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private CfgRuleStockUpService cfgRuleStockUpService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheEvict(cacheNames = {"cache:mrp:ratio:listByStockUpIdList","cache:mrp:listByStockUpIdAndType"}, allEntries = true, beforeInvocation = true)
    public Boolean update(List<CfgRuleStockingRatioDTO.UpdateDTO> stockingRatioList,String stockUpId,String type,Boolean isCustom) {
        if (CollectionUtils.isEmpty(stockingRatioList)) {
            stockingRatioList = Collections.emptyList();
        }
        List<CfgRuleStockingRatioEntity> list = BeanMapperUtils.copyList(CfgRuleStockingRatioEntity.class, stockingRatioList);

        //原物流信息
        List<CfgRuleStockingRatioEntity> oldList = listByStockUpIdListAndType(Collections.singletonList(stockUpId),type);
        //自定义更新无需删除
        if (Boolean.FALSE.equals(isCustom)) {
            //删除明细
            List<String> deleteIds = getDeleteIds(list, oldList);
            if (!CollectionUtils.isEmpty(deleteIds)) {
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
        Map<String, List<CfgRuleStockingRatioEntity>> map = list.stream().collect(Collectors.groupingBy(CfgRuleStockingRatioEntity::getType));
        //日志
        for (Map.Entry<String, List<CfgRuleStockingRatioEntity>> entry : map.entrySet()) {
            List<CfgRuleStockingRatioEntity> value = entry.getValue();
            StringBuilder msg = new StringBuilder();
            msg.append(CharSequenceUtil.format("{}_动态备货系数：<br>",CfgRuleStockingRatioTypeEnum.getName(value.get(0).getType())));
            for (CfgRuleStockingRatioEntity ratioEntity : value) {
                msg.append(CharSequenceUtil.format("•序号【{}】、名称【{}】、时间段【{}】、备货系数【{}】<br>", ratioEntity.getIndex(),ratioEntity.getName(),CharSequenceUtil.format("{}~{}",ratioEntity.getStartDate(),ratioEntity.getEndDate()),ratioEntity.getStockingRatio()));
            }
            operateLogService.addModuleOperateLog(msg.toString(), ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), CharSequenceUtil.blankToDefault(stockUpEntity.getRefId(),stockUpEntity.getId()), "备货");
        }
    }

    @Override
    @Cacheable(cacheNames = "cache:mrp:ratio:listByStockUpIdList",keyGenerator = "myKeyGenerator")
    public List<CfgRuleStockingRatioEntity> listByStockUpIdList (List<String> stockUpIdList) {
        if (CollectionUtils.isEmpty(stockUpIdList)) {
            return Collections.emptyList();
        }
        List<CfgRuleStockingRatioEntity> list = lambdaQuery().in(CfgRuleStockingRatioEntity::getStockUpId, stockUpIdList)
                .orderByAsc(CfgRuleStockingRatioEntity::getIndex)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        list.stream().forEach(obj -> obj.setDateList(Arrays.asList(obj.getStartDate(),obj.getEndDate())));
        return list;
    }

    @Override
    public List<CfgRuleStockingRatioEntity> listByStockUpIdListAndType (List<String> stockUpIdList,String type) {
        if (CollectionUtils.isEmpty(stockUpIdList)) {
            return Collections.emptyList();
        }
        List<CfgRuleStockingRatioEntity> list = lambdaQuery().in(CfgRuleStockingRatioEntity::getStockUpId, stockUpIdList)
                .eq(CfgRuleStockingRatioEntity::getType,type)
                .orderByAsc(CfgRuleStockingRatioEntity::getIndex)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        list.forEach(obj -> obj.setDateList(Arrays.asList(obj.getStartDate(),obj.getEndDate())));
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
        List<String> newIds = newList.stream().map(CfgRuleStockingRatioEntity::getId).
                filter(StringUtils::isNotBlank).collect(Collectors.toList());
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
        validateUniqueNames(list);
        //排序
        int maxIndex = Boolean.TRUE.equals(isCustom) ? getMaxIndex(oldList) : MathUtil.ZERO;
        for (CfgRuleStockingRatioEntity stockingRatioEntity : list) {
            updateStockingRatio(oldList, stockUpId, type, isCustom, stockingRatioEntity, maxIndex);
            maxIndex ++;
        }
    }

    /**
     * 修改备货系数
     *
     * @param oldList             旧数据
     * @param stockUpId           备货id
     * @param type                类型
     * @param isCustom            是否自定义
     * @param stockingRatioEntity 备货系数
     * @param maxIndex            优先级
     */
    private static void updateStockingRatio(List<CfgRuleStockingRatioEntity> oldList, String stockUpId, String type, Boolean isCustom, CfgRuleStockingRatioEntity stockingRatioEntity, int maxIndex) {
        //排序
        stockingRatioEntity.setIndex(maxIndex + 1);

        //存在相同名称时则赋值id
        CfgRuleStockingRatioEntity entity = oldList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), stockingRatioEntity.getName())).findFirst().orElse(null);
        if (!ObjectUtils.isEmpty(entity)) {
            stockingRatioEntity.setId(entity.getId());
            //自定义添加的需要保持原有序号
            stockingRatioEntity.setIndex(Boolean.TRUE.equals(isCustom) ? entity.getIndex() : stockingRatioEntity.getIndex());
        }
        //主表id
        stockingRatioEntity.setStockUpId(stockUpId);
        //类型
        stockingRatioEntity.setType(type);
        //时间
        List<LocalDate> dateList = stockingRatioEntity.getDateList();
        if (!CollectionUtils.isEmpty(dateList)) {
            if (dateList.size() != 2) {
                throw new ServiceException("时间区间不能为空");
            }
            if (dateList.get(0).isAfter(dateList.get(1))) {
                throw new ServiceException("开始时间不能大于结束时间");
            }
        }
        stockingRatioEntity.setStartDate(!CollectionUtils.isEmpty(dateList) ? dateList.get(0) : null);
        stockingRatioEntity.setEndDate(!CollectionUtils.isEmpty(dateList) ? dateList.get(1) : null);
    }


    /**
     * 获取最大排序
     *
     * @param oldList 原销量系数
     */
    private Integer getMaxIndex(List<CfgRuleStockingRatioEntity> oldList) {
        return oldList.stream().max(Comparator.comparingInt(CfgRuleStockingRatioEntity::getIndex)).map(CfgRuleStockingRatioEntity::getIndex).orElse(MathUtil.ZERO);
    }

    /**
     * 获取销量名称重复数据
     * @param list 参数
     */
    private void validateUniqueNames(List<CfgRuleStockingRatioEntity> list) {
        String names = list.stream().collect(Collectors.groupingBy(CfgRuleStockingRatioEntity::getName)).entrySet().stream().filter(obj -> obj.getValue().size() > MathUtil.ONE)
                .map(Map.Entry::getKey).distinct().collect(Collectors.joining(","));
        if (CharSequenceUtil.isNotBlank(names)) {
            throw new ServiceException("备货系数名称【{}】唯一不能添加重复数据",names);
        }
    }
}
