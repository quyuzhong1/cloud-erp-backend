package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.CfgRuleStockingRatioDTO;
import com.erp.model.mrp.entity.CfgRuleStockingRatioEntity;
import com.erp.server.mrp.mapper.CfgRuleStockingRatioMapper;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        //自定义更新无需删除
        if (!isCustom) {
            //原物流信息
            List<CfgRuleStockingRatioEntity> oldList = listByStockUpIdListAndType(Arrays.asList(stockUpId),type);
            //删除明细
            List<String> deleteIds = getDeleteIds(list, oldList);
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                this.removeByIds(deleteIds);
            }
        }

        if (CollectionUtils.isEmpty(list)) {
            return  Boolean.TRUE;
        }
        // 数据处理
        handleData(list,stockUpId,type);
        log.info("编辑 开始修改备货系数（规则设置）数据，id：【{}】",stockUpId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("备货系数（规则设置）保存失败");
        }
        return Boolean.TRUE;
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
    private void handleData(List<CfgRuleStockingRatioEntity> list,String stockUpId,String type) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        String names = list.stream().collect(Collectors.groupingBy(CfgRuleStockingRatioEntity::getName)).entrySet().stream().filter(obj -> obj.getValue().size() > MathUtil.ONE).map(obj -> obj.getKey()).collect(Collectors.joining(","));
        if (StrUtil.isNotBlank(names)) {
            throw new ServiceException("备货系数名称【{}】唯一不能添加重复数据",names);
        }
        List<String> nameList = list.stream().map(CfgRuleStockingRatioEntity::getName).distinct().collect(Collectors.toList());
        List<CfgRuleStockingRatioEntity> cfgRuleStockingRatioList = listByNameListAndType(nameList, type);

        for (CfgRuleStockingRatioEntity stockingRatioEntity : list) {
            //存在相同名称时则赋值id
            CfgRuleStockingRatioEntity entity = cfgRuleStockingRatioList.stream().filter(obj -> StrUtil.equals(obj.getName(), stockingRatioEntity.getName())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(entity) && !StrUtil.equals(stockingRatioEntity.getId(),entity.getId())) {
                stockingRatioEntity.setId(entity.getId());
            }
            //主表id
            stockingRatioEntity.setStockUpId(stockUpId);
            //类型
            stockingRatioEntity.setType(type);
            //时间
            List<LocalDate> dateList = stockingRatioEntity.getDateList();
            stockingRatioEntity.setStartDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(0) : null);
            stockingRatioEntity.setEndDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(1) : null);
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
