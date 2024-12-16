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
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        FbaTransitExcelDTO addDTO = new FbaTransitExcelDTO();
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
            errorMsgList.add(CharSequenceUtil.format("货件单号【{}】ASIN【{}】MSKU【{}】记录不存在",excelDTO.getShipmentCode(),excelDTO.getAsin(),excelDTO.getMsku()));
        }
        LocalDate reportMonth = excelDTO.getReportMonth();
        //本月是否生成在途货件数据
        List<FbaTransitCalculateDetailReportEntity> transitCalculateDetailReportEntityList2 = fbaTransitCalculateDetailReportService.listTransitDetail(reportMonth, excelDTO.getShipmentCode(), excelDTO.getAsin(), excelDTO.getMsku());
        if (CollUtil.isNotEmpty(transitCalculateDetailReportEntityList2)){
            errorMsgList.add(CharSequenceUtil.format("本月【{}】在途货件单号【{}】ASIN【{}】MSKU【{}】记录已存在", reportMonth, excelDTO.getShipmentCode(), excelDTO.getAsin(), excelDTO.getMsku()));
        }
        //上月在途货件数据是否存在
        LocalDate lastMonth = reportMonth.minusMonths(1).withDayOfMonth(1);
        List<FbaTransitCalculateDetailReportEntity> transitCalculateDetailReportEntityList = fbaTransitCalculateDetailReportService.listTransitDetail(lastMonth, excelDTO.getShipmentCode(), excelDTO.getAsin(), excelDTO.getMsku());
        if (CollUtil.isNotEmpty(transitCalculateDetailReportEntityList)){
            errorMsgList.add(CharSequenceUtil.format("上月【{}】在途货件单号【{}】ASIN【{}】MSKU【{}】记录已存在", lastMonth, excelDTO.getShipmentCode(), excelDTO.getAsin(), excelDTO.getMsku()));
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
        successList.add(addDTO);
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
