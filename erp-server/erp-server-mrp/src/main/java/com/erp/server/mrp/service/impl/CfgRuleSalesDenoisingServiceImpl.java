package com.erp.server.mrp.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.CfgRuleSalesDenoisingDTO;
import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingEntity;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;
import com.erp.model.mrp.enums.CfgRuleSalesDenoisingDenoisingTypeEnum;
import com.erp.model.mrp.enums.CfgRuleStockingRatioTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleSalesDenoisingMapper;
import com.erp.server.mrp.service.CfgRuleSalesDenoisingService;
import com.erp.server.mrp.service.CfgRuleSalesQtyService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
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
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private CfgRuleSalesQtyService cfgRuleSalesQtyService;

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(List<CfgRuleSalesDenoisingDTO.UpdateDTO> salesDenoisingList, String salesQtyId, Boolean isCustom) {
        if (CollectionUtils.isEmpty(salesDenoisingList)) {
            salesDenoisingList = Collections.emptyList();
        }

        List<CfgRuleSalesDenoisingEntity> list = BeanMapperUtils.copyList(CfgRuleSalesDenoisingEntity.class, salesDenoisingList);

        //原去噪信息
        List<CfgRuleSalesDenoisingEntity> oldList = listBySalesQtyIdList(Collections.singletonList(salesQtyId));
        //自定义更新无需删除
        if (Boolean.FALSE.equals(isCustom)) {
            //删除明细
            List<String> deleteIds = getDeleteIds(list, oldList);
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                this.removeByIds(deleteIds);
                // 数据处理
                oldList = oldList.stream().filter(obj -> !deleteIds.contains(obj.getId())).collect(Collectors.toList());
            }
        }
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //销量信息
        CfgRuleSalesQtyEntity salesQtyEntity = cfgRuleSalesQtyService.getById(salesQtyId);
        if (ObjectUtil.isEmpty(salesQtyEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"规则设置（销量）");
        }

        handleData(list,oldList,salesQtyId,isCustom);
        log.info("编辑 开始修改销量去噪信息数据，id：【{}】", salesQtyId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("销量去噪信息保存失败");
        }
        StringBuilder msg = new StringBuilder();
        msg.append( CharSequenceUtil.format("{}_销量去噪：<br>", CfgRuleStockingRatioTypeEnum.getName(salesQtyEntity.getType())));
        //日志
        for (CfgRuleSalesDenoisingEntity denoisingEntity : list) {
            String dateStr = CharSequenceUtil.format("{}~{}", denoisingEntity.getStartDate(), denoisingEntity.getEndDate());
            msg.append(CharSequenceUtil.format("•序号【{}】、名称【{}】、时间段【{}】、去噪类型【{}，{}】<br>", denoisingEntity.getIndex(), denoisingEntity.getName(),dateStr,CfgRuleSalesDenoisingDenoisingTypeEnum.getName(denoisingEntity.getDenoisingType()),denoisingEntity.getEffectiveValue()));
        }
        operateLogService.addModuleOperateLog(msg.toString(), ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), CharSequenceUtil.blankToDefault(salesQtyEntity.getRefId(),salesQtyEntity.getId()), "销量");
    }

    @Override
    public List<CfgRuleSalesDenoisingEntity> listBySalesQtyIdList(List<String> salesQtyIdList) {
        if (CollectionUtils.isEmpty(salesQtyIdList)) {
            return Collections.emptyList();
        }
        List<CfgRuleSalesDenoisingEntity> list = lambdaQuery().in(CfgRuleSalesDenoisingEntity::getSalesQtyId, salesQtyIdList).orderByAsc(CfgRuleSalesDenoisingEntity::getIndex).list();
        if (CollectionUtils.isEmpty(list)) {
            return  Collections.emptyList();
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
    private void handleData(List<CfgRuleSalesDenoisingEntity> list,List<CfgRuleSalesDenoisingEntity> oldList,String salesQtyId,Boolean isCustom) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        validateUniqueNames(list);
        //排序
        int maxIndex = MathUtil.ZERO;
        if (Boolean.TRUE.equals(isCustom)) {
            maxIndex = oldList.stream().max(Comparator.comparingInt(CfgRuleSalesDenoisingEntity::getIndex)).map(CfgRuleSalesDenoisingEntity::getIndex).orElse(MathUtil.ZERO);
        }
        for (CfgRuleSalesDenoisingEntity denoisingEntity : list) {
            //销量数据
            denoisingEntity.setSalesQtyId(salesQtyId);
            //排序
            denoisingEntity.setIndex(maxIndex + 1);
            //主键id赋值
            CfgRuleSalesDenoisingEntity entity = oldList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), denoisingEntity.getName())).findFirst().orElse(null);
            if (!ObjectUtils.isEmpty(entity)) {
                denoisingEntity.setId(entity.getId());
                //自定义添加的需要保持原有序号
                denoisingEntity.setIndex(Boolean.TRUE.equals(isCustom) ? entity.getIndex() : denoisingEntity.getIndex());
            }

            boolean isCompare = checkNumericValue(denoisingEntity);
            if (isCompare) {
                throw new ServiceException("百分比去噪、固定值去噪数值不能小于1");
            }
            //时间
            List<LocalDate> dateList = denoisingEntity.getDateList();
            checkDate(dateList);
            denoisingEntity.setStartDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(0) : null);
            denoisingEntity.setEndDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(1) : null);
            maxIndex ++;
        }
    }

    /**
     * 校验时间
     * @param dateList 时间
     */
    private void checkDate(List<LocalDate> dateList) {
        if (CollectionUtils.isNotEmpty(dateList)) {
            if (CollectionUtils.isEmpty(dateList) || dateList.size() != 2) {
                throw new ServiceException("时间区间不能为空");
            }
            if (dateList.get(0).isAfter(dateList.get(1))) {
                throw new ServiceException("开始时间不能大于结束时间");
            }
        }
    }

    /**
     * 校验百分比去噪、固定值去噪数值不能小于1
     * @param denoisingEntity 参数
     */
    private boolean checkNumericValue(CfgRuleSalesDenoisingEntity denoisingEntity) {
        return (CharSequenceUtil.equals(denoisingEntity.getDenoisingType(), CfgRuleSalesDenoisingDenoisingTypeEnum.PERCENTAGE.getCode())
                || CharSequenceUtil.equals(denoisingEntity.getDenoisingType(), CfgRuleSalesDenoisingDenoisingTypeEnum.FIXED_VALUE.getCode()))
                && MathUtil.compareTo(denoisingEntity.getEffectiveValue(), MathUtil.ZERO) <= MathUtil.ZERO;
    }

    /**
     * 获取销量去噪名字
     * @param list 参数
     */
    private void validateUniqueNames(List<CfgRuleSalesDenoisingEntity> list) {
        String names = list.stream().collect(Collectors.groupingBy(CfgRuleSalesDenoisingEntity::getName)).entrySet().stream()
                .filter(obj -> obj.getValue().size() > MathUtil.ONE).map(Map.Entry::getKey).distinct().collect(Collectors.joining(","));
        if (CharSequenceUtil.isNotBlank(names)) {
            throw new ServiceException("销量去噪名称【{}】唯一不能添加重复数据",names);
        }
    }
}
