package com.erp.server.mrp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleStockingRatioDTO;
import com.erp.model.mrp.entity.CfgRuleStockingRatioEntity;
import com.erp.server.mrp.mapper.CfgRuleStockingRatioMapper;
import com.erp.server.mrp.service.CfgRuleStockingRatioService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Boolean update(List<CfgRuleStockingRatioDTO.UpdateDTO> stockingRatioList,String stockUpId,String type) {
        if (CollectionUtils.isEmpty(stockingRatioList)) {
            stockingRatioList = Collections.EMPTY_LIST;
        }
        List<CfgRuleStockingRatioEntity> list = BeanMapperUtils.copyList(CfgRuleStockingRatioEntity.class, stockingRatioList);
        //原物流信息
        List<CfgRuleStockingRatioEntity> oldList = listByStockUpIdListAndType(Arrays.asList(stockUpId),type);

        //删除明细
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
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
        List<CfgRuleStockingRatioEntity> list = lambdaQuery().in(CfgRuleStockingRatioEntity::getStockUpId, stockUpIdList).list();
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
        List<CfgRuleStockingRatioEntity> list = lambdaQuery().in(CfgRuleStockingRatioEntity::getStockUpId, stockUpIdList).eq(CfgRuleStockingRatioEntity::getType,type).list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        list.stream().forEach(obj -> obj.setDateList(Arrays.asList(obj.getStartDate(),obj.getEndDate())));
        return list;
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
        for (CfgRuleStockingRatioEntity stockingRatioEntity : list) {
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
}
