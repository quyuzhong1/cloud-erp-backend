package com.erp.server.tms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.excel.TmsCfgSailingExcelDTO;
import com.erp.model.tms.entity.DictBasicEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.entity.TmsCfgSailingEntity;
import com.erp.model.tms.enums.DictBasicEnum;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class TmsCfgSailingExcelListener extends AnalysisEventListener<TmsCfgSailingExcelDTO> {
    /**
     * 错误信息
     */
    @Getter
    private List<TmsCfgSailingExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private final List<TmsCfgSailingExcelDTO> allList = new ArrayList<>();

    /**
     * 成功信息
     */
    @Getter
    private List<TmsCfgSailingEntity> successList = new ArrayList<>();

    //物流商信息
    private List<LogisticsSupplierEntity> logisticsSupplierList;
    //渠道信息
    private List<LogisticsChannelEntity> logisticsChannelList;
    //字典表
    private List<DictBasicEntity> dictList;

    public TmsCfgSailingExcelListener(List<LogisticsSupplierEntity> logisticsSupplierList,List<LogisticsChannelEntity> logisticsChannelList,List<DictBasicEntity> dictList) {
        this.logisticsSupplierList = CollectionUtils.isEmpty(logisticsSupplierList) ? Collections.emptyList() : logisticsSupplierList;

        this.logisticsChannelList = CollectionUtils.isEmpty(logisticsChannelList) ? Collections.emptyList() : logisticsChannelList;

        this.dictList = CollectionUtils.isEmpty(dictList) ? Collections.emptyList() : dictList;
    }

    DateTimeFormatter dataForamt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    DateTimeFormatter timeForamt = DateTimeFormatter.ofPattern("HH:mm:ss");

   /**
    * @description:
    * @author jack
    * @date: 2025-07-18
    * @param excelDTO 导入信息
    * @param analysisContext
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(TmsCfgSailingExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        TmsCfgSailingEntity entity = new TmsCfgSailingEntity();
        //物流商
        String logisticsSupplierName = excelDTO.getLogisticsSupplierName();
        LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierList.stream().filter(l -> l.getSupplierName().equals(logisticsSupplierName) || l.getShortName().equals(logisticsSupplierName)).findFirst().orElse(null);
        if(Objects.nonNull(logisticsSupplierEntity)){
            entity.setLogisticsSupplierId(logisticsSupplierEntity.getId());
        }else {
            errorMsgList.add("物流商不存在");
        }
        //渠道
        String logisticsChannelName = excelDTO.getLogisticsChannelName();
        LogisticsChannelEntity logisticsChannelEntity = logisticsChannelList.stream().filter(l -> l.getMainId().equals(entity.getLogisticsSupplierId()) && l.getName().equals(logisticsChannelName)).findFirst().orElse(null);
        if(Objects.nonNull(logisticsChannelEntity)){
            entity.setLogisticsChannelId(logisticsChannelEntity.getId());
        }else {
            errorMsgList.add("物流渠道不存在");
        }

        entity.setDateValue(excelDTO.getDateValue());

        //截单周期单位
        String dateTypeName = excelDTO.getDateTypeName();
        String dateType = dictList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), "dateType") && CharSequenceUtil.equals(obj.getName(), dateTypeName))
                .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");
        if (StringUtils.isNotBlank(dateType)) {
            entity.setDateType(dateType);
        }else {
            errorMsgList.add("截单周期单位不存在");
        }
        //截单日
        String endDateName = excelDTO.getEndDateName();
        String endDate = dictList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(),dateType) && CharSequenceUtil.equals(obj.getName(), endDateName))
                .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");
        if (StringUtils.isNotBlank(endDate)) {
            entity.setEndDate(Integer.parseInt(endDate));
        }else {
            errorMsgList.add("截单日错误或不存在");
        }
        //截单时间
        String endStr = excelDTO.getEndTime();
        try {
            // 统计冒号数量
            long colonCount = endStr.chars().filter(ch -> ch == ':').count();
            // 如果只有一个冒号(格式为HH:mm)，则补全秒数
            if (colonCount == 1) {
                endStr = endStr + ":00";
            }
            LocalTime endTime = StringUtils.isBlank(endStr) ? null : LocalTime.parse(endStr, timeForamt);
            entity.setEndTime(endTime);
        }catch (Exception e){
            errorMsgList.add("截单时间格式错误");
        }
        //开船日
        String satartDateName = excelDTO.getStartDateName();
        String satartDate = dictList.stream().filter(obj ->CharSequenceUtil.equals(obj.getType(),dateType) &&  CharSequenceUtil.equals(obj.getName(), satartDateName))
                .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse("");
        if (StringUtils.isNotBlank(satartDate)) {
            entity.setStartDate(Integer.parseInt(satartDate));
        }else {
            errorMsgList.add("开船日错误或不存在");
        }
        //开船时间
        String startStr = excelDTO.getStartTime();
        try {
            // 统计冒号数量
            long colonCount = startStr.chars().filter(ch -> ch == ':').count();
            // 如果只有一个冒号(格式为HH:mm)，则补全秒数
            if (colonCount == 1) {
                startStr = startStr + ":00";
            }
            LocalTime startTime = StringUtils.isBlank(startStr) ? null : LocalTime.parse(startStr, timeForamt);
            entity.setStartTime(startTime);
        }catch (Exception e){
            errorMsgList.add("开船时间格式错误");
        }
        //起始日
        String effectiveDateStr = excelDTO.getEffectiveDate();
        try {
            LocalDate effectiveDate = StringUtils.isBlank(effectiveDateStr) ? null : LocalDate.parse(effectiveDateStr, dataForamt);
            entity.setEffectiveDate(effectiveDate);
        }catch (Exception e){
            errorMsgList.add("起始日格式错误");
        }
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        //添加数据用于判断是否为空
        allList.add(excelDTO);
        successList.add(entity);
    }

    public List<TmsCfgSailingExcelDTO> getExcelDateList(){
        return allList;
    }
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
