package com.erp.server.tms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.enums.ConfirmStatusEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.model.tms.dto.excel.FirstMileEstimatedBillExcelDTO;
import com.erp.model.tms.dto.excel.FmLogisticsBillCostExcelDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.TmsFirstMileReconciliationDetailEntity;
import com.erp.model.tms.enums.DetailReconciliationTypeEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.server.tms.service.FirstMileEstimatedBillService;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
import com.erp.server.tms.service.TmsFirstMileReconciliationDetailService;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 头程暂估账单
 * @date 2024-08-21
 * @author tanmujin
 */
@Getter
public class FirstMileEstimatedBillExcelListener extends AnalysisEventListener<FirstMileEstimatedBillExcelDTO> {

    /**
     * 错误信息
     */
    private List<FirstMileEstimatedBillExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<FirstMileEstimatedBillExcelDTO> dataList = new ArrayList<>();
    /**
     * 成功信息
     */
    private List<FirstMileEstimatedBillExcelDTO> successList = new ArrayList<>();
    private final LogisticsBillCostService logisticsBillCostService = SpringUtil.getBean(LogisticsBillCostService.class);
    private final TmsFirstMileLogisticService tmsFirstMileLogisticService = SpringUtil.getBean(TmsFirstMileLogisticService.class);
    private final FirstMileEstimatedBillService firstMileEstimatedBillService = SpringUtil.getBean(FirstMileEstimatedBillService.class);
    private final TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService = SpringUtil.getBean(TmsFirstMileReconciliationDetailService.class);


    @Override
    public void invoke(FirstMileEstimatedBillExcelDTO excelDTO, AnalysisContext context) {
        List<String> errorMsgList = new ArrayList<>();

        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if(CharSequenceUtil.isAllBlank(excelDTO.getSourceCode(),excelDTO.getTransportNo(),excelDTO.getBusinessCode())){
            errorMsgList.add("来源单号和业务单号和运单号不能都为空");
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
    public void doAfterAllAnalysed(AnalysisContext context) {
        if(CollectionUtils.isEmpty(dataList)){
            return;
        }
        List<String> sourceCodeList = dataList.stream().map(FirstMileEstimatedBillExcelDTO::getSourceCode).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> businessCodeList = dataList.stream().map(FirstMileEstimatedBillExcelDTO::getBusinessCode).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> transportNoList = dataList.stream().map(FirstMileEstimatedBillExcelDTO::getTransportNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillEntityList = tmsFirstMileLogisticService.listBySourceCodeList(businessCodeList, sourceCodeList, transportNoList);
        List<String> mainIdList = logisticsBillEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<LogisticsBillCostEntity> logisticsBillCostEntitieList = logisticsBillCostService.listByLogisticsBillIdList(mainIdList);
        //暂估账单
        List<FirstMileEstimatedBillDTO.View> estimatedBillList = firstMileEstimatedBillService.listByLogisticsBillIds(mainIdList, ConfirmStatusEnum.CONFIRM.getCode());
        //对账单明细
        List<TmsFirstMileReconciliationDetailEntity> reconciliationDetailEntityList = tmsFirstMileReconciliationDetailService.listBySourceIdsAndStatus(mainIdList, null, DetailReconciliationTypeEnum.ACTUAL.getCode());

        for (FirstMileEstimatedBillExcelDTO excelDTO : dataList) {
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
                errorList.add(excelDTO);
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
            if (CollUtil.isEmpty(logisticsBillEntityList1)) {
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
                msg.append("不存在关联的物流单");
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
                msg.append("存在多条物流单");
                excelDTO.setErrorMsg(msg.toString());
                errorList.add(excelDTO);
                continue;
            }
            LogisticsBillEntity entity = entityList.get(0);
            if(Objects.isNull(entity)){
                excelDTO.setErrorMsg("未找到物流单");
                errorList.add(excelDTO);
                continue;
            }
            estimatedBillList.stream().filter(v->v.getLogisticsBillId().equals(entity.getId())).findFirst().ifPresent(v->{
                excelDTO.setErrorMsg("暂估账单已确认，不能更新信息");
                errorList.add(excelDTO);
            });
            reconciliationDetailEntityList.stream().filter(v->v.getSourceId().equals(entity.getId()) && !v.getStatus().equals(ReconciliationStatusEnum.TO_BE_GENERATED.getCode())).findFirst().ifPresent(v->{
                excelDTO.setErrorMsg("实际账单状态{已生成/已确认/已对账/差异确认}，不能更新信息");
                errorList.add(excelDTO);
            });
            excelDTO.setLogisticsBillId(entity.getId());
            LogisticsBillCostEntity costEntity = logisticsBillCostEntitieList.stream().filter(v->v.getLogisticsBillId().equals(entity.getId())).findFirst().orElse(null);
            if(Objects.isNull(costEntity)){
                excelDTO.setErrorMsg("未找到物流费用");
                errorList.add(excelDTO);
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
            successList.add(excelDTO);
        }
    }
}
