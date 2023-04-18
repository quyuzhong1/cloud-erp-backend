package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.wms.dto.QcEffectivenessDTO;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.server.wms.mapper.QcBillMapper;
import com.erp.server.wms.service.QcEffectivenessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    public List<QcEffectivenessDTO.ViewQcTrendDTO> viewQcTrend(QcEffectivenessDTO.ViewQcTrendSearchParamDTO dto) {
        return null;
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
