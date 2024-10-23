package com.erp.server.tms.listener;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.constant.EnumMessage;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.tms.dto.excel.FmLogisticsBillExcelDTO;
import com.erp.model.tms.entity.*;
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
        List<String> outstockCodeList = dataList.stream().map(FmLogisticsBillExcelDTO::getOutstockCode).collect(Collectors.toList());
        List<String> supplierNameList = dataList.stream().map(FmLogisticsBillExcelDTO::getSupplierName).distinct().collect(Collectors.toList());
        List<String> channelNameList = dataList.stream().map(FmLogisticsBillExcelDTO::getChannelName).distinct().collect(Collectors.toList());
        List<String> transportNoList = dataList.stream().map(FmLogisticsBillExcelDTO::getTransportNo).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> existWithTransportList = tmsFirstMileLogisticService.listByTransportNo(transportNoList);
        List<LogisticsBillEntity> logisticsBillEntityList = tmsFirstMileLogisticService.listByOutstcockCode(outstockCodeList);
        List<String> mainIdList = logisticsBillEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<LogisticsBillDetailEntity> logisticsBillDetailEntityList = logisticsBillDetailService.listByMainIds(mainIdList);
        List<LogisticsSupplierEntity> logisticsSupplierEntityList = logisticsSupplierService.listByName(supplierNameList);
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
            //校验数据
            LogisticsBillEntity entity = logisticsBillEntityList.stream().filter(v->v.getOutstockCode().equals(excelDTO.getOutstockCode())).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                excelDTO.setErrorMsg("来源单号不存在或未生成物流单");
                errorList.add(excelDTO);
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
            if(StringUtils.isNotBlank(excelDTO.getTransportNo()) && !excelDTO.getTransportNo().equals(entity.getTransportNo())){
                LogisticsBillEntity existTransportEntity = logisticsBillEntityList.stream().filter(v->v.getTransportNo().equals(excelDTO.getTransportNo())).findFirst().orElse(null);
                if(Objects.nonNull(existTransportEntity)){
                    excelDTO.setErrorMsg("运单号已存在，不能修改");
                    errorList.add(excelDTO);
                    continue;
                }
            }
            //校验供应商和渠道
            List<String> errorMsgList = new ArrayList<>();
            if(StringUtils.isNotBlank(excelDTO.getSupplierName())){
                LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierEntityList.stream().filter(v->v.getSupplierName().equals(excelDTO.getSupplierName())).findFirst().orElse(null);
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
            }
            if(StringUtils.isNotBlank(excelDTO.getChannelName())){
                LogisticsChannelEntity logisticsChannelEntity = logisticsChannelEntityList.stream().filter(v->v.getName().equals(excelDTO.getChannelName())).findFirst().orElse(null);
                if(Objects.isNull(logisticsChannelEntity)){
                    errorMsgList.add("渠道不存在");
                }else{
                    entity.setChannelId(logisticsChannelEntity.getId());
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
                    errorMsgList.add(StrUtil.format("{}状态不能更新为待下单",Objects.isNull(nowStatusEnum)?"":nowStatusEnum.getName()));
                }

                if(FmLogisticTrackStatusEnum.ORDERED != nowStatusEnum &&  FmLogisticTrackStatusEnum.WAIT_ORDER != nowStatusEnum && FmLogisticTrackStatusEnum.ORDERED == statusEnum){
                    errorMsgList.add(StrUtil.format(StrUtil.format("{}状态不能更新为已下单",Objects.isNull(nowStatusEnum)?"":nowStatusEnum.getName())));
                }
                FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntityList.stream().filter(v->v.getId().equals(entity.getOutstockId())).findFirst().orElse(null);
                if(Objects.isNull(firstMileDeliveryEntity)){
                    errorMsgList.add("未找到对应的发货单");
                }
                if(!firstMileDeliveryEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && (statusEnum == FmLogisticTrackStatusEnum.TRACK_ING || statusEnum == FmLogisticTrackStatusEnum.ARRIVED ||statusEnum == FmLogisticTrackStatusEnum.SIGN ||statusEnum == FmLogisticTrackStatusEnum.INSPECTING )){
                    errorMsgList.add(StrUtil.format("关联单据{}尚未审核通过无法提交",firstMileDeliveryEntity.getCode()));
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
                        addTrackList.add(trackEntity);
                    }else{
                        if(StringUtils.isNotBlank(excelDTO.getCurrencyTrack())){
                            LogisticsTrackEntity trackEntity = new LogisticsTrackEntity();
                            trackEntity.setTrackNo(entity.getCounterNo());
                            trackEntity.setTrackTime(Objects.isNull(excelDTO.getStatusTime())? LocalDateTime.now():excelDTO.getStatusTime());
                            trackEntity.setStatus(logisticTrackStatusEnum.getCode());
                            trackEntity.setContent(excelDTO.getCurrencyTrack());
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
}
