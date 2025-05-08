package com.erp.server.tms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.enums.ConfirmStatusEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.model.tms.dto.excel.FirstMileReconciliationStandardExcelDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.TmsFirstMileReconciliationDetailEntity;
import com.erp.model.tms.entity.TmsFirstMileReconciliationEntity;
import com.erp.model.tms.enums.DetailReconciliationTypeEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.server.tms.service.FirstMileEstimatedBillService;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
import com.erp.server.tms.service.TmsFirstMileReconciliationDetailService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;



/**
 * 头程对账单标准模板监听
 *
 * @author Jim
 * {@code @date:} 2024/03/22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FirstMileReconciliationStandardExcelListener extends AnalysisEventListener<FirstMileReconciliationStandardExcelDTO> {

    private TmsFirstMileReconciliationEntity mainEntity;
    /**
     * 错误信息
     */
    private List<FirstMileReconciliationStandardExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<FirstMileReconciliationStandardExcelDTO> dataList = new ArrayList<>();

    /**
     * 记录哪些序号有错误信息
     */
    private Set<String> errorNoSet = new HashSet<>();

    /**
     * 成功信息
     */
    private List<FirstMileReconciliationStandardExcelDTO> successList = new ArrayList<>();
    private final LogisticsBillCostService logisticsBillCostService = SpringUtil.getBean(LogisticsBillCostService.class);
    private final TmsFirstMileLogisticService tmsFirstMileLogisticService = SpringUtil.getBean(TmsFirstMileLogisticService.class);
    private final FirstMileEstimatedBillService firstMileEstimatedBillService = SpringUtil.getBean(FirstMileEstimatedBillService.class);
    private final TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService = SpringUtil.getBean(TmsFirstMileReconciliationDetailService.class);

    public FirstMileReconciliationStandardExcelListener(TmsFirstMileReconciliationEntity mainEntity) {
        this.mainEntity = mainEntity;
    }

    /**
     * 每解析一行数据回调一遍
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(FirstMileReconciliationStandardExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();



        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if(CharSequenceUtil.isAllBlank(excelDTO.getSourceCode(),excelDTO.getTransportNo(),excelDTO.getBusinessCode())){
            errorMsgList.add("来源单号和业务单号和运单号不能都为空");
        }
        if ((CharSequenceUtil.isBlank(excelDTO.getCostName()) || CharSequenceUtil.isBlank(excelDTO.getCostValue()))
                && CharSequenceUtil.isBlank(excelDTO.getActualWeight())
                && CharSequenceUtil.isBlank(excelDTO.getVolumeWeight())
        ) {
            errorMsgList.add("实际实重、实际计费重、（费用项、费用金额）至少填一个");
        }
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            if(StringUtils.isNotBlank(excelDTO.getNo())){
                errorNoSet.add(excelDTO.getNo());
            }
            return;
        }
        //添加数据用于判断是否为空
        dataList.add(excelDTO);
    }

    public List<FirstMileReconciliationStandardExcelDTO> getExcelDateList() {
        return dataList;
    }

    /**
     * 据全部解析完后
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if(CollectionUtils.isEmpty(dataList)){
            return;
        }
        List<String> sourceCodeList = dataList.stream().map(FirstMileReconciliationStandardExcelDTO::getSourceCode).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> businessCodeList = dataList.stream().map(FirstMileReconciliationStandardExcelDTO::getBusinessCode).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> transportNoList = dataList.stream().map(FirstMileReconciliationStandardExcelDTO::getTransportNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillEntityList = tmsFirstMileLogisticService.listBySourceCodeList(businessCodeList, sourceCodeList, transportNoList);
        List<String> mainIdList = logisticsBillEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<LogisticsBillCostEntity> logisticsBillCostEntitieList = logisticsBillCostService.listByLogisticsBillIdList(mainIdList);
        //暂估账单
        List<FirstMileEstimatedBillDTO.View> estimatedBillList = firstMileEstimatedBillService.listByLogisticsBillIds(mainIdList, ConfirmStatusEnum.CONFIRM.getCode());
        //对账单明细
        List<TmsFirstMileReconciliationDetailEntity> reconciliationDetailEntityList = tmsFirstMileReconciliationDetailService.listBySourceIdsAndStatus(mainIdList, null, DetailReconciliationTypeEnum.ACTUAL.getCode());

        Map<String, List<FirstMileReconciliationStandardExcelDTO>> dataMap = dataList.stream()
                .collect(Collectors.groupingBy(FirstMileReconciliationStandardExcelDTO::getNo));

        for (Map.Entry<String, List<FirstMileReconciliationStandardExcelDTO>> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            List<FirstMileReconciliationStandardExcelDTO> value = entry.getValue();
            for (FirstMileReconciliationStandardExcelDTO excelDTO : value) {
                int notEmptyCount = 0;
                if (CharSequenceUtil.isNotBlank(excelDTO.getSourceCode())) {notEmptyCount++;}
                if (CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode())) {notEmptyCount++;}
                if (CharSequenceUtil.isNotBlank(excelDTO.getTransportNo())) {notEmptyCount++;}
                //校验数据
                List<LogisticsBillEntity> entityList = logisticsBillEntityList.stream().filter(v -> {
                    //同时不为空时，匹配来源单号和业务单号
                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(), excelDTO.getBusinessCode(),excelDTO.getTransportNo())) {
                        if (v.getOutstockCode().equals(excelDTO.getSourceCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                    }
                    //运单号不为空时，匹配运单号
                    if (CharSequenceUtil.isNotBlank(excelDTO.getTransportNo()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(), excelDTO.getBusinessCode())) {
                        if (v.getOutstockCode().equals(excelDTO.getSourceCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode())) {return true;}
                    }
                    //同时不为空时，匹配来源单号和运单号
                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(),excelDTO.getTransportNo())) {
                        if (v.getOutstockCode().equals(excelDTO.getSourceCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                    }
                    //同时不为空时，匹配来源单号和业务单号
                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getBusinessCode(),excelDTO.getTransportNo())) {
                        if (v.getBusinessCode().equals(excelDTO.getBusinessCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                    }
                    //来源单号不为空时，匹配来源单号
                    if (CharSequenceUtil.isNotBlank(excelDTO.getSourceCode()) && v.getOutstockCode().equals(excelDTO.getSourceCode())) {return true;}
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
                    msg.append("未匹配到物流单");
                    excelDTO.setErrorMsg(msg.toString());
                    errorNoSet.add(excelDTO.getNo());
                    continue;
                }
                //校验两个参数及以上都存在时。是否存在关联的多条物流单
                List<LogisticsBillEntity> logisticsBillEntityList1 = logisticsBillEntityList.stream().filter(v -> {
                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(), excelDTO.getBusinessCode(),excelDTO.getTransportNo())) {
                        if (v.getOutstockCode().equals(excelDTO.getSourceCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                    }
                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(), excelDTO.getBusinessCode())) {
                        if (v.getOutstockCode().equals(excelDTO.getSourceCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode())) {return true;}
                    }
                    //同时不为空时，匹配来源单号和运单号
                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(),excelDTO.getTransportNo())) {
                        if (v.getOutstockCode().equals(excelDTO.getSourceCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                    }
                    //同时不为空时，匹配来源单号和业务单号
                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getBusinessCode(),excelDTO.getTransportNo())) {
                        if (v.getBusinessCode().equals(excelDTO.getBusinessCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                    }
                    return false;
                }).collect(Collectors.toList());
                if (CollUtil.isEmpty(logisticsBillEntityList1) && notEmptyCount > 1) {
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
                    msg.append("匹配不到物流单");
                    excelDTO.setErrorMsg(msg.toString());
                    errorNoSet.add(excelDTO.getNo());
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
                    msg.append("存在多条物流单");
                    excelDTO.setErrorMsg(msg.toString());
                    errorNoSet.add(excelDTO.getNo());
                    continue;
                }
                LogisticsBillEntity entity = entityList.get(0);
                if(Objects.isNull(entity)){
                    excelDTO.setErrorMsg("未找到物流单");
                    errorNoSet.add(excelDTO.getNo());
                    continue;
                }
                estimatedBillList.stream().filter(v->v.getLogisticsBillId().equals(entity.getId())).findFirst().ifPresent(v->{
                    excelDTO.setErrorMsg("暂估账单已确认，不能更新信息");
                    errorNoSet.add(excelDTO.getNo());
                });
                if(CharSequenceUtil.isNotBlank(excelDTO.getErrorMsg())){
                    continue;
                }
                List<String> statusList = new ArrayList<>();
                statusList.add(ReconciliationStatusEnum.TO_BE_GENERATED.getCode());
                statusList.add(ReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
                reconciliationDetailEntityList.stream().filter(v->v.getSourceId().equals(entity.getId()) && !statusList.contains(v.getStatus()) && v.getMainId().equals(mainEntity.getId())).findFirst().ifPresent(v->{
                    excelDTO.setErrorMsg("实际账单状态{已确认/已对账/差异确认}，不能更新信息");
                    errorNoSet.add(excelDTO.getNo());
                });
                if(CharSequenceUtil.isNotBlank(excelDTO.getErrorMsg())){
                    continue;
                }
                excelDTO.setLogisticsBillId(entity.getId());
                LogisticsBillCostEntity costEntity = logisticsBillCostEntitieList.stream().filter(v->v.getLogisticsBillId().equals(entity.getId())).findFirst().orElse(null);
                if(Objects.isNull(costEntity)){
                    excelDTO.setErrorMsg("未找到物流费用");
                    errorNoSet.add(excelDTO.getNo());
                    continue;
                }
                excelDTO.setCostId(costEntity.getId());
                if (CharSequenceUtil.isBlank(excelDTO.getBusinessCode())){
                    excelDTO.setBusinessCode(entity.getBusinessCode());
                }
                if (CharSequenceUtil.isBlank(excelDTO.getSourceCode())){
                    excelDTO.setSourceCode(entity.getOutstockCode());
                }
                if (CharSequenceUtil.isBlank(excelDTO.getTransportNo())){
                    excelDTO.setTransportNo(entity.getTransportNo());
                }
            }
            if(errorNoSet.contains(key)){
                for (FirstMileReconciliationStandardExcelDTO excelDTO : value) {
                    if(StringUtils.isBlank(excelDTO.getErrorMsg())){
                        excelDTO.setErrorMsg("相同序号的数据存在错误，请检查");
                    }
                }
                errorList.addAll(value);
            }else {
                successList.addAll(value);
            }
        }
    }
}
