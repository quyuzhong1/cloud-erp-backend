package com.erp.server.dmp.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.dmp.dto.excel.FirstMileInTransitAdjustExcelDTO;
import com.erp.model.dmp.dto.excel.FirstMileInTransitInitExcelDTO;
import com.erp.model.dmp.entity.doris.AdsErpFirstMileInTransitDiffEntity;
import com.erp.server.dmp.service.AdsErpFirstMileInTransitDiffService;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 调整
 */
public class FirstMileInTransitAdjustExcelListener extends AnalysisEventListener<FirstMileInTransitAdjustExcelDTO> {
    //报表明细
    private final AdsErpFirstMileInTransitDiffService adsErpFirstMileInTransitDiffService = SpringUtil.getBean(AdsErpFirstMileInTransitDiffService.class);

    /**
     * 错误信息
     */
    @Getter
    private List<FirstMileInTransitAdjustExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private final List<FirstMileInTransitAdjustExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    @Getter
    private List<FirstMileInTransitAdjustExcelDTO> successList = new ArrayList<>();

   /**
    * 每解析一行数据回调一遍
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(FirstMileInTransitAdjustExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //校验货件单号是否存在
        List<AdsErpFirstMileInTransitDiffEntity> shipmentList = adsErpFirstMileInTransitDiffService.lambdaQuery()
                .eq(AdsErpFirstMileInTransitDiffEntity::getShipmentCode, excelDTO.getShipmentCode())
                .list();
        if (CollectionUtils.isEmpty(shipmentList)) {
            errorMsgList.add(CharSequenceUtil.format("货件单号【{}】记录不存在",excelDTO.getShipmentCode()));
        }
        long count = shipmentList.stream()
                .filter(e -> e.getExecStatus().equals("doing"))
                .count();
        if (count > 0){
            errorMsgList.add(CharSequenceUtil.format("货件单号【{}】存在执行中的头程在途差异调整单，请等处理完成后再导入",excelDTO.getShipmentCode()));
        }
        //校验数据是否已存在
        AdsErpFirstMileInTransitDiffEntity entity = shipmentList.stream()
                .filter(e -> (e.getPlatformSkuNo().equals(excelDTO.getPlatformSkuNo()) || e.getPlatformStockSku().equals(excelDTO.getPlatformSkuNo()))
                && (StringUtils.isBlank(excelDTO.getAsin()) || e.getPlatformSpuNo().equals(excelDTO.getAsin()))
                && e.getCheckMonthQuery().contains(excelDTO.getReportMonth()))
                .findFirst()
                .orElse(null);
        if (null == entity){
            errorMsgList.add(CharSequenceUtil.format("货件单号【{}】ASIN【{}】MSKU【{}】货件明细不存在",excelDTO.getShipmentCode(),excelDTO.getAsin(),excelDTO.getPlatformSkuNo()));
        }
        String reportMonthStr = excelDTO.getReportMonth();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate reportMonth = null;
        try {
            reportMonth = LocalDate.parse(reportMonthStr,formatter).withDayOfMonth(1);
        }catch (Exception e){
            errorMsgList.add(CharSequenceUtil.format("导入月份格式【yyyy-MM-dd】错误:【{}】",reportMonthStr));
        }
        // 上个月1日
        LocalDate beforeMonth = LocalDate.now().withDayOfMonth(1).minusMonths(1);
        if (null != reportMonth && reportMonth.isBefore(beforeMonth)){
            errorMsgList.add(CharSequenceUtil.format("导入月份错误:只能是上个月之后的数据:【{}】",reportMonthStr));
        }
        String adjustQty = excelDTO.getAdjustQty();
        try {
            Integer i = Integer.valueOf(adjustQty);
        }catch (Exception e){
            errorMsgList.add(CharSequenceUtil.format("期初在途数字类型错误:【{}】",adjustQty));
        }
        //判断记录是否已存在
        FirstMileInTransitAdjustExcelDTO fbaTransitExcelDTO = dataList.stream().filter(e -> CharSequenceUtil.isNotBlank(excelDTO.getShipmentCode()) && excelDTO.getShipmentCode().equals(e.getShipmentCode())
                && (StringUtils.isBlank(excelDTO.getAsin()) || e.getAsin().equals(excelDTO.getAsin()))
                && CharSequenceUtil.isNotBlank(excelDTO.getPlatformSkuNo()) && excelDTO.getPlatformSkuNo().equals(e.getPlatformSkuNo())
        ).findFirst().orElse(null);
        if (Objects.nonNull(fbaTransitExcelDTO)){
            errorMsgList.add("重复记录");
        }
        //添加数据用于判断是否为空
        dataList.add(excelDTO);
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        excelDTO.setEntity(entity);
        successList.add(excelDTO);
    }

    public List<FirstMileInTransitAdjustExcelDTO> getExcelDateList(){
        return dataList;
    }

    /**
     * @description: 数据全部解析完后删除明细
     * @author zdy
     * @date: 2024/8/14 15:32
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
