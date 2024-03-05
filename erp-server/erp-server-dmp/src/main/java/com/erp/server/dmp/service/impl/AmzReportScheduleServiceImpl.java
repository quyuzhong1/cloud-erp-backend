package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonJobParamDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.entity.AmzReportScheduleEntity;
import com.erp.model.dmp.entity.CfgAmzReportTypeEntity;
import com.erp.model.dmp.enums.ReportScheduleCancelStatusEnum;
import com.erp.model.dmp.enums.ReportScheduleSubscribedStatusEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.CreateReportScheduleSpecification;
import com.erp.server.dmp.mapper.AmzReportScheduleMapper;
import com.erp.server.dmp.service.AmzReportScheduleService;
import com.erp.server.dmp.service.CfgAmzReportTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 亚马逊报告计划表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
 */
@Slf4j
@Service
public class AmzReportScheduleServiceImpl extends SuperServiceImpl<AmzReportScheduleMapper, AmzReportScheduleEntity> implements AmzReportScheduleService {

    @Resource
    private CfgAmzReportTypeService cfgAmzReportTypeService;

    @Override
    public boolean existByReportScheduleId(String reportScheduleId) {
        Integer count = lambdaQuery()
                .eq(AmzReportScheduleEntity::getAmzReportScheduleId, reportScheduleId)
                .count();
        return count > 0;
    }

