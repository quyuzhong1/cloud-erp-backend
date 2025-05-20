package com.erp.server.tms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.excel.FirstMileWeightChangeExcelDTO;
import com.erp.model.tms.entity.FirstMileChangeRecordEntity;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import com.erp.model.tms.enums.CostAllocationStatusEnum;
import com.erp.server.tms.convert.FirstMileChangeRecordConverter;
import com.erp.server.tms.service.FirstMileChangeRecordService;
import com.erp.server.tms.service.FirstMileWeightAllocationService;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class FirstMileWeightChangeExcelListener extends AnalysisEventListener<FirstMileWeightChangeExcelDTO> {


    private final FirstMileChangeRecordService firstMileChangeRecordService = SpringUtil.getBean(FirstMileChangeRecordService.class);

    private final FirstMileWeightAllocationService firstMileWeightAllocationService = SpringUtil.getBean(FirstMileWeightAllocationService.class);

    @Getter
    private List<FirstMileWeightChangeExcelDTO> dataList = new ArrayList<>();

    @Getter
    private List<FirstMileWeightChangeExcelDTO> errorList = new ArrayList<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(FirstMileWeightChangeExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if (CharSequenceUtil.isAllBlank(excelDTO.getBusinessCode(),excelDTO.getSourceCode(),excelDTO.getTransportNo())){
            errorMsgList.add("来源单号、业务单号和物流运单号不能同时为空");
        }
        if (CharSequenceUtil.isAllBlank(excelDTO.getProductWeightStr(),excelDTO.getOutStockWeightStr(),excelDTO.getOutStockSizeStr())){
            errorMsgList.add("单产品重量、出库重量和出库尺寸不能同时为空");
        }
        //箱号
        try {
            excelDTO.setBoxNo(Integer.valueOf(excelDTO.getBoxNoStr()));
        }catch (Exception e){
            errorMsgList.add("箱号格式错误");
        }
        //单产品重量
        if (CharSequenceUtil.isNotBlank(excelDTO.getProductWeightStr())){
            try {
                excelDTO.setProductWeight(new BigDecimal(excelDTO.getProductWeightStr()));
            }catch (Exception e){
                errorMsgList.add("单产品重量格式错误");
            }
        }
        if (CharSequenceUtil.isNotBlank(excelDTO.getOutStockWeightStr())){
            try {
                excelDTO.setOutStockWeight(new BigDecimal(excelDTO.getOutStockWeightStr()));
            }catch (Exception e){
                errorMsgList.add("出库重量格式错误");
            }
        }
        if (CharSequenceUtil.isNotBlank(excelDTO.getOutStockSizeStr())){
            String[] split = excelDTO.getOutStockSizeStr().split("/*");
            if (split.length != 3){
                errorMsgList.add("出库尺寸格式错误");
            }
            try {
                excelDTO.setLength(new BigDecimal(split[0]));
                excelDTO.setWidth(new BigDecimal(split[1]));
                excelDTO.setHeight(new BigDecimal(split[2]));
            }catch (Exception e){
                errorMsgList.add("出库尺寸格式错误");
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
        List<String> sourceCodeList = dataList.stream().map(FirstMileWeightChangeExcelDTO::getSourceCode).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<String> businessCodeList = dataList.stream().map(FirstMileWeightChangeExcelDTO::getBusinessCode).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<String> transportNoList = dataList.stream().map(FirstMileWeightChangeExcelDTO::getTransportNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<FirstMileWeightAllocationEntity> weightAllocationEntityList = firstMileWeightAllocationService.listBySourceCodeList(businessCodeList, sourceCodeList, transportNoList);
        List<FirstMileChangeRecordEntity> productWeightList = new ArrayList<>();
        List<FirstMileChangeRecordEntity> outStockList = new ArrayList<>();
        Set<String> logisticsBillIds = new LinkedHashSet<>();
        for (FirstMileWeightChangeExcelDTO excelDTO : dataList) {
            int notEmptyCount = 0;
            if (CharSequenceUtil.isNotBlank(excelDTO.getSourceCode())) {notEmptyCount++;}
            if (CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode())) {notEmptyCount++;}
            if (CharSequenceUtil.isNotBlank(excelDTO.getTransportNo())) {notEmptyCount++;}
            //校验数据
            List<FirstMileWeightAllocationEntity> entityList = weightAllocationEntityList.stream()
                    .filter(e -> Objects.equals(e.getBoxNo(), excelDTO.getBoxNo()) && Objects.equals(e.getPlatformSkuNo(), excelDTO.getPlatformSkuNo()) && Objects.equals(e.getSkuNo(), excelDTO.getSkuNo()))
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
                StringBuilder msg = new StringBuilder();
                if(CharSequenceUtil.isNotBlank(excelDTO.getSourceCode())){
                    msg.append("来源单号【").append(excelDTO.getSourceCode()).append("】");
                }
                if(CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode())){
                    msg.append("业务单号【").append(excelDTO.getBusinessCode()).append("】");
                }
                if(CharSequenceUtil.isNotBlank(excelDTO.getTransportNo())){
                    msg.append("运单号【").append(excelDTO.getTransportNo()).append("】");
                }
                msg.append("未匹配到重量分摊记录");
                excelDTO.setErrorMsg(msg.toString());
                errorList.add(excelDTO);
                continue;
            }
            //校验两个参数及以上都存在时。是否存在关联的多条重量分摊记录
            List<FirstMileWeightAllocationEntity> weightAllocationEntityList1 = weightAllocationEntityList.stream()
                    .filter(e -> Objects.equals(e.getBoxNo(), excelDTO.getBoxNo()) && Objects.equals(e.getPlatformSkuNo(), excelDTO.getPlatformSkuNo()) && Objects.equals(e.getSkuNo(), excelDTO.getSkuNo()))
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
            if (CollUtil.isEmpty(weightAllocationEntityList1) && notEmptyCount > 1) {
                StringBuilder msg = new StringBuilder();
                if(CharSequenceUtil.isNotBlank(excelDTO.getSourceCode())){
                    msg.append("来源单号【").append(excelDTO.getSourceCode()).append("】");
                }
                if(CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode())){
                    msg.append("业务单号【").append(excelDTO.getBusinessCode()).append("】");
                }
                if(CharSequenceUtil.isNotBlank(excelDTO.getTransportNo())){
                    msg.append("运单号【").append(excelDTO.getTransportNo()).append("】");
                }
                msg.append("匹配不到重量分摊记录");
                excelDTO.setErrorMsg(msg.toString());
                errorList.add(excelDTO);
                continue;

            }
            if (entityList.size() > 1) {
                StringBuilder msg = new StringBuilder();
                if(CharSequenceUtil.isNotBlank(excelDTO.getSourceCode())){
                    msg.append("来源单号【").append(excelDTO.getSourceCode()).append("】");
                }
                if(CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode())){
                    msg.append("业务单号【").append(excelDTO.getBusinessCode()).append("】");
                }
                if(CharSequenceUtil.isNotBlank(excelDTO.getTransportNo())){
                    msg.append("运单号【").append(excelDTO.getTransportNo()).append("】");
                }
                msg.append("存在多条重量分摊记录");
                excelDTO.setErrorMsg(msg.toString());
                errorList.add(excelDTO);
                continue;
            }
            FirstMileWeightAllocationEntity entity = entityList.get(0);
            if(Objects.isNull(entity)){
                excelDTO.setErrorMsg("未找到重量分摊记录");
                errorList.add(excelDTO);
                continue;
            }
            if(StringUtils.isNotBlank(excelDTO.getTransportNo()) && StringUtils.isNotBlank(excelDTO.getSourceCode())
                    &&(!entity.getTransportNo().equals(excelDTO.getTransportNo()) || !entity.getSourceCode().equals(excelDTO.getSourceCode()))){
                excelDTO.setErrorMsg("来源单号与运单号不匹配");
                errorList.add(excelDTO);
                continue;
            }
            //已下推费用分摊不能变更
            if(!CostAllocationStatusEnum.NOT.getCode().equals(entity.getCostAllocationStatus())){
                excelDTO.setErrorMsg("已下推费用分摊，不能变更");
                errorList.add(excelDTO);
                continue;
            }
            if(CharSequenceUtil.isNotBlank(excelDTO.getErrorMsg())){
                continue;
            }
            //单产品重量
            if(CharSequenceUtil.isNotBlank(excelDTO.getProductWeightStr()) && excelDTO.getProductWeight().compareTo(entity.getProductWeight()) != 0){
                FirstMileChangeRecordEntity productWeightEntity = FirstMileChangeRecordConverter.INSTANCE.excelProductWeightToEntity(excelDTO, entity);
                productWeightList.add(productWeightEntity);
                logisticsBillIds.add(entity.getLogisticsBillId());
            }
            //出库重量
            if(CharSequenceUtil.isNotBlank(excelDTO.getOutStockWeightStr()) && excelDTO.getOutStockWeight().compareTo(entity.getOutStockWeight())!= 0){
                FirstMileChangeRecordEntity outStockEntity = FirstMileChangeRecordConverter.INSTANCE.excelOutStockWeightToEntity(excelDTO, entity);
                outStockList.add(outStockEntity);
                logisticsBillIds.add(entity.getLogisticsBillId());
            }
            //出库尺寸
            if(CharSequenceUtil.isNotBlank(excelDTO.getOutStockSizeStr())){
                if (excelDTO.getLength().compareTo(entity.getBoxLength()) != 0){
                    FirstMileChangeRecordEntity outStockEntity = FirstMileChangeRecordConverter.INSTANCE.excelOutStockLengthToEntity(excelDTO, entity);
                    outStockList.add(outStockEntity);
                    logisticsBillIds.add(entity.getLogisticsBillId());
                }
                if (excelDTO.getWidth().compareTo(entity.getBoxWidth())!= 0){
                    FirstMileChangeRecordEntity outStockEntity = FirstMileChangeRecordConverter.INSTANCE.excelOutStockWidthToEntity(excelDTO, entity);
                    outStockList.add(outStockEntity);
                    logisticsBillIds.add(entity.getLogisticsBillId());
                }
                if (excelDTO.getHeight().compareTo(entity.getBoxHeight())!= 0){
                    FirstMileChangeRecordEntity outStockEntity = FirstMileChangeRecordConverter.INSTANCE.excelOutStockHeightToEntity(excelDTO, entity);
                    outStockList.add(outStockEntity);
                    logisticsBillIds.add(entity.getLogisticsBillId());
                }
            }
        }
        if (CollUtil.isNotEmpty(productWeightList)){
            firstMileChangeRecordService.saveProductWeightByEntity(productWeightList);
        }
        if (CollUtil.isNotEmpty(outStockList)){
            firstMileChangeRecordService.savePackageByEntity(outStockList);
        }
        //重新计算重量
        logisticsBillIds.forEach(firstMileWeightAllocationService::weightReCompute);
    }
}
