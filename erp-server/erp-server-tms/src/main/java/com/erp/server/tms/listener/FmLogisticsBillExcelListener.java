package com.erp.server.tms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ConfirmStatusEnum;
import com.common.core.constant.EnumMessage;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.model.tms.dto.excel.FmLogisticsBillExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.DetailReconciliationTypeEnum;
import com.erp.model.tms.enums.FmLogisticTrackStatusEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.service.*;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FmLogisticsBillExcelListener extends AnalysisEventListener<FmLogisticsBillExcelDTO> {


    private final TmsFirstMileLogisticService tmsFirstMileLogisticService = SpringUtil.getBean(TmsFirstMileLogisticService.class);

    private final LogisticsBillDetailService logisticsBillDetailService = SpringUtil.getBean(LogisticsBillDetailService.class);

    private final LogisticsSupplierService logisticsSupplierService = SpringUtil.getBean(LogisticsSupplierService.class);

    private final LogisticsChannelService logisticsChannelService = SpringUtil.getBean(LogisticsChannelService.class);

    private final LogisticsBillCostService logisticsBillCostService = SpringUtil.getBean(LogisticsBillCostService.class);

    private final WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign = SpringUtil.getBean(WmsFirstMileDeliveryFeign.class);
    private final FirstMileEstimatedBillService firstMileEstimatedBillService = SpringUtil.getBean(FirstMileEstimatedBillService.class);
    private final TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService = SpringUtil.getBean(TmsFirstMileReconciliationDetailService.class);
    private final static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SupplierFeign supplierFeign = SpringUtil.getBean(SupplierFeign.class);

    @Getter
    private List<FmLogisticsBillExcelDTO> dataList = new ArrayList<>();

    @Getter
    private List<FmLogisticsBillExcelDTO> errorList = new ArrayList<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(FmLogisticsBillExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if (CharSequenceUtil.isAllBlank(excelDTO.getBusinessCode(),excelDTO.getOutstockCode())){
            errorMsgList.add("来源单号和业务单号不能同时为空");
        }
        //校验状态时间
        if(StringUtils.isNotBlank(excelDTO.getLogisticStatusName()) && !excelDTO.getLogisticStatusName().equals(FmLogisticTrackStatusEnum.WAIT_ORDER.getName()) && Objects.isNull(excelDTO.getStatusTime())){
            errorMsgList.add("状态时间不能为空");
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
        List<String> outstockCodeList = dataList.stream().map(FmLogisticsBillExcelDTO::getOutstockCode).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<String> businessCodeList = dataList.stream().map(FmLogisticsBillExcelDTO::getBusinessCode).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<String> supplierNameList = dataList.stream().map(FmLogisticsBillExcelDTO::getSupplierName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> channelNameList = dataList.stream().map(FmLogisticsBillExcelDTO::getChannelName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillEntityList = tmsFirstMileLogisticService.listBySourceCodeList(businessCodeList, outstockCodeList, null);
        List<String> mainIdList = logisticsBillEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<LogisticsBillDetailEntity> logisticsBillDetailEntityList = logisticsBillDetailService.listByMainIds(mainIdList);
        //暂估账单
        List<FirstMileEstimatedBillDTO.View> estimatedBillList = firstMileEstimatedBillService.listByLogisticsBillIds(mainIdList, ConfirmStatusEnum.CONFIRM.getCode());
        //对账单明细
        List<TmsFirstMileReconciliationDetailEntity> reconciliationDetailEntityList = tmsFirstMileReconciliationDetailService.listBySourceIdsAndStatus(mainIdList, null, DetailReconciliationTypeEnum.ACTUAL.getCode());

        List<LogisticsSupplierEntity> logisticsSupplierEntityList = logisticsSupplierService.listByShortName(supplierNameList);
        List<String> supplierIds = logisticsSupplierEntityList.stream().map(v->v.getSupplierId()).collect(Collectors.toList());
        List<SupplierDTO.SupplierDefaultDTO> supplierDefaultDTOList = supplierFeign.listDefaultBySupplierIdList(supplierIds);

        List<LogisticsChannelEntity> logisticsChannelEntityList = logisticsChannelService.listByName(channelNameList);
        List<LogisticsBillCostEntity> logisticsBillCostEntityList = logisticsBillCostService.listByLogisticsBillIdList(mainIdList);
        List<String> outIds = logisticsBillEntityList.stream().map(LogisticsBillEntity::getOutstockId).collect(Collectors.toList());
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(outIds);
        List<LogisticsBillEntity> updateList = new ArrayList<>();
        List<LogisticsBillDetailEntity> updateDetailList = new ArrayList<>();
        List<LogisticsBillCostEntity> updateCostList = new ArrayList<>();
        List<LogisticsTrackEntity> addTrackList = new ArrayList<>();
        for (FmLogisticsBillExcelDTO excelDTO : dataList) {
            int notEmptyCount = 0;
            if (CharSequenceUtil.isNotBlank(excelDTO.getOutstockCode())) {notEmptyCount++;}
            if (CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode())) {notEmptyCount++;}
            //校验数据
            List<LogisticsBillEntity> entityList = logisticsBillEntityList.stream().filter(v -> {
                //同时不为空时，匹配来源单号和业务单号
                if (CharSequenceUtil.isAllNotBlank(excelDTO.getOutstockCode(), excelDTO.getBusinessCode())) {
                    if (v.getOutstockCode().equals(excelDTO.getOutstockCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode())) {return true;}else {return false;}
                }
                //来源单号不为空时，匹配来源单号
                if (CharSequenceUtil.isNotBlank(excelDTO.getOutstockCode())) {if (v.getOutstockCode().equals(excelDTO.getOutstockCode())) return true;else {return false;}}
                //业务单号不为空时，匹配业务单号
                if (CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode())) {if (v.getBusinessCode().equals(excelDTO.getBusinessCode())) return true;else {return false;}}
                return false;
            }).collect(Collectors.toList());
            if (CollUtil.isEmpty(entityList)) {
                StringBuilder msg = new StringBuilder();
                if(CharSequenceUtil.isNotBlank(excelDTO.getOutstockCode())){
                    msg.append("发货单号【").append(excelDTO.getOutstockCode()).append("】");
                }
                if(CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode())){
                    msg.append("业务单号【").append(excelDTO.getBusinessCode()).append("】");
                }
                msg.append("未匹配到物流单");
                excelDTO.setErrorMsg(msg.toString());
                errorList.add(excelDTO);
                continue;
            }
            //校验两个参数及以上都存在时。是否存在关联的多条物流单
            List<LogisticsBillEntity> logisticsBillEntityList1 = logisticsBillEntityList.stream().filter(v -> {
                if (CharSequenceUtil.isAllNotBlank(excelDTO.getOutstockCode(), excelDTO.getBusinessCode())) {
                    return v.getOutstockCode().equals(excelDTO.getOutstockCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode());
                }
                return false;
            }).collect(Collectors.toList());
            if (CollUtil.isEmpty(logisticsBillEntityList1) && notEmptyCount > 1) {
                StringBuilder msg = new StringBuilder();
                if(CharSequenceUtil.isNotBlank(excelDTO.getOutstockCode())){
                    msg.append("来源单号【").append(excelDTO.getOutstockCode()).append("】");
                }
                if(CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode())){
                    msg.append("业务单号【").append(excelDTO.getBusinessCode()).append("】");
                }
                msg.append("匹配不到物流单");
                excelDTO.setErrorMsg(msg.toString());
                errorList.add(excelDTO);
                continue;

            }
            if (entityList.size() > 1) {
                StringBuilder msg = new StringBuilder();
                if(CharSequenceUtil.isNotBlank(excelDTO.getOutstockCode())){
                    msg.append("发货单号【").append(excelDTO.getOutstockCode()).append("】");
                }
                if(CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode())){
                    msg.append("业务单号【").append(excelDTO.getBusinessCode()).append("】");
                }
                msg.append("存在多条物流单");
                excelDTO.setErrorMsg(msg.toString());
                errorList.add(excelDTO);
                continue;
            }
            LogisticsBillEntity entity = entityList.get(0);
            if(Objects.isNull(entity)){
                excelDTO.setErrorMsg("来源单号不存在或未生成物流单");
                errorList.add(excelDTO);
                continue;
            }
            if(CharSequenceUtil.isNotBlank(excelDTO.getErrorMsg())){
                continue;
            }
            reconciliationDetailEntityList.stream().filter(v->v.getSourceId().equals(entity.getId()) && !v.getStatus().equals(ReconciliationStatusEnum.TO_BE_GENERATED.getCode())).findFirst().ifPresent(v->{
                excelDTO.setErrorMsg("实际账单状态{已生成/已确认/已对账/差异确认}，不能更新信息");
                errorList.add(excelDTO);
            });
            if(CharSequenceUtil.isNotBlank(excelDTO.getErrorMsg())){
                continue;
            }
            LogisticsBillDetailEntity detailEntity = logisticsBillDetailEntityList.stream().filter(v->v.getMainId().equals(entity.getId())).findFirst().orElse(null);
            if(Objects.isNull(detailEntity)){
                excelDTO.setErrorMsg("物流单明细不存在");
                errorList.add(excelDTO);
                continue;
            }
            LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostEntityList.stream().filter(v->v.getLogisticsBillId().equals(entity.getId())).findFirst().orElse(null);
            if(Objects.isNull(logisticsBillCostEntity)){
                excelDTO.setErrorMsg("物流单费用不存在");
                errorList.add(excelDTO);
                continue;
            }

            if(!(logisticsBillCostEntity.getReconciliationStatus().equals(ReconciliationStatusEnum.INVALID.getCode()) ||logisticsBillCostEntity.getReconciliationStatus().equals(ReconciliationStatusEnum.TO_BE_GENERATED.getCode()))){
                excelDTO.setErrorMsg("已生成对账单，不能更新信息");
                errorList.add(excelDTO);
                continue;
            }
            //校验供应商和渠道
            List<String> errorMsgList = new ArrayList<>();
            LogisticsSupplierEntity logisticsSupplierEntity;
            if(StringUtils.isNotBlank(excelDTO.getSupplierName())){
                logisticsSupplierEntity = logisticsSupplierEntityList.stream().filter(v->v.getShortName().equals(excelDTO.getSupplierName())).findFirst().orElse(null);
                if(Objects.isNull(logisticsSupplierEntity)){
                    errorMsgList.add("供应商不存在");
                }else{
                    SupplierDTO.SupplierDefaultDTO supplierDefaultDTO = supplierDefaultDTOList.stream().filter(v->v.getSupplierId().equals(logisticsSupplierEntity.getSupplierId())).findFirst().orElse(null);
                    if(Objects.nonNull(supplierDefaultDTO)){
                        logisticsBillCostEntity.setCurrency(supplierDefaultDTO.getSupplierEntity().getPayCurrency());
                        updateCostList.add(logisticsBillCostEntity);
                    }
                    entity.setLogisticsSupplierId(logisticsSupplierEntity.getId());
                }
            } else {
                logisticsSupplierEntity = null;
            }
            if(StringUtils.isNotBlank(excelDTO.getChannelName())){
                List<LogisticsChannelEntity> currentlogisticsChannelEntityList = logisticsChannelEntityList.stream().filter(v->v.getName().equals(excelDTO.getChannelName())).collect(Collectors.toList());
                if(CollectionUtils.isEmpty(currentlogisticsChannelEntityList)){
                    errorMsgList.add("渠道不存在");
                }else{
                    if(currentlogisticsChannelEntityList.size() > 1 && Objects.isNull(logisticsSupplierEntity)){
                        errorMsgList.add("渠道存在多条，请选择对应的物流商");
                    }else if (currentlogisticsChannelEntityList.size() > 1){
                        LogisticsChannelEntity logisticsChannel = currentlogisticsChannelEntityList.stream().filter(v->v.getMainId().equals(entity.getLogisticsSupplierId())).findFirst().orElse(null);
                        if(Objects.isNull(logisticsChannel)) {
                            errorMsgList.add("渠道与物流商不匹配");
                        }else{
                            entity.setChannelId(logisticsChannel.getId());
                            if(StringUtils.isBlank(entity.getLogisticsSupplierId())){
                                entity.setLogisticsSupplierId(logisticsChannel.getMainId());
                            }
                        }
                    }else{
                        LogisticsChannelEntity logisticsChannel = currentlogisticsChannelEntityList.get(0);
                        if(Objects.nonNull(logisticsSupplierEntity) && !logisticsSupplierEntity.getId().equals(logisticsChannel.getMainId())){
                            errorMsgList.add("渠道与物流商不匹配");
                        }else{
                            entity.setChannelId(logisticsChannel.getId());
                            if(StringUtils.isBlank(entity.getLogisticsSupplierId())){
                                entity.setLogisticsSupplierId(logisticsChannel.getMainId());
                            }
                        }
                    }
                }
            }

            if(StringUtils.isNotBlank(excelDTO.getLogisticStatusName())){
                //校验物流状态
                FmLogisticTrackStatusEnum statusEnum = EnumMessage.getByName(FmLogisticTrackStatusEnum.class,(excelDTO.getLogisticStatusName()));
                if(statusEnum == FmLogisticTrackStatusEnum.ORDERED && StringUtils.isBlank(entity.getChannelId())){
                    errorMsgList.add("已下单但尚未填写渠道信息，请填写后更新");
                }
                if(statusEnum == FmLogisticTrackStatusEnum.SIGN && StringUtils.isBlank(entity.getTransportNo())){
                    errorMsgList.add("已签收但无运单号，请填写后更新");
                }
                FmLogisticTrackStatusEnum nowStatusEnum = EnumMessage.getByCode(FmLogisticTrackStatusEnum.class,(detailEntity.getTrackStatus()));
                if(FmLogisticTrackStatusEnum.WAIT_ORDER == nowStatusEnum && FmLogisticTrackStatusEnum.ORDERED != statusEnum){
                    errorMsgList.add("待下单状态只能更新为已下单");
                }

                if(FmLogisticTrackStatusEnum.WAIT_ORDER != nowStatusEnum && FmLogisticTrackStatusEnum.WAIT_ORDER == statusEnum){
                    errorMsgList.add(CharSequenceUtil.format("{}状态不能更新为待下单",Objects.isNull(nowStatusEnum)?"":nowStatusEnum.getName()));
                }

                if(FmLogisticTrackStatusEnum.ORDERED != nowStatusEnum &&  FmLogisticTrackStatusEnum.WAIT_ORDER != nowStatusEnum && FmLogisticTrackStatusEnum.ORDERED == statusEnum){
                    errorMsgList.add(CharSequenceUtil.format(CharSequenceUtil.format("{}状态不能更新为已下单",Objects.isNull(nowStatusEnum)?"":nowStatusEnum.getName())));
                }
                FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntityList.stream().filter(v->v.getId().equals(entity.getOutstockId())).findFirst().orElse(null);
                if(Objects.isNull(firstMileDeliveryEntity)){
                    errorMsgList.add("未找到对应的发货单");
                }
                if(!firstMileDeliveryEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && (statusEnum == FmLogisticTrackStatusEnum.TRACK_ING || statusEnum == FmLogisticTrackStatusEnum.ARRIVED ||statusEnum == FmLogisticTrackStatusEnum.SIGN ||statusEnum == FmLogisticTrackStatusEnum.INSPECTING )){
                    errorMsgList.add(CharSequenceUtil.format("关联单据{}尚未审核通过无法提交",firstMileDeliveryEntity.getCode()));
                }

            }

            if (!errorMsgList.isEmpty()) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }

            if(StringUtils.isNotBlank(excelDTO.getShippingMethodName())){
                LogisticsMethodEnum logisticsMethodEnum = LogisticsMethodEnum.getByName(excelDTO.getShippingMethodName());
                if(Objects.nonNull(logisticsMethodEnum)){
                    entity.setShippingMethod(logisticsMethodEnum.getCode());
                }
            }


            if(StringUtils.isNotBlank(excelDTO.getTransportNo())){
                entity.setTransportNo(excelDTO.getTransportNo());
            }

            if(StringUtils.isNotBlank(excelDTO.getCounterNo())){
                entity.setCounterNo(excelDTO.getCounterNo());
            }
            if(StringUtils.isNotBlank(excelDTO.getLogisticStatusName())){
                FmLogisticTrackStatusEnum logisticTrackStatusEnum = FmLogisticTrackStatusEnum.getByName(excelDTO.getLogisticStatusName());
                if(Objects.nonNull(logisticTrackStatusEnum)){
                    //待下单不用封装轨迹，其他状态需要
                    if(logisticTrackStatusEnum == FmLogisticTrackStatusEnum.WAIT_ORDER){
                        entity.setOrderTime(null);
                    }else if (logisticTrackStatusEnum == FmLogisticTrackStatusEnum.ORDERED){

                        entity.setOrderTime(Objects.isNull(excelDTO.getStatusTime())? LocalDateTime.now():excelDTO.getStatusTime());

                        LogisticsTrackEntity trackEntity = new LogisticsTrackEntity();
                        trackEntity.setTrackNo(entity.getCounterNo());
                        trackEntity.setTrackTime(Objects.isNull(excelDTO.getStatusTime())? LocalDateTime.now():excelDTO.getStatusTime());
                        trackEntity.setStatus(logisticTrackStatusEnum.getCode());
                        trackEntity.setContent(StringUtils.isBlank(excelDTO.getCurrencyTrack())?"已下单":excelDTO.getCurrencyTrack());
                        trackEntity.setMd5(getDataMd5(trackEntity));
                        addTrackList.add(trackEntity);
                    }else{
                        if(StringUtils.isNotBlank(excelDTO.getCurrencyTrack())){
                            LogisticsTrackEntity trackEntity = new LogisticsTrackEntity();
                            trackEntity.setTrackNo(entity.getCounterNo());
                            trackEntity.setTrackTime(Objects.isNull(excelDTO.getStatusTime())? LocalDateTime.now():excelDTO.getStatusTime());
                            trackEntity.setStatus(logisticTrackStatusEnum.getCode());
                            trackEntity.setContent(excelDTO.getCurrencyTrack());
                            trackEntity.setMd5(getDataMd5(trackEntity));
                            addTrackList.add(trackEntity);
                        }
                    }
                    if (logisticTrackStatusEnum == FmLogisticTrackStatusEnum.SIGN){
                        detailEntity.setSignTime(Objects.isNull(excelDTO.getStatusTime())? LocalDateTime.now():excelDTO.getStatusTime());
                    }
                    if(StringUtils.isNotBlank(excelDTO.getCounterNo())){
                        detailEntity.setTrackNo(excelDTO.getCounterNo());
                    }
                    detailEntity.setTrackStatus(logisticTrackStatusEnum.getCode());
                    updateDetailList.add(detailEntity);
                }
            }
            updateList.add(entity);
        }
        tmsFirstMileLogisticService.updateImport(updateList,updateDetailList,addTrackList, updateCostList);
    }

    /**
     * 获取唯一值
     * @param trackingDetail
     * @return
     */
    private String getDataMd5(LogisticsTrackEntity trackingDetail) {
        String trackTime = trackingDetail.getTrackTime().format(TIME_FORMAT);
        return DigestUtil.md5Hex(trackingDetail.getTrackNo() + "-" + trackingDetail.getContent() + "-" + trackTime);
    }
}
