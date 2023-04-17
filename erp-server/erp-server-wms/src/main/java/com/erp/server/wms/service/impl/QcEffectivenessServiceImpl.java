package com.erp.server.wms.service.impl;

import com.erp.model.wms.dto.QcEffectivenessDTO;
import com.erp.server.wms.mapper.QcBillMapper;
import com.erp.server.wms.service.QcEffectivenessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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
    public List<QcEffectivenessDTO.ViewQcOverviewDTO> viewQcOverview(QcEffectivenessDTO.CommonSearchParamDTO dto) {
        List<QcEffectivenessDTO.ViewQcOverviewDTO> resultList = new ArrayList<>();
        //质检总览
        List<QcEffectivenessDTO.ViewQcOverviewDetailDTO> list = qcBillMapper.listQcBillGroupQcStatus(dto);


        return null;
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
