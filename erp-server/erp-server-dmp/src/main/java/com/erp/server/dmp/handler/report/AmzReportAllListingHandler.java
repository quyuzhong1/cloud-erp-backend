package com.erp.server.dmp.handler.report;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.JobTaskDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.sdk.oms.amz.spapi.csv.ReportListingCsvEntity;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Listing报告处理服务
 *
 * @author Jim
 * @date 2024/1/24
 */
@Component("amzReportAllListingHandler")
public class AmzReportAllListingHandler extends AmzReportBusinessHandler {

    @Resource
    private BusinessServiceImpl businessService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void businessHandler(AmzReportTaskEntity taskEntity, JSONArray jsonArray) {
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setShopId(taskEntity.getShopId());
        jobTaskDTO.setShopName(taskEntity.getShopId());
        jobTaskDTO.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
        jobTaskDTO.setApiCode("products");
        jobTaskDTO.setApiName("亚马逊Listing");
        jobTaskDTO.setIntervalTime(900);
        jobTaskDTO.setStatus(3);
        jobTaskDTO.setRetryTimes(0);
        jobTaskDTO.setCreateTime(LocalDateTime.now());
        jobTaskDTO.setUpdateTime(taskEntity.getReqDataEndTime());

        jobTaskDTO.setPlatformApiId(taskEntity.getReportId());
        jobTaskDTO.setPlatformCategory(PlatformCategoryEnum.THIRD_SYSTEM.getCode());
        jobTaskDTO.setBillType(BusinessTypeEnum.PRODUCT.getCode());
        jobTaskDTO.setOperateType("pull");

        List<ReportListingCsvEntity> list = JSONUtil.toList(jsonArray, ReportListingCsvEntity.class);
        jobTaskDTO.setSourceList(list);
        // 事务处理
        businessService.pullProcessBusiness(jobTaskDTO.getPlatformCategory(), jobTaskDTO.getDictPlatform(), jobTaskDTO.getBillType(), jobTaskDTO);

    }
}
