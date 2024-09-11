package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.CfgRuleSalesDenoisingDTO;
import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingEntity;
import com.erp.model.mrp.enums.CfgRuleSalesDenoisingDenoisingTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleSalesDenoisingMapper;
import com.erp.server.mrp.service.CfgRuleSalesDenoisingService;
import com.erp.server.mrp.service.CfgRuleStockUpService;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 销量去噪信息 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleSalesDenoisingServiceImpl extends SuperServiceImpl<CfgRuleSalesDenoisingMapper, CfgRuleSalesDenoisingEntity> implements CfgRuleSalesDenoisingService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CfgRuleStockUpService cfgRuleStockUpService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<CfgRuleSalesDenoisingDTO.UpdateDTO> salesDenoisingList,String salesQtyId,Boolean isCustom) {
        if (CollectionUtils.isEmpty(salesDenoisingList)) {
            salesDenoisingList = Collections.EMPTY_LIST;
        }

        List<CfgRuleSalesDenoisingEntity> list = BeanMapperUtils.copyList(CfgRuleSalesDenoisingEntity.class, salesDenoisingList);
        //自定义更新无需删除
        if (!isCustom) {
            //原去噪信息
            List<CfgRuleSalesDenoisingEntity> oldList = listBySalesQtyIdList(Arrays.asList(salesQtyId));
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
        handleData(list,salesQtyId);
        log.info("编辑 开始修改销量去噪信息数据，id：【{}】", salesQtyId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("销量去噪信息保存失败");
        }
        //日志
        for (CfgRuleSalesDenoisingEntity denoisingEntity : list) {
            String dateStr = StrUtil.format("{}_{}", denoisingEntity.getStartDate(), denoisingEntity.getEndDate());
            String msg = StrUtil.format("销量去噪:序号【{}】、名称【{}】、时间段【{}】、去噪类型【{}，{}】", denoisingEntity.getIndex(), denoisingEntity.getName(),dateStr,CfgRuleSalesDenoisingDenoisingTypeEnum.getName(denoisingEntity.getDenoisingType()),denoisingEntity.getEffectiveValue());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), salesQtyId, "设置规则");
        }
        return Boolean.TRUE;
    }

    @Override
    public List<CfgRuleSalesDenoisingEntity> listBySalesQtyIdList(List<String> salesQtyIdList) {
        if (CollectionUtils.isEmpty(salesQtyIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<CfgRuleSalesDenoisingEntity> list = lambdaQuery().in(CfgRuleSalesDenoisingEntity::getSalesQtyId, salesQtyIdList).orderByAsc(CfgRuleSalesDenoisingEntity::getIndex).list();
        if (CollectionUtils.isEmpty(list)) {
            return  Collections.EMPTY_LIST;
        }
        for (CfgRuleSalesDenoisingEntity salesDenoisingEntity : list) {
            //时间
            salesDenoisingEntity.setDateList(Arrays.asList(salesDenoisingEntity.getStartDate(),salesDenoisingEntity.getEndDate()));
        }
        return list;
    }

    @Override
    public void deleteBySalesQtyId(String salesQtyId) {
        lambdaUpdate().eq(CfgRuleSalesDenoisingEntity::getSalesQtyId,salesQtyId).remove();
    }

    @Override
    public List<CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO> listDenoisingBySalesId(String id) {
        List<CfgRuleSalesDenoisingEntity> list = list(Wrappers.<CfgRuleSalesDenoisingEntity>lambdaQuery().eq(CfgRuleSalesDenoisingEntity::getSalesQtyId, id));
        return BeanMapperUtils.copyList(CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO.class, list);
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<CfgRuleSalesDenoisingEntity> newList, List<CfgRuleSalesDenoisingEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CfgRuleSalesDenoisingEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(CfgRuleSalesDenoisingEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<CfgRuleSalesDenoisingEntity> list,String salesQtyId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<CfgRuleSalesDenoisingEntity> oldSalesDenoisingList = this.listBySalesQtyIdList(Arrays.asList(salesQtyId));
        //最大排序
        Integer maxIndex = oldSalesDenoisingList.stream().max(Comparator.comparingInt(CfgRuleSalesDenoisingEntity::getIndex)).map(CfgRuleSalesDenoisingEntity::getIndex).orElse(MathUtil.ZERO);

        for (CfgRuleSalesDenoisingEntity denoisingEntity : list) {
            //主键id赋值
            String id = oldSalesDenoisingList.stream().filter(obj -> StrUtil.equals(obj.getName(), denoisingEntity.getName())).map(CfgRuleSalesDenoisingEntity::getId).findFirst().orElse("");
            if (StrUtil.isNotBlank(id)) {
                denoisingEntity.setId(id);
            } else {
                //排序
                denoisingEntity.setIndex(maxIndex + 1);
            }
            denoisingEntity.setSalesQtyId(salesQtyId);

            boolean isCompare = (StrUtil.equals(denoisingEntity.getDenoisingType(), CfgRuleSalesDenoisingDenoisingTypeEnum.PERCENTAGE.getCode())
                    || StrUtil.equals(denoisingEntity.getDenoisingType(), CfgRuleSalesDenoisingDenoisingTypeEnum.FIXED_VALUE.getCode()))
                    && MathUtil.compareTo(denoisingEntity.getEffectiveValue(), MathUtil.ZERO) <= MathUtil.ZERO;
            if (isCompare) {
                throw new ServiceException("百分比去噪、固定值去噪数值不能小于1");
            }
            //时间
            List<LocalDate> dateList = denoisingEntity.getDateList();
            if (CollectionUtils.isNotEmpty(dateList)) {
                if (CollectionUtils.isEmpty(dateList) || dateList.size() != 2) {
                    throw new ServiceException("时间区间不能为空");
                }
                if (dateList.get(0).isAfter(dateList.get(1))) {
                    throw new ServiceException("开始时间不能大于结束时间");
                }
            }
            denoisingEntity.setStartDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(0) : null);
            denoisingEntity.setEndDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(1) : null);
            maxIndex ++;
        }
    }
}