    @Override
    public AmzReportScheduleEntity getByReportScheduleId(String reportScheduleId) {
        return lambdaQuery()
                .eq(AmzReportScheduleEntity::getAmzReportScheduleId, reportScheduleId)
                .last("LIMIT 1")
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addOrUpdateReportSchedule(PlatformTaskDTO.DisabledDTO dto) {
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(dto.getDictCountryCode());
        // 查询报告类型配置
        List<CfgAmzReportTypeEntity> reportTypeConfiglist = cfgAmzReportTypeService.findActive(null);
        if (CollectionUtils.isEmpty(reportTypeConfiglist)) {
            return true;
        }
        List<String> allReportTypeList = reportTypeConfiglist.stream().map(CfgAmzReportTypeEntity::getReportType).distinct().collect(Collectors.toList());

        // 根据店铺id查询是否已经存在计划任务
        List<AmzReportScheduleEntity> existEntityList = lambdaQuery()
                .eq(AmzReportScheduleEntity::getShopId, dto.getShopId())
                .in(AmzReportScheduleEntity::getReportType, allReportTypeList)
                .list();

        // 对比当前店铺不存在的任务
        Set<String> existReportTypeList = existEntityList.stream().map(AmzReportScheduleEntity::getReportType).collect(Collectors.toSet());
        // 需要添加的任务
        List<CfgAmzReportTypeEntity> notExistReportTypeList = reportTypeConfiglist
                .stream()
                .filter(item -> !existReportTypeList.contains(item.getReportType()))
                .collect(Collectors.toList());
        // 新增
        if (CollectionUtil.isNotEmpty(notExistReportTypeList)) {
            List<AmzReportScheduleEntity> insertEntityList = notExistReportTypeList.stream()
                    .filter(e-> e.getCountryList().contains(marketplaceEnum.getCountryCode()))
                    .map(e -> new AmzReportScheduleEntity(e.getReportType(), marketplaceEnum.getMarketplaceId(),
                            dto.getShopId(),
                            e.getPeriod(),
                            ""
                    ))
                    .collect(Collectors.toList());
            if (!this.saveBatch(insertEntityList)) {
                throw new ServiceException("批量添加报告计划失败");
            }
        }
        // 需要更新的任务
        if (CollectionUtil.isNotEmpty(existEntityList)) {
            Map<String, CfgAmzReportTypeEntity> configMap = reportTypeConfiglist
                    .stream()
                    .collect(Collectors.toMap(CfgAmzReportTypeEntity::getReportType, Function.identity()));
            existEntityList.forEach(e-> {
                CfgAmzReportTypeEntity config = configMap.get(e.getReportType());
                if (null == config){
                    throw new ServiceException("报告数据异常：reportType=" + e.getReportType());
                }
                boolean disabled = config.getDisabled() || !config.getCountryList().contains(marketplaceEnum.getCountryCode());
                // 根据配置决定最终状态
                e.setCancelStatus(disabled ? ReportScheduleCancelStatusEnum.CANCEL.getCode() : ReportScheduleCancelStatusEnum.NONE.getCode());
                e.setSubscribedStatus(disabled ? ReportScheduleSubscribedStatusEnum.NOT.getCode() : ReportScheduleSubscribedStatusEnum.ALREADY.getCode());
                e.setSubscribedType(config.getSubscribedType());
            });
            this.updateBatchById(existEntityList);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelReportSchedule(String shopId) {
        List<AmzReportScheduleEntity> list = lambdaQuery()
                .eq(AmzReportScheduleEntity::getShopId, shopId)
                // 未取消
                .eq(AmzReportScheduleEntity::getCancelStatus, ReportScheduleCancelStatusEnum.NONE.getCode())
                // 查询非未订阅
                .ne(AmzReportScheduleEntity::getSubscribedStatus, ReportScheduleSubscribedStatusEnum.NOT.getCode())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        // 设置待取消
        list.forEach(e -> e.setCancelStatus(ReportScheduleCancelStatusEnum.WAIT.getCode()));
        if (!this.updateBatchById(list)) {
            throw new ServiceException("批量更新报告计划失败");
        }
        return true;
    }

    @Override
    public List<AmzReportScheduleEntity> findList(String subscribedStatus, String cancelStatus, Integer size) {
        return lambdaQuery()
                .eq(AmzReportScheduleEntity::getSubscribedStatus, subscribedStatus)
                .eq(AmzReportScheduleEntity::getCancelStatus, cancelStatus)
                .orderByAsc(AmzReportScheduleEntity::getId)
                .last(" LIMIT " + size)
                .list()
                ;
    }

    @Override
    public List<AmzReportScheduleEntity> listByParams(String subscribedStatus, String cancelStatus, List<String> subscribedTypeList, List<String> recordTypeList, List<String> shopIds, LocalDateTime minTime) {
        return this.lambdaQuery()
                // 已订阅
                .eq(AmzReportScheduleEntity::getAmzReportScheduleId, "")
                // 已订阅
                .eq(AmzReportScheduleEntity::getSubscribedStatus, subscribedStatus)
                // 未取消
                .eq(AmzReportScheduleEntity::getCancelStatus, cancelStatus)
                // 手动类型
                .in(AmzReportScheduleEntity::getSubscribedType, subscribedTypeList)
                // 指定类型
                .in(!CollectionUtils.isEmpty(recordTypeList), AmzReportScheduleEntity::getReportType, recordTypeList)
                // 指定店铺
                .in(AmzReportScheduleEntity::getShopId, shopIds)
                // 下次创建时间小于等于当前
                .le(null != minTime, AmzReportScheduleEntity::getFirstNextReportCreationTime, minTime)
                .orderByAsc(AmzReportScheduleEntity::getId)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNextTime(String mainId, CfgAmzReportTypeEntity config) {
        AmzReportScheduleEntity reportSchedule = this.getByIdOpt(mainId).orElseThrow(() -> new ServiceException("未找到计划任务ID=" + mainId));
        // 支持切换时间间隔
        CreateReportScheduleSpecification.PeriodEnum periodEnum = CreateReportScheduleSpecification.PeriodEnum.getByCode(reportSchedule.getPeriod());
        OffsetDateTime roundedOffsetDateTime;
        // 是否是全量报告
        if (config.getIsFullUpdate()){
            roundedOffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC);
        } else {
            // 之前的时间
            LocalDateTime historyTime = reportSchedule.getFirstNextReportCreationTime();
            roundedOffsetDateTime = periodEnum.formatTime(historyTime.atOffset(ZoneOffset.of("+8")).withOffsetSameInstant(ZoneOffset.UTC));
        }

        // 修改下次创建时间
        LocalDateTime nextTime = periodEnum.plusPeriod(roundedOffsetDateTime)
                .withOffsetSameInstant(BusinessCommonConstants.systemZoneOffset)
                .toLocalDateTime();
        reportSchedule.setFirstNextReportCreationTime(nextTime);
        if (!this.updateById(reportSchedule)) {
            throw new ServiceException("[reportSchedule] 更新失败");
        }
    }

    @Override
    public List<AmzReportScheduleEntity> findActionList(List<ShopInfoEntity> shopInfoEntityList, List<CfgAmzReportTypeEntity> reportTypeConfigList, AmazonJobParamDTO.ReportJobDTO jobParamDTO, List<String> subscribedTypeList) {
        List<String> shopIds =  shopInfoEntityList.stream().map(ShopInfoEntity::getId).collect(Collectors.toList());
        // (任务参数优选)
        List<String> recordTypeList;
        // (任务参数优选)
        if (!CollectionUtils.isEmpty(jobParamDTO.getRecordTypeList())) {
            recordTypeList = jobParamDTO.getRecordTypeList();
        } else {
            recordTypeList = reportTypeConfigList.stream().map(CfgAmzReportTypeEntity::getReportType).distinct().collect(Collectors.toList());
        }
        LocalDateTime minTime = LocalDateTime.now(ZoneId.systemDefault());
        if (null != jobParamDTO.getIgnoreNextReportCreationTime() && jobParamDTO.getIgnoreNextReportCreationTime()) {
            minTime = null;
        }

        return this.listByParams(
                ReportScheduleSubscribedStatusEnum.ALREADY.getCode(),
                ReportScheduleCancelStatusEnum.NONE.getCode(),
                subscribedTypeList,
                recordTypeList,
                shopIds,
                minTime
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelById(String mainId) {
        boolean update = lambdaUpdate()
                .eq(AmzReportScheduleEntity::getId, mainId)
                .set(AmzReportScheduleEntity::getCancelStatus, ReportScheduleSubscribedStatusEnum.WAIT.getCode())
                .update();
        if (!update){
            log.warn("取消任务计划失败:id={}", mainId);
        }
    }
}
