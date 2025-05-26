package com.erp.server.tms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.srm.enums.ConfirmStatusEnum;
import com.erp.model.tms.dto.excel.FirstMileCostChangeExcelDTO;
import com.erp.model.tms.entity.FirstMileChangeRecordEntity;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import com.erp.model.tms.enums.AllocationFeeTypeEnum;
import com.erp.model.tms.enums.FirstMileChangeRecordCategoryFieldEnum;
import com.erp.model.tms.enums.FirstMileChangeRecordSourceTypeEnum;
import com.erp.model.tms.enums.ReconciliationBillTypeEnum;
import com.erp.server.tms.convert.FirstMileChangeRecordConverter;
import com.erp.server.tms.service.FirstMileChangeRecordService;
import com.erp.server.tms.service.FirstMileSkuCostAllocationDetailService;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class FirstMileCostChangeExcelListener extends AnalysisEventListener<FirstMileCostChangeExcelDTO> {

    private final FirstMileChangeRecordService firstMileChangeRecordService = SpringUtil.getBean(FirstMileChangeRecordService.class);
    private final FirstMileSkuCostAllocationDetailService firstMileSkuCostAllocationDetailService = SpringUtil.getBean(FirstMileSkuCostAllocationDetailService.class);

    @Getter
    private List<FirstMileCostChangeExcelDTO> dataList = new ArrayList<>();

    @Getter
    private List<FirstMileCostChangeExcelDTO> errorList = new ArrayList<>();
    /**
     * 重新分摊的主键id
     */
    @Getter
    private Set<String> mainIdList = new LinkedHashSet<>();;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(FirstMileCostChangeExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if (CharSequenceUtil.isAllBlank(excelDTO.getBusinessCode(),excelDTO.getSourceCode(),excelDTO.getTransportNo())){
            errorMsgList.add("来源单号、业务单号和物流运单号不能同时为空");
        }
        if (CharSequenceUtil.isAllBlank(excelDTO.getMidPeriodTransitCostStr(),excelDTO.getCurrentPeriodAllocatedCostStr(),excelDTO.getEndPeriodEstimatedCostStr(),excelDTO.getEndPeriodTransitCostStr())){
            errorMsgList.add("冲期初、本期分摊、期末在途和期末暂估不能同时为空");
        }
        //月份格式验证
        try {
            excelDTO.setReportMonth(LocalDate.parse(excelDTO.getReportMonthStr()+"-01"));
        }catch (Exception e){
            errorMsgList.add("月份格式错误");
        }
        //费用类型
        if (CharSequenceUtil.isNotBlank(excelDTO.getFeeTypeName())){
            try {
                excelDTO.setFeeType(AllocationFeeTypeEnum.getByName(excelDTO.getFeeTypeName()).getCode());
            }catch (Exception e){
                errorMsgList.add("费用类型格式错误");
            }
        }
        //分摊重量
        if (CharSequenceUtil.isNotBlank(excelDTO.getAllocatedWeightStr())){
            try {
                excelDTO.setAllocatedWeight(new BigDecimal(excelDTO.getAllocatedWeightStr()));
            }catch (Exception e){
                errorMsgList.add("分摊重量格式错误");
            }
        }
        //冲期初在途费用
        if (CharSequenceUtil.isNotBlank(excelDTO.getMidPeriodTransitCostStr())){
            try {
                excelDTO.setMidPeriodTransitCost(new BigDecimal(excelDTO.getMidPeriodTransitCostStr()));
            }catch (Exception e){
                errorMsgList.add("冲期初在途费用格式错误");
            }
        }
        //本期分摊费用
        if (CharSequenceUtil.isNotBlank(excelDTO.getCurrentPeriodAllocatedCostStr())){
            try {
                excelDTO.setCurrentPeriodAllocatedCost(new BigDecimal(excelDTO.getCurrentPeriodAllocatedCostStr()));
            }catch (Exception e){
                errorMsgList.add("本期分摊费用格式错误");
            }
        }
        //期末在途费用
        if (CharSequenceUtil.isNotBlank(excelDTO.getEndPeriodTransitCostStr())){
            try {
                excelDTO.setEndPeriodTransitCost(new BigDecimal(excelDTO.getEndPeriodTransitCostStr()));
            }catch (Exception e){
                errorMsgList.add("期末在途费用格式错误");
            }
        }
        //期末暂估费用
        if (CharSequenceUtil.isNotBlank(excelDTO.getEndPeriodEstimatedCostStr())){
            try {
                excelDTO.setEndPeriodEstimatedCost(new BigDecimal(excelDTO.getEndPeriodEstimatedCostStr()));
            }catch (Exception e){
                errorMsgList.add("期末暂估费用格式错误");
            }
        }
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        dataList.add(excelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if(CollectionUtils.isEmpty(dataList)){
            return;
        }
        List<String> sourceCodeList = dataList.stream().map(FirstMileCostChangeExcelDTO::getSourceCode).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<String> businessCodeList = dataList.stream().map(FirstMileCostChangeExcelDTO::getBusinessCode).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<String> transportNoList = dataList.stream().map(FirstMileCostChangeExcelDTO::getTransportNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<FirstMileSkuCostAllocationDetailEntity> skuCostAllocationDetailEntityList = firstMileSkuCostAllocationDetailService.listBySourceCodeList(businessCodeList, sourceCodeList, transportNoList);
        List<FirstMileChangeRecordEntity> productWeightList = new ArrayList<>();
        for (FirstMileCostChangeExcelDTO excelDTO : dataList) {
            int notEmptyCount = 0;
            if (CharSequenceUtil.isNotBlank(excelDTO.getSourceCode())) {notEmptyCount++;}
            if (CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode())) {notEmptyCount++;}
            if (CharSequenceUtil.isNotBlank(excelDTO.getTransportNo())) {notEmptyCount++;}
            //校验数据
            List<FirstMileSkuCostAllocationDetailEntity> entityList = skuCostAllocationDetailEntityList.stream()
                    .filter(e -> Objects.equals(e.getFeeType(), excelDTO.getFeeType()) && Objects.equals(e.getPlatformSkuNo(), excelDTO.getPlatformSkuNo()) && Objects.equals(e.getSkuNo(), excelDTO.getSkuNo()) && Objects.equals(e.getReportMonth(), excelDTO.getReportMonth()))
                    .filter(v -> {
                //同时不为空时，匹配来源单号和业务单号
                if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(), excelDTO.getBusinessCode(),excelDTO.getTransportNo())) {
                    if (v.getSourceCode().equals(excelDTO.getSourceCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                }
                //运单号不为空时，匹配运单号
                if (CharSequenceUtil.isNotBlank(excelDTO.getTransportNo()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(), excelDTO.getBusinessCode())) {
                    if (v.getSourceCode().equals(excelDTO.getSourceCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode())) {return true;}
                }
                //同时不为空时，匹配来源单号和运单号
                if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(),excelDTO.getTransportNo())) {
                    if (v.getSourceCode().equals(excelDTO.getSourceCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                }
                //同时不为空时，匹配来源单号和业务单号
                if (CharSequenceUtil.isAllNotBlank(excelDTO.getBusinessCode(),excelDTO.getTransportNo())) {
                    if (v.getBusinessCode().equals(excelDTO.getBusinessCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                }
                //来源单号不为空时，匹配来源单号
                if (CharSequenceUtil.isNotBlank(excelDTO.getSourceCode()) && v.getSourceCode().equals(excelDTO.getSourceCode())) {return true;}
                //业务单号不为空时，匹配业务单号
                if (CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode())) {return true;}
                //运单号不为空时，匹配运单号
                if (CharSequenceUtil.isNotBlank(excelDTO.getTransportNo()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                return false;
            }).collect(Collectors.toList());
            if (CollUtil.isEmpty(entityList)) {
                excelDTO.setErrorMsg("未匹配到费用分摊记录");
                errorList.add(excelDTO);
                continue;
            }
            //校验两个参数及以上都存在时。是否存在关联的多条费用分摊记录
            List<FirstMileSkuCostAllocationDetailEntity> skuCostAllocationDetailEntityList1 = skuCostAllocationDetailEntityList.stream()
                    .filter(e -> Objects.equals(e.getFeeType(), excelDTO.getFeeType()) && Objects.equals(e.getPlatformSkuNo(), excelDTO.getPlatformSkuNo()) && Objects.equals(e.getSkuNo(), excelDTO.getSkuNo()) && Objects.equals(e.getReportMonth(), excelDTO.getReportMonth()))
                    .filter(v -> {
                if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(), excelDTO.getBusinessCode(),excelDTO.getTransportNo())) {
                    if (v.getSourceCode().equals(excelDTO.getSourceCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}else {return false;}
                }
                if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(), excelDTO.getBusinessCode())) {
                    if (v.getSourceCode().equals(excelDTO.getSourceCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode())) {return true;}else {return false;}
                }
                //同时不为空时，匹配来源单号和运单号
                if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(),excelDTO.getTransportNo())) {
                    if (v.getSourceCode().equals(excelDTO.getSourceCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}else {return false;}
                }
                //同时不为空时，匹配来源单号和业务单号
                if (CharSequenceUtil.isAllNotBlank(excelDTO.getBusinessCode(),excelDTO.getTransportNo())) {
                    if (v.getBusinessCode().equals(excelDTO.getBusinessCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}else {return false;}
                }
                return false;
            }).collect(Collectors.toList());
            if (CollUtil.isEmpty(skuCostAllocationDetailEntityList1) && notEmptyCount > 1) {
                excelDTO.setErrorMsg("匹配不到费用分摊记录");
                errorList.add(excelDTO);
                continue;

            }
            if (entityList.size() > 1) {
                excelDTO.setErrorMsg("存在多条费用分摊记录");
                errorList.add(excelDTO);
                continue;
            }
            FirstMileSkuCostAllocationDetailEntity entity = entityList.get(0);
            if(Objects.isNull(entity)){
                excelDTO.setErrorMsg("未找到费用分摊记录");
                errorList.add(excelDTO);
                continue;
            }
            //判断是否存在历史分摊数据
            List<FirstMileSkuCostAllocationDetailEntity> entityList1 = firstMileSkuCostAllocationDetailService.listByReportMonth(entity.getSourceId(), entity.getBusinessCode(), entity.getTransportNo(), entity.getSkuId(), entity.getPlatformSkuNo(), entity.getReportPeriodId());
            if (CollUtil.isNotEmpty(entityList1)){
                excelDTO.setErrorMsg(CharSequenceUtil.format("【{}】存在历史分摊数据，不支持再次修改重量", excelDTO.getSkuNo()));
                errorList.add(excelDTO);
                continue;
            }
            excelDTO.setMainId(entity.getMainId());
            if(StringUtils.isNotBlank(excelDTO.getTransportNo()) && StringUtils.isNotBlank(excelDTO.getSourceCode())
                    &&(!entity.getTransportNo().equals(excelDTO.getTransportNo()) || !entity.getSourceCode().equals(excelDTO.getSourceCode()))){
                excelDTO.setErrorMsg("来源单号与运单号不匹配");
                errorList.add(excelDTO);
                continue;
            }
            //已下推费用分摊不能变更
            if(!ConfirmStatusEnum.WAIT_CONFIRM.getCode().equals(entity.getStatus())){
                excelDTO.setErrorMsg("仅可操作待确认单据");
                errorList.add(excelDTO);
                continue;
            }
            if(CharSequenceUtil.isNotBlank(excelDTO.getErrorMsg())){
                continue;
            }
            if (ReconciliationBillTypeEnum.ACTUAL.getCode().equals(entity.getBillSourceType())){
                if(CharSequenceUtil.isNotBlank(excelDTO.getEndPeriodEstimatedCostStr()) && excelDTO.getEndPeriodEstimatedCost().compareTo(entity.getEndPeriodEstimatedCost())!= 0){
                    excelDTO.setErrorMsg("实际账单不支持修改期末暂估费用");
                    errorList.add(excelDTO);
                    continue;
                }
            }
            if (ReconciliationBillTypeEnum.ESTIMATED.getCode().equals(entity.getBillSourceType())){
                if((CharSequenceUtil.isNotBlank(excelDTO.getMidPeriodTransitCostStr()) && excelDTO.getMidPeriodTransitCost().compareTo(entity.getMidPeriodTransitCost()) != 0)
                || (CharSequenceUtil.isNotBlank(excelDTO.getCurrentPeriodAllocatedCostStr()) && excelDTO.getCurrentPeriodAllocatedCost().compareTo(entity.getCurrentPeriodAllocatedCost())!= 0)
                || (CharSequenceUtil.isNotBlank(excelDTO.getEndPeriodTransitCostStr()) && excelDTO.getEndPeriodTransitCost().compareTo(entity.getEndPeriodTransitCost())!= 0)){
                    excelDTO.setErrorMsg("暂估账单不支持修改本期/冲期初/期末在途费用");
                    errorList.add(excelDTO);
                    continue;
                }
            }
            Boolean isRetry = Boolean.FALSE;
            //冲期初在途费用
            if(CharSequenceUtil.isNotBlank(excelDTO.getAllocatedWeightStr()) && excelDTO.getAllocatedWeight().compareTo(entity.getAllocatedWeight()) != 0){
                FirstMileChangeRecordEntity productWeightEntity = FirstMileChangeRecordConverter.INSTANCE.changeCostAllocatedWeightToEntityConvert(excelDTO, entity);
                productWeightList.add(productWeightEntity);
                mainIdList.add(entity.getMainId());
                isRetry = Boolean.TRUE;
            }
            //冲期初在途费用
            if(CharSequenceUtil.isNotBlank(excelDTO.getMidPeriodTransitCostStr()) && excelDTO.getMidPeriodTransitCost().compareTo(entity.getMidPeriodTransitCost()) != 0){
                FirstMileChangeRecordEntity productWeightEntity = FirstMileChangeRecordConverter.INSTANCE.changeCostMidPeriodTransitToEntityConvert(excelDTO, entity);
                productWeightList.add(productWeightEntity);
                mainIdList.add(entity.getMainId());
                isRetry = Boolean.TRUE;
            }
            //本期分摊费用
            if(CharSequenceUtil.isNotBlank(excelDTO.getCurrentPeriodAllocatedCostStr()) && excelDTO.getCurrentPeriodAllocatedCost().compareTo(entity.getCurrentPeriodAllocatedCost())!= 0){
                FirstMileChangeRecordEntity productWeightEntity = FirstMileChangeRecordConverter.INSTANCE.changeCostCurrentPeriodAllocatedToEntityConvert(excelDTO, entity);
                productWeightList.add(productWeightEntity);
                mainIdList.add(entity.getMainId());
                isRetry = Boolean.TRUE;
            }
            if (isRetry){
                firstMileChangeRecordService.updateCostIsLatest(FirstMileChangeRecordSourceTypeEnum.FIRSTMILECOST.getCode(),entity.getBusinessCode(),entity.getSourceCode(),entity.getLogisticsBillId(),entity.getSkuId(),entity.getFeeType(), FirstMileChangeRecordCategoryFieldEnum.END_PERIOD_TRANSIT_COST.getCode());
            }
            //期末在途费用
            if(CharSequenceUtil.isNotBlank(excelDTO.getEndPeriodTransitCostStr()) && excelDTO.getEndPeriodTransitCost().compareTo(entity.getEndPeriodTransitCost())!= 0){
                FirstMileChangeRecordEntity productWeightEntity = FirstMileChangeRecordConverter.INSTANCE.changeCostEndPeriodTransitToEntityConvert(excelDTO, entity);
                productWeightList.add(productWeightEntity);
                if (!isRetry){
                    firstMileSkuCostAllocationDetailService.updateEndPeriodTransitCost(entity.getId(), excelDTO.getEndPeriodTransitCostStr());
                }
            }
            //期末暂估费用
            if(CharSequenceUtil.isNotBlank(excelDTO.getEndPeriodEstimatedCostStr()) && excelDTO.getEndPeriodEstimatedCost().compareTo(entity.getEndPeriodEstimatedCost())!= 0){
                FirstMileChangeRecordEntity productWeightEntity = FirstMileChangeRecordConverter.INSTANCE.changeCostEndPeriodEstimatedToEntityConvert(excelDTO, entity);
                productWeightList.add(productWeightEntity);
                if (!isRetry){
                    firstMileSkuCostAllocationDetailService.updateEndPeriodEstimatedCost(entity.getId(), excelDTO.getEndPeriodEstimatedCostStr());
                }
            }
            //明细备注
            if(CharSequenceUtil.isNotBlank(excelDTO.getRemark()) && !Objects.equals(excelDTO.getRemark(),entity.getRemark())){
                FirstMileChangeRecordEntity productWeightEntity = FirstMileChangeRecordConverter.INSTANCE.changeCostDetailRemarkToEntityConvert(excelDTO, entity);
                productWeightList.add(productWeightEntity);
                if (!isRetry){
                    firstMileSkuCostAllocationDetailService.updateDetailRemark(entity.getId(), excelDTO.getRemark());
                }
            }
        }
        //保存分摊记录
        if (CollUtil.isNotEmpty(productWeightList)){
            firstMileChangeRecordService.saveCostByEntity(productWeightList);
        }
    }
}
