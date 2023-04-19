package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.QcBillEntity;
import com.erp.server.wms.mapper.QcBillMapper;
import com.erp.server.wms.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 质检单表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Service
public class QcBillServiceImpl extends SuperServiceImpl<QcBillMapper, QcBillEntity> implements QcBillService {


    @Resource
    private QcProductService qcProductService;

    @Resource
    private QcInfoService qcInfoService;

    @Resource
    private QcReportDetailService qcReportDetailService;

    @Resource
    private QcBillRemarkService qcBillRemarkService;

    /**
     * 暂存 质检单
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean draft(QcBillDTO.SaveOrUpdateDTO dto) {
        //质检单
        QcBillEntity bill = new QcBillEntity();
        String billId = dto.getId();
        if (StringUtils.isBlank(billId)) {
            billId = IdWorker.getIdStr();
        }
        BeanMapper.copy(dto, bill);
        bill.setId(billId);
        Boolean result = this.saveOrUpdate(bill);
        if (result) {
            //质检产品 暂存
            qcProductService.draft(billId, dto.getQcProduct());
            //质检信息 暂存
            qcInfoService.draft(billId, dto.getQcInfo());
            //质检报告 暂存
            qcReportDetailService.draft(billId, dto.getReportDetailList());
            //质检备注暂存
            qcBillRemarkService.draft(billId, dto.getRemarkList());
        }
        return result;
    }

    @Override
    public List<QcBillEntity> listByPoIds(List<String> poIds) {
        return lambdaQuery().in(QcBillEntity::getPurchaseOrderId, poIds).list();
    }


    /**
     * 质检单详情
     *
     * @param id
     * @return com.erp.model.wms.dto.QcBillDTO.ViewDTO
     * @author yl
     * @date 2023-04-19 11:53
     */
    @Override
    public QcBillDTO.ViewDTO view(String id) {
        QcBillEntity bill = this.getById(id);
        if (Objects.isNull(bill)) {
            throw new ServiceException(ApiError.ERROR_99015);
        }
        QcBillDTO.ViewDTO view = new QcBillDTO.ViewDTO();
        BeanMapper.copy(bill, view);
        //产品信息
        QcProductDTO.ViewDTO qcProduct = qcProductService.getByMainId(id);
        view.setQcProduct(qcProduct);

        //质检信息
        QcInfoDTO.ViewDTO qcInfo = qcInfoService.getByMainId(id);
        view.setQcInfo(qcInfo);

        //质检报告 信息
        List<QcReportDetailDTO.ViewDTO> reportDetailList = qcReportDetailService.getByMainId(id);
        view.setReportDetailList(reportDetailList);

        //质检备注
        List<QcRemarkDTO.AddDTO> remarkList = qcBillRemarkService.getByMainId(id);
        view.setRemarkList(remarkList);

        return view;
    }
}
