package com.erp.server.oms.schedule;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoReceiptDetailEntity;
import com.erp.model.oms.entity.SoReceiptEntity;
import com.erp.server.oms.service.InvoiceInfoService;
import com.erp.server.oms.service.SoInfoService;
import com.erp.server.oms.service.SoReceiptDetailService;
import com.erp.server.oms.service.SoReceiptService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 生成历史收款单
 */
@Component
@Slf4j
public class HistoryReceiptJob {

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SoReceiptService soReceiptService;

    @Resource
    private SoReceiptDetailService soReceiptDetailService;


    @Resource
    private DocNoGenHelper docNoGenHelper;

    @XxlJob("HistoryReceiptJob")
    public ReturnT<String> HistoryReceiptJob() throws Exception {
        List<SoInfoEntity> soInfoEntityList = soInfoService.list();
        List<SoReceiptEntity> soReceiptEntityList = new ArrayList<>();
        List<SoReceiptDetailEntity> soReceiptDetailEntities = new ArrayList<>();
        soInfoEntityList = soInfoEntityList.stream().filter(v->v.getReceiveAmount().compareTo(BigDecimal.ZERO)> 0 ).collect(Collectors.toList());
        for (SoInfoEntity soInfoEntity : soInfoEntityList) {
            SoReceiptEntity soReceiptEntity = new SoReceiptEntity();
            String idStr = IdWorker.getIdStr();
            soReceiptEntity.setId(idStr);
            // 生成单号
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SKD);
            soReceiptEntity.setCode(code);
            soReceiptEntity.setCustomerId(soInfoEntity.getCustomerId());
            soReceiptEntity.setApproveStatus(ApproveStatusEnum.APPROVE);
            soReceiptEntity.setCurrency(soInfoEntity.getCurrency());
            soReceiptEntity.setApproveTime(LocalDateTime.now());
            soReceiptEntity.setApproveUserId("0");
            soReceiptEntity.setRemark("历史数据生成");
            soReceiptEntity.setReceiptAmount(soInfoEntity.getReceiveAmount());
            soReceiptEntity.setDictReceiptMethod(soInfoEntity.getReceiveMethod());
            soReceiptEntity.setReceiptAccount(soInfoEntity.getReceiveAccount());
            soReceiptEntity.setReceiptDate(soInfoEntity.getBillDate());
            soReceiptEntity.setSalesOrgId(soInfoEntity.getSalesOrgId());
            soReceiptEntityList.add(soReceiptEntity);

            SoReceiptDetailEntity soReceiptDetailEntity = new SoReceiptDetailEntity();
            soReceiptDetailEntity.setMainId(idStr);
            soReceiptDetailEntity.setSoId(soInfoEntity.getId());
            soReceiptDetailEntity.setSoCode(soInfoEntity.getCode());
            soReceiptDetailEntity.setSourceDetailId(soInfoEntity.getSourceId());
            soReceiptDetailEntity.setRemark("历史数据生成");
            soReceiptDetailEntity.setReceiptAmount(soInfoEntity.getReceiveAmount());
            soReceiptDetailEntities.add(soReceiptDetailEntity);

        }
        soReceiptService.saveBatch(soReceiptEntityList);
        soReceiptDetailService.saveBatch(soReceiptDetailEntities);

        return ReturnT.SUCCESS;
    }

}
