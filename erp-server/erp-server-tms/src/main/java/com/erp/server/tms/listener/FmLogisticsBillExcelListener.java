package com.erp.server.tms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.excel.FmLogisticsBillExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.FmLogisticTrackStatusEnum;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
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
        List<LogisticsBillEntity> logisticsBillEntityList = tmsFirstMileLogisticService.listByOutstcockCode(outstockCodeList);
        List<String> mainIdList = logisticsBillEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<LogisticsBillDetailEntity> logisticsBillDetailEntityList = logisticsBillDetailService.listByMainIds(mainIdList);
        List<LogisticsSupplierEntity> logisticsSupplierEntityList = logisticsSupplierService.listByName(supplierNameList);
        List<LogisticsChannelEntity> logisticsChannelEntityList = logisticsChannelService.listByName(channelNameList);
        List<LogisticsBillEntity> updateList = new ArrayList<>();
        List<LogisticsBillDetailEntity> updateDetailList = new ArrayList<>();
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
            List<String> errorMsgList = new ArrayList<>();
            if(StringUtils.isNotBlank(excelDTO.getSupplierName())){
                LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierEntityList.stream().filter(v->v.getSupplierName().equals(excelDTO.getSupplierName())).findFirst().orElse(null);
                if(Objects.isNull(logisticsSupplierEntity)){
                    errorMsgList.add("供应商不存在");
                }else{
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
            if (!errorMsgList.isEmpty()) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                return;
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
                        if(Objects.nonNull(excelDTO.getStatusTime())){
                            entity.setOrderTime(excelDTO.getStatusTime());
                        }
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
                    detailEntity.setTrackStatus(logisticTrackStatusEnum.getCode());
                    updateDetailList.add(detailEntity);
                }
            }
            updateList.add(entity);
        }
        tmsFirstMileLogisticService.updateImport(updateList,updateDetailList,addTrackList);
    }
}
