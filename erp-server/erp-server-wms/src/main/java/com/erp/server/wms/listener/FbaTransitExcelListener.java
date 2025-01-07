package com.erp.server.wms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.enums.UnitEnum;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.excel.FbaTransitExcelDTO;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaTransitCalculateDetailReportEntity;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.wms.service.FbaShipmentDetailService;
import com.erp.server.wms.service.FbaShipmentService;
import com.erp.server.wms.service.FbaTransitCalculateDetailReportService;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 期初头程分摊
 */
public class FbaTransitExcelListener extends AnalysisEventListener<FbaTransitExcelDTO> {
    //报表明细
    private final FbaTransitCalculateDetailReportService fbaTransitCalculateDetailReportService = SpringUtil.getBean(FbaTransitCalculateDetailReportService.class);
    private final FbaShipmentService fbaShipmentService = SpringUtil.getBean(FbaShipmentService.class);
    private final FbaShipmentDetailService fbaShipmentDetailService = SpringUtil.getBean(FbaShipmentDetailService.class);
    /**
     * 错误信息
     */
    @Getter
    private List<FbaTransitExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private final List<FbaTransitExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    @Getter
    private List<FbaTransitExcelDTO> successList = new ArrayList<>();

   /**
    * @description: 每解析一行数据回调一遍
    * @author zdy
    * @date: 2023/3/7 11:22
    * @param excelDTO 导入信息
    * @param analysisContext
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(FbaTransitExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        FbaShipmentEntity entity = fbaShipmentService.getByCode(excelDTO.getShipmentCode());
        if (Objects.isNull(entity)){
            errorMsgList.add(CharSequenceUtil.format("货件单号【{}】记录不存在",excelDTO.getShipmentCode()));
        }
        //校验数据是否已存在
        FbaShipmentDetailEntity detailEntity = fbaShipmentDetailService.getDetail(excelDTO.getShipmentCode(),excelDTO.getAsin(),excelDTO.getMsku());
        if (Objects.isNull(detailEntity)){
            errorMsgList.add(CharSequenceUtil.format("货件单号【{}】ASIN【{}】MSKU【{}】货件明细不存在",excelDTO.getShipmentCode(),excelDTO.getAsin(),excelDTO.getMsku()));
        }
        String reportMonthStr = excelDTO.getReportMonth();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate reportMonth = null;
        try {
            reportMonth = LocalDate.parse(reportMonthStr,formatter).withDayOfMonth(1);
        }catch (Exception e){
            errorMsgList.add(CharSequenceUtil.format("导入月份格式【yyyy-MM-dd】错误:【{}】",reportMonthStr));
        }
        if (Objects.nonNull(reportMonth)){
            List<FbaTransitCalculateDetailReportEntity> transitCalculateDetailReportEntityList = fbaTransitCalculateDetailReportService.listTransitDetail(null, excelDTO.getShipmentCode(), excelDTO.getAsin(), excelDTO.getMsku());
            if (CollUtil.isNotEmpty(transitCalculateDetailReportEntityList)){
                List<String> monthList = transitCalculateDetailReportEntityList.stream().map(FbaTransitCalculateDetailReportEntity::getReportMonthStr).distinct().collect(Collectors.toList());
                errorMsgList.add(CharSequenceUtil.format("在途货件单号【{}】ASIN【{}】MSKU【{}】记录已存在【{}】在途数据", excelDTO.getShipmentCode(), excelDTO.getAsin(), excelDTO.getMsku(), String.join(",",monthList)));
            }
//            //本月是否生成在途货件数据
//            String currentMonthStr = reportMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
//            List<FbaTransitCalculateDetailReportEntity> transitCalculateDetailReportEntityList1 = transitCalculateDetailReportEntityList.stream().filter(e -> Objects.equals(e.getReportMonthStr(), currentMonthStr)).collect(Collectors.toList());
//            if (CollUtil.isNotEmpty(transitCalculateDetailReportEntityList1)){
//                errorMsgList.add(CharSequenceUtil.format("本月【{}】在途货件单号【{}】ASIN【{}】MSKU【{}】记录已存在", currentMonthStr, excelDTO.getShipmentCode(), excelDTO.getAsin(), excelDTO.getMsku()));
//            }
//            //上月在途货件数据是否存在
//            LocalDate lastMonth = reportMonth.minusMonths(1).withDayOfMonth(1);
//            String lastMonthStr = lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
//            List<FbaTransitCalculateDetailReportEntity> transitCalculateDetailReportEntityList2 = transitCalculateDetailReportEntityList.stream().filter(e -> Objects.equals(e.getReportMonthStr(), lastMonthStr)).collect(Collectors.toList());
//            if (CollUtil.isNotEmpty(transitCalculateDetailReportEntityList2)){
//                errorMsgList.add(CharSequenceUtil.format("上月【{}】在途货件单号【{}】ASIN【{}】MSKU【{}】记录已存在", lastMonthStr, excelDTO.getShipmentCode(), excelDTO.getAsin(), excelDTO.getMsku()));
//            }
        }
        String initTransitQty = excelDTO.getInitTransitQty();
        try {
            Integer i = Integer.valueOf(initTransitQty);
        }catch (Exception e){
            errorMsgList.add(CharSequenceUtil.format("期初在途数字类型错误:【{}】",initTransitQty));
        }
        //判断记录是否已存在
        FbaTransitExcelDTO fbaTransitExcelDTO = dataList.stream().filter(e -> CharSequenceUtil.isNotBlank(excelDTO.getShipmentCode()) && excelDTO.getShipmentCode().equals(e.getShipmentCode())
                && CharSequenceUtil.isNotBlank(excelDTO.getAsin()) && excelDTO.getAsin().equals(e.getAsin())
                && CharSequenceUtil.isNotBlank(excelDTO.getMsku()) && excelDTO.getMsku().equals(e.getMsku())
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
        successList.add(excelDTO);
    }

    public List<FbaTransitExcelDTO> getExcelDateList(){
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
