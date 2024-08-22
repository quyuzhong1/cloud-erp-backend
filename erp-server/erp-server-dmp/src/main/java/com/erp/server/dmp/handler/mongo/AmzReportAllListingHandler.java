package com.erp.server.dmp.handler.mongo;

import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.entity.DmpMongoHandleTaskEntity;
import com.erp.sdk.oms.amz.spapi.dto.ReportListingMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportSuperMongoDTO;
import com.erp.server.dmp.handler.DmpMongoHandler;
import com.erp.server.dmp.service.DmpMongoHandleTaskService;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Listing报告处理服务
 *
 * @author Jim
 * @date 2024/1/24
 */
@Slf4j
@Component("amzReportAllListingHandler")
public class AmzReportAllListingHandler extends DmpMongoHandler {

    @Resource
    private BusinessServiceImpl businessService;
    @Resource
    private DmpMongoHandleTaskService dmpMongoHandleTaskService;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer findAndFillDataOrHandle(DmpMongoHandleTaskEntity mongoHandleTaskEntity, Boolean queryIsAddOrUpdate) {
        // 任务每次处理数量
        Integer handleCount = mongoHandleTaskEntity.getHandleCount();
        // 指定的mongo表
        String mongoTableName = MongoTableNameContant.DATA_REPORT_AMZ_LISTING;
        List<ReportListingMongoDTO> allList = dmpMongoHandleTaskService.findMongoData(mongoHandleTaskEntity.getLastId(), handleCount, mongoTableName, ReportListingMongoDTO.class, queryIsAddOrUpdate);
        if (CollectionUtils.isEmpty(allList)) {
            log.warn("Listing报告处理服务处理结束：处理数据为空:handleType={}", mongoHandleTaskEntity.getHandleType());
            return 0;
        }
        // 记录最大ID和下次执行时间
        String maxLastId = allList.stream().map(ReportSuperMongoDTO::getId).max(String::compareTo).orElse("0");
        dmpMongoHandleTaskService.updateMaxLastIdAndNextTime(mongoHandleTaskEntity, maxLastId);

        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
        jobTaskDTO.setApiCode("products");
        jobTaskDTO.setApiName("亚马逊Listing");
        jobTaskDTO.setIntervalTime(900);
        jobTaskDTO.setStatus(3);
        jobTaskDTO.setRetryTimes(0);
        jobTaskDTO.setCreateTime(LocalDateTime.now());
        jobTaskDTO.setPlatformCategory(PlatformCategoryEnum.THIRD_SYSTEM.getCode());
        jobTaskDTO.setBillType(BusinessTypeEnum.PRODUCT.getCode());
        jobTaskDTO.setOperateType("pull");
        allList.forEach(e -> {
            if (null != e.getProductIdType() && e.getProductIdType().contains("1") && StringUtils.isBlank(e.getAsin1())) {
                e.setAsin1(e.getProductId());
            }
        });

        jobTaskDTO.setSourceList(allList);
        PlatformApiEnum enumByType = PlatformApiEnum.getEnumByType(jobTaskDTO.getApiCode());
        RequestDTO dto = new RequestDTO();
        dto.setPlatformApiEnum(enumByType);
        dto.setJobTaskDTO(jobTaskDTO);

        // 事务处理
        businessService.pullProcessBusiness(jobTaskDTO.getPlatformCategory(), jobTaskDTO.getDictPlatform(), jobTaskDTO.getBillType(), jobTaskDTO, dto.getPlatformApiEnum());
        return allList.size();
    }
}
