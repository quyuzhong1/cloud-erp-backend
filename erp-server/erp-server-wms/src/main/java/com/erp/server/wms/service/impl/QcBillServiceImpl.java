package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.QcBillDTO;
import com.erp.model.wms.entity.QcBillEntity;
import com.erp.server.wms.mapper.QcBillMapper;
import com.erp.server.wms.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

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
    private QcReportService qcReportService;

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
        String billId = bill.getId();
        if (StringUtils.isBlank(billId)) {
            billId = IdWorker.getIdStr();
        }
        BeanMapper.copy(dto, bill);
        //质检产品 暂存
        qcProductService.draft(billId, dto.getQcProduct());
        //质检信息 暂存
        qcInfoService.draft(billId, dto.getQcInfo());
        //质检报告 暂存
        qcReportService.draft(billId, dto.getReportDetailList());
        //质检备注暂存
        qcBillRemarkService.draft(billId,dto.getRemarkList());
        return true;
    }

    @Override
    public List<QcBillEntity> listByPoIds(List<String> poIds) {
        return lambdaQuery().in(QcBillEntity::getPurchaseOrderId, poIds).list();
    }
}
