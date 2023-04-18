package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.wms.dto.QcEffectivenessDTO;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.model.wms.enums.ViewQcTrendEnum;
import com.erp.server.wms.mapper.QcBillMapper;
import com.erp.server.wms.service.QcEffectivenessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/12 11:18
 */
@Service
public class QcEffectivenessServiceImpl implements QcEffectivenessService {

    @Resource
    private QcBillMapper qcBillMapper;

    @Override
    public QcEffectivenessDTO.ViewQcOverviewDTO viewQcOverview(QcEffectivenessDTO.CommonSearchParamDTO dto) {
        QcEffectivenessDTO.ViewQcOverviewDTO resultDTO = new QcEffectivenessDTO.ViewQcOverviewDTO();

        List<QcEffectivenessDTO.ViewQcOverviewDetailDTO> details = new ArrayList<>();

        //质检总览
        List<QcEffectivenessDTO.ViewQcOverviewDetailDTO> list = qcBillMapper.listQcBillGroupQcStatus(dto);

        //总计
        QcEffectivenessDTO.ViewQcOverviewDetailDTO totalDTO = new QcEffectivenessDTO.ViewQcOverviewDetailDTO();
        totalDTO.setType("总计");
        Integer totalCount = MathUtil.ZERO;
        if (CollectionUtils.isNotEmpty(list)) {
             totalCount = list.stream().map(QcEffectivenessDTO.ViewQcOverviewDetailDTO::getCount).reduce(MathUtil.ZERO, Integer::sum);
        }
        totalDTO.setCount(totalCount);
        details.add(totalDTO);

        Integer qcCount = MathUtil.ZERO;
        QcBillStatusEnum[] values = QcBillStatusEnum.values();
        for (QcBillStatusEnum qcBillStatusEnum : values) {
            QcEffectivenessDTO.ViewQcOverviewDetailDTO overviewDetailDTO = new QcEffectivenessDTO.ViewQcOverviewDetailDTO();
            Integer count = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(list)) {
                count = list.stream().filter(obj -> obj.getType().equals(qcBillStatusEnum.getCode())).map(QcEffectivenessDTO.ViewQcOverviewDetailDTO::getCount).findFirst().orElse(MathUtil.ZERO);
            }
            overviewDetailDTO.setCount(count);
            overviewDetailDTO.setType(qcBillStatusEnum.getName());
            if (QcBillStatusEnum.WAIT_QC.getCode().equals(qcBillStatusEnum.getCode())) {
                details.add(overviewDetailDTO);
            }
            if (QcBillStatusEnum.FINISH_QC.getCode().equals(qcBillStatusEnum.getCode()) || QcBillStatusEnum.EXEMPTION.getCode().equals(qcBillStatusEnum.getCode())) {
                details.add(overviewDetailDTO);
                qcCount += count;
            }
        }
        resultDTO.setList(details);
        String rate = MathUtil.compareTo(totalCount, MathUtil.ZERO) == 0 ? MathUtil.subtract(new BigDecimal(qcCount), new BigDecimal(totalCount)).multiply(MathUtil.BigDecimal_100) + "%" : "0%";
        resultDTO.setCompletionRate(rate);
        return resultDTO;
    }

    @Override
    public QcEffectivenessDTO.ViewQcTrendDTO viewQcTrend(QcEffectivenessDTO.ViewQcTrendSearchParamDTO dto) {
        QcEffectivenessDTO.ViewQcTrendDTO result = new  QcEffectivenessDTO.ViewQcTrendDTO();
        //日期集合
        List<String> dateList = new ArrayList<>();
        //暂存数量集合
        List<Integer> waitSubmitQtyList = new ArrayList<>();
        //待质检数量集合
        List<Integer> waitQcQtyList = new ArrayList<>();
        //质检（已质检、免检）数量集合
        List<Integer> qcQtyList = new ArrayList<>();
        //已取消数量集合
        List<Integer> cancelQtyList = new ArrayList<>();

        //是否是时间段，非时间段则默认取最近15（日，周，月）数据，是时间段则按时间段取质检数据
        Boolean isTimeSlot = dto.getIsTimeSlot();
        LocalDate beginDate = dto.getDateList().get(0);
        LocalDate endDate = dto.getDateList().get(1);

        //按日查询
        if (ViewQcTrendEnum.DAY.getCode().equals(dto.getType())) {
            if (!isTimeSlot) {
                beginDate = endDate.minusDays(15);
            }
        }
        //按周查询
        if (ViewQcTrendEnum.WEEK.getCode().equals(dto.getType())) {
            if (!isTimeSlot) {
                beginDate = endDate.minusWeeks(15);
            }
        }
        //按月查询
        if (ViewQcTrendEnum.MONTH.getCode().equals(dto.getType())) {
            if (!isTimeSlot) {
                beginDate = endDate.minusMonths(15);
            }
        }
        List<QcEffectivenessDTO.GroupQcTrendDTO> list =  qcBillMapper.listQcBillGroupQcTrend(dto.getType(),beginDate,endDate);
        if (CollectionUtils.isNotEmpty(list)) {
            Map<String, List<QcEffectivenessDTO.GroupQcTrendDTO>> map = list.stream().collect(Collectors.groupingBy(QcEffectivenessDTO.GroupQcTrendDTO::getDateStr));
            for (Map.Entry<String, List<QcEffectivenessDTO.GroupQcTrendDTO>> entry : map.entrySet()) {
                String key = entry.getKey();
                List<QcEffectivenessDTO.GroupQcTrendDTO> value = entry.getValue();
                dateList.add(key);
                //暂存
                Integer waitSubmitQty= value.stream().filter(obj -> QcBillStatusEnum.DRAFT.getCode().equals(obj.getStatus())).map(QcEffectivenessDTO.GroupQcTrendDTO::getCount).reduce(MathUtil.ZERO,Integer::sum);
                waitSubmitQtyList.add(waitSubmitQty);
                //待质检
                Integer waitQcQty = value.stream().filter(obj -> QcBillStatusEnum.WAIT_QC.getCode().equals(obj.getStatus())).map(QcEffectivenessDTO.GroupQcTrendDTO::getCount).reduce(MathUtil.ZERO,Integer::sum);
                waitQcQtyList.add(waitQcQty);
                //已质检、免检
                Integer qcQty = value.stream().filter(obj -> QcBillStatusEnum.EXEMPTION.getCode().equals(obj.getStatus()) || QcBillStatusEnum.FINISH_QC.getCode().equals(obj.getStatus())).map(QcEffectivenessDTO.GroupQcTrendDTO::getCount).reduce(MathUtil.ZERO,Integer::sum);
                qcQtyList.add(qcQty);
                //已取消
                Integer cancelQty= value.stream().filter(obj -> QcBillStatusEnum.CANCEL.getCode().equals(obj.getStatus())).map(QcEffectivenessDTO.GroupQcTrendDTO::getCount).reduce(MathUtil.ZERO,Integer::sum);
                cancelQtyList.add(cancelQty);
            }
        }
        result.setDateList(dateList);
        result.setWaitSubmitQtyList(waitSubmitQtyList);
        result.setWaitQcQtyList(waitQcQtyList);
        result.setQcQtyList(qcQtyList);
        result.setCancelQtyList(cancelQtyList);
        return result;
    }

    @Override
    public List<QcEffectivenessDTO.ViewQcForPersonnelDTO> viewQcForPersonnel(QcEffectivenessDTO.CommonSearchParamDTO dto) {
        return null;
    }

    @Override
    public List<QcEffectivenessDTO.ViewQcForDocumentDTO> viewQcForDocument(QcEffectivenessDTO.ViewQcForDocumentSearchParamDTO dto) {
        return null;
    }

    @Override
    public Boolean exportExcel(QcEffectivenessDTO.ExportExcelSearchParamDTO dto, HttpServletResponse response) {
        return null;
    }
}
