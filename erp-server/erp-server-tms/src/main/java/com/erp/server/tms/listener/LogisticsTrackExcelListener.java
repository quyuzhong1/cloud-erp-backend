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
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.excel.LogisticsTrackExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.DetailReconciliationTypeEnum;
import com.erp.model.tms.enums.FmLogisticTrackStatusEnum;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
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

public class LogisticsTrackExcelListener extends AnalysisEventListener<LogisticsTrackExcelDTO> {
    private final LogisticsBillService logisticsBillService = SpringUtil.getBean(LogisticsBillService.class);
    private final LogisticsBillDetailService logisticsBillDetailService = SpringUtil.getBean(LogisticsBillDetailService.class);
    private final static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Getter
    private List<LogisticsTrackExcelDTO> dataList = new ArrayList<>();

    @Getter
    private List<LogisticsTrackExcelDTO> errorList = new ArrayList<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(LogisticsTrackExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        try {
            excelDTO.setStatusTime(LocalDateUtil.stringToLocalDateTime(excelDTO.getStatusTimeStr()));
        }catch (Exception e){
            errorMsgList.add("状态时间格式错误");
        }
        try {
            String trackStatus = LogisticTrackStatusEnum.getCode(excelDTO.getTrackStatusName());
            if (CharSequenceUtil.isNotBlank(trackStatus)){
                excelDTO.setTrackStatus(trackStatus);
            }else {
                errorMsgList.add("运输状态名称错误");
            }
        }catch (Exception e){
            errorMsgList.add("运输状态错误");
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
        List<String> trackNoList = dataList.stream().map(LogisticsTrackExcelDTO::getTrackNo).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = logisticsBillService.listLogisticsBillVoByTrackNo(trackNoList);
        List<String> billIds = logisticsBillVos.stream().map(LogisticsBillDTO.LogisticsBillVo::getId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<LogisticsBillDetailEntity> logisticsBillDetailEntityList = logisticsBillDetailService.listByMainIds(billIds);
        List<LogisticsBillDetailEntity> updateDetailList = new ArrayList<>();
        List<LogisticsTrackEntity> addTrackList = new ArrayList<>();
        for (LogisticsTrackExcelDTO excelDTO : dataList) {
            //校验数据
            List<LogisticsBillDTO.LogisticsBillVo> entityList = logisticsBillVos.stream().filter(v -> {
                if (CharSequenceUtil.isNotBlank(excelDTO.getCode()) && excelDTO.getCode().equals(v.getSourceCode())
                        && CharSequenceUtil.isNotBlank(excelDTO.getOutstockCode()) && excelDTO.getOutstockCode().equals(v.getOutstockCode())
                        && excelDTO.getTrackNo().equals(v.getTrackNo())){
                    return true;
                }else if (CharSequenceUtil.isNotBlank(excelDTO.getCode()) && excelDTO.getCode().equals(v.getSourceCode())
                        && excelDTO.getTrackNo().equals(v.getTrackNo())){
                    return true;
                }else if (CharSequenceUtil.isNotBlank(excelDTO.getOutstockCode()) && excelDTO.getOutstockCode().equals(v.getOutstockCode())
                        && excelDTO.getTrackNo().equals(v.getTrackNo())){
                    return true;
                }else return excelDTO.getTrackNo().equals(v.getTrackNo());
            }).collect(Collectors.toList());
            if (CollUtil.isEmpty(entityList)) {
                excelDTO.setErrorMsg(CharSequenceUtil.format("订单号【{}】跟踪单号单号【{}】未匹配到物流单", excelDTO.getCode(), excelDTO.getTrackNo()));
                errorList.add(excelDTO);
                continue;
            }else if (entityList.size() > 1) {
                excelDTO.setErrorMsg(CharSequenceUtil.format("订单号【{}】出库单号【{}】跟踪单号【{}】匹配到多条物流单", excelDTO.getCode(), excelDTO.getOutstockCode(), excelDTO.getTrackNo()));
                errorList.add(excelDTO);
                continue;
            }
            LogisticsBillDTO.LogisticsBillVo logisticsBillVo = entityList.get(0);
            LogisticsBillDetailEntity detailEntity = logisticsBillDetailEntityList.stream().filter(v->v.getMainId().equals(logisticsBillVo.getId())).findFirst().orElse(null);
            if(Objects.isNull(detailEntity)){
                excelDTO.setErrorMsg("物流单明细不存在");
                errorList.add(excelDTO);
                continue;
            }
            if (CharSequenceUtil.isBlank(logisticsBillVo.getTransportNo())){
                excelDTO.setErrorMsg(CharSequenceUtil.format("订单号【{}】出库单号【{}】跟踪单号【{}】关联的物流单运单号不能为空", excelDTO.getCode(), excelDTO.getOutstockCode(), excelDTO.getTrackNo()));
                errorList.add(excelDTO);
                continue;
            }
            List<String> errorMsgList = new ArrayList<>();
            if(StringUtils.isNotBlank(excelDTO.getTrackStatusName())){
                //校验物流状态
                FmLogisticTrackStatusEnum statusEnum = EnumMessage.getByName(FmLogisticTrackStatusEnum.class,(excelDTO.getTrackStatusName()));
                if(statusEnum == FmLogisticTrackStatusEnum.ORDERED && StringUtils.isBlank(logisticsBillVo.getChannelId())){
                    errorMsgList.add("已下单但尚未填写渠道信息，请填写后更新");
                }
                if(statusEnum == FmLogisticTrackStatusEnum.SIGN && StringUtils.isBlank(logisticsBillVo.getTransportNo())){
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

            }

            if (!errorMsgList.isEmpty()) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            if(StringUtils.isNotBlank(excelDTO.getTrackStatusName())){
                String trackStatus = LogisticTrackStatusEnum.getCode(excelDTO.getTrackStatusName());
                if(CharSequenceUtil.isNotBlank(trackStatus)){
                    //待下单不用封装轨迹，其他状态需要
                    if(LogisticTrackStatusEnum.WAIT_ORDER.getCode().equals(trackStatus)){
                        logisticsBillVo.setOrderTime(null);
                    }else if (LogisticTrackStatusEnum.ORDERED.getCode().equals(trackStatus)){

                        logisticsBillVo.setOrderTime(Objects.isNull(excelDTO.getStatusTime())? LocalDateTime.now():excelDTO.getStatusTime());

                        LogisticsTrackEntity trackEntity = new LogisticsTrackEntity();
                        trackEntity.setTrackNo(logisticsBillVo.getTransportNo());
                        trackEntity.setTrackTime(Objects.isNull(excelDTO.getStatusTime())? LocalDateTime.now():excelDTO.getStatusTime());
                        trackEntity.setStatus(trackStatus);
                        trackEntity.setContent(StringUtils.isBlank(excelDTO.getTrackDesc())?"已下单":excelDTO.getTrackDesc());
                        trackEntity.setMd5(getDataMd5(trackEntity));
                        addTrackList.add(trackEntity);
                    }else{
                        LogisticsTrackEntity trackEntity = new LogisticsTrackEntity();
                        trackEntity.setTrackNo(logisticsBillVo.getTransportNo());
                        trackEntity.setTrackTime(Objects.isNull(excelDTO.getStatusTime())? LocalDateTime.now():excelDTO.getStatusTime());
                        trackEntity.setStatus(trackStatus);
                        trackEntity.setContent(CharSequenceUtil.isNotBlank(excelDTO.getTrackDesc()) ? excelDTO.getTrackDesc() : "");
                        trackEntity.setMd5(getDataMd5(trackEntity));
                        addTrackList.add(trackEntity);
                    }
                    if (LogisticTrackStatusEnum.SIGN.getCode().equals(trackStatus)){
                        detailEntity.setSignTime(Objects.isNull(excelDTO.getStatusTime())? LocalDateTime.now():excelDTO.getStatusTime());
                    }
                    detailEntity.setTrackContent(excelDTO.getTrackDesc());
                    detailEntity.setTrackTime((excelDTO.getStatusTime()));
                    detailEntity.setTrackStatus(trackStatus);
                    updateDetailList.add(detailEntity);
                }
            }
        }
        logisticsBillService.updateImport(updateDetailList,addTrackList);
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
