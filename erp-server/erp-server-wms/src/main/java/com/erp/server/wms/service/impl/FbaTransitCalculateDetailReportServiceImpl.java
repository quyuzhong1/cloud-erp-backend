package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.FbaTransitCalculateReportDTO;
import com.erp.model.wms.entity.FbaTransitCalculateDetailReportEntity;
import com.erp.server.wms.mapper.FbaTransitCalculateDetailReportMapper;
import com.erp.server.wms.service.FbaTransitCalculateDetailReportService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-12-12
 */
@Slf4j
@Service
public class FbaTransitCalculateDetailReportServiceImpl extends SuperServiceImpl<FbaTransitCalculateDetailReportMapper, FbaTransitCalculateDetailReportEntity> implements FbaTransitCalculateDetailReportService {
    @Autowired
    private OperateLogService operateLogService;
    @Override
    public List<FbaTransitCalculateDetailReportEntity> listTransitDetail(LocalDate reportMonth, String shipmentCode, String asin, String msku) {
        if (CharSequenceUtil.isBlank(shipmentCode) || CharSequenceUtil.isBlank(asin) || CharSequenceUtil.isBlank(msku)){
            return Collections.emptyList();
        }
        return baseMapper.listTransitDetail(reportMonth, shipmentCode, asin, msku);
    }

    @Override
    public void updateAdjustQty(FbaTransitCalculateReportDTO.AdjustDTO adjustDTO, FbaTransitCalculateDetailReportEntity detailReportEntity) {
        int afterAdjustQty = detailReportEntity.getEndPeriodTransitQty() + adjustDTO.getAdjustQty();
        if (afterAdjustQty < 0){
            throw new ServiceException("期末在途(调整后)不能小于0");
        }
        this.lambdaUpdate().eq(FbaTransitCalculateDetailReportEntity::getId, adjustDTO.getDetailId())
                .set(FbaTransitCalculateDetailReportEntity::getEndPeriodTransitAdjustQty,adjustDTO.getAdjustQty())
                .set(FbaTransitCalculateDetailReportEntity::getAfterEndPeriodTransitQty,afterAdjustQty)
                .set(FbaTransitCalculateDetailReportEntity::getAdjustReason,adjustDTO.getAdjustReason())
                .set(FbaTransitCalculateDetailReportEntity::getAdjustTime, LocalDateTime.now())
                .set(FbaTransitCalculateDetailReportEntity::getAdjustUserId, UserContext.getDefaultLoginUser().getUid())
                .set(FbaTransitCalculateDetailReportEntity::getAdjustUserName, UserContext.getDefaultLoginUser().getUserName())
                .update();
        String msg = CharSequenceUtil.format("编辑期末在途调整数量从【{}】变为【{}】,调整原因：【{}】", detailReportEntity.getAfterEndPeriodTransitQty(),afterAdjustQty, adjustDTO.getAdjustReason());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_TRANSIT_CALCULATE_REPORT.getCode(), detailReportEntity.getId(), "编辑操作");
    }
}
