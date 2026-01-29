package com.erp.server.dmp.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.dto.excel.FirstMileInTransitInitExcelDTO;
import com.erp.model.dmp.entity.doris.AdsErpFirstMileInTransitDiffEntity;
import com.erp.server.dmp.service.AdsErpFirstMileInTransitDiffService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 期初
 */
@Slf4j
public class FirstMileInTransitInitExcelListener extends AnalysisEventListener<FirstMileInTransitInitExcelDTO> {
    //报表明细
    private final AdsErpFirstMileInTransitDiffService adsErpFirstMileInTransitDiffService = SpringUtil.getBean(AdsErpFirstMileInTransitDiffService.class);

    /**
     * 错误信息
     */
    @Getter
    private List<FirstMileInTransitInitExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private final List<FirstMileInTransitInitExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    @Getter
    private List<FirstMileInTransitInitExcelDTO> successList = new ArrayList<>();

   /**
    * 每解析一行数据回调一遍
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(FirstMileInTransitInitExcelDTO excelDTO, AnalysisContext analysisContext) {
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
        //校验数据是否已存在
        AdsErpFirstMileInTransitDiffEntity entity = shipmentList.stream()
                .filter(e -> e.getPlatformSkuNo().equals(excelDTO.getPlatformSkuNo())
                        && (StringUtils.isBlank(excelDTO.getAsin()) || e.getPlatformSpuNo().equals(excelDTO.getAsin())))
                .findFirst()
                .orElse(null);
        if (Objects.isNull(entity)){
            errorMsgList.add(CharSequenceUtil.format("货件单号【{}】ASIN【{}】MSKU【{}】货件明细不存在",excelDTO.getShipmentCode(),excelDTO.getAsin(),excelDTO.getPlatformSkuNo()));
        }
        // 解析日期
        LocalDate reportMonth = LocalDateUtil.parseCheckLocalDate(excelDTO.getReportMonth(), errorMsgList);
        if (Objects.nonNull(reportMonth)){
            List<AdsErpFirstMileInTransitDiffEntity> collect = shipmentList.stream()
                    .filter(e -> e.getPlatformSkuNo().equals(excelDTO.getPlatformSkuNo())
                            && (StringUtils.isBlank(excelDTO.getAsin()) || e.getPlatformSpuNo().equals(excelDTO.getAsin())))
                    .filter(e -> e.getReceiveQty() > 0)
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(collect)){
                List<String> monthList = collect.stream().map(AdsErpFirstMileInTransitDiffEntity::getCheckMonth).distinct().collect(Collectors.toList());
//                errorMsgList.add(CharSequenceUtil.format("在途货件单号【{}】ASIN【{}】MSKU【{}】记录已存在【{}】在途数据", excelDTO.getShipmentCode(), excelDTO.getAsin(), excelDTO.getPlatformSkuNo(), String.join(",",monthList)));
                errorMsgList.add(CharSequenceUtil.format("在途货件单号【{}】ASIN【{}】MSKU【{}】存在【{}】本期签收不允许导入期初", excelDTO.getShipmentCode(), excelDTO.getAsin(), excelDTO.getPlatformSkuNo(), String.join(",",monthList)));
            }
        }
        String initTransitQty = excelDTO.getInitTransitQty();
        try {
            Integer i = Integer.valueOf(initTransitQty);
        }catch (Exception e){
            errorMsgList.add(CharSequenceUtil.format("期初在途数字类型错误:【{}】",initTransitQty));
        }
        //判断记录是否已存在
        FirstMileInTransitInitExcelDTO fbaTransitExcelDTO = dataList.stream().filter(
                e -> CharSequenceUtil.isNotBlank(excelDTO.getShipmentCode()) && excelDTO.getShipmentCode().equals(e.getShipmentCode())
                && (StringUtils.isBlank(excelDTO.getAsin()) || e.getAsin().equals(excelDTO.getAsin()))
                && excelDTO.getPlatformSkuNo().equals(e.getPlatformSkuNo())
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

    public List<FirstMileInTransitInitExcelDTO> getExcelDateList(){
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
