package com.erp.server.dmp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DmpSyncReportScheduleDTO;
import com.erp.model.dmp.entity.ReportScheduleEntity;
import com.erp.model.dmp.enums.ReportScheduleCancelStatusEnum;
import com.erp.model.dmp.enums.ReportScheduleSubscribedStatusEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import com.erp.server.dmp.mapper.ReportScheduleMapper;
import com.erp.server.dmp.service.ReportScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 亚马逊报告计划表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
@Slf4j
@Service
public class ReportScheduleServiceImpl extends SuperServiceImpl<ReportScheduleMapper, ReportScheduleEntity> implements ReportScheduleService {

    @Override
    public boolean existByReportScheduleId(String reportScheduleId) {
        Integer count = lambdaQuery()
                .eq(ReportScheduleEntity::getReportScheduleId, reportScheduleId)
                .count();
        return count > 0;
    }

    @Override
    public ReportScheduleEntity getByReportScheduleId(String reportScheduleId) {
        return lambdaQuery()
                .eq(ReportScheduleEntity::getReportScheduleId, reportScheduleId)
                .last("LIMIT 1")
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addReportSchedule(DmpSyncReportScheduleDTO dto) {
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(dto.getDictCountryCode());
        // 组合
        List<ReportScheduleEntity> entityList = Stream.of(AmazonReportRecordTypeEnum.values())
                .map(e -> new ReportScheduleEntity(e.getRecordType(), marketplaceEnum.getMarketplaceId(), dto.getShopId()))
                .collect(Collectors.toList());
        if (!this.saveBatch(entityList)) {
            throw new ServiceException("批量添加报告计划失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelReportSchedule(DmpSyncReportScheduleDTO dto) {
        List<ReportScheduleEntity> list = lambdaQuery()
                .eq(ReportScheduleEntity::getShopId, dto.getShopId())
                // 未取消
                .eq(ReportScheduleEntity::getCancelStatus, ReportScheduleCancelStatusEnum.NONE.getCode())
                // 查询非未订阅
                .ne(ReportScheduleEntity::getSubscribedStatus, ReportScheduleSubscribedStatusEnum.NOT.getCode())
                .list();
        if (CollectionUtils.isEmpty(list)){
            return true;
        }
        // 设置待取消
        list.forEach(e-> e.setCancelStatus(ReportScheduleCancelStatusEnum.WAIT.getCode()));
        if (!this.updateBatchById(list)) {
            throw new ServiceException("批量更新报告计划失败");
        }
        return true;
    }
}
