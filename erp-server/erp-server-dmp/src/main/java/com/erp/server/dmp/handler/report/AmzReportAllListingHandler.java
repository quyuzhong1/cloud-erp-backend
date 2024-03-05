package com.erp.server.dmp.handler.report;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.entity.AmzReportInfoEntity;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.sdk.oms.amz.spapi.csv.ReportListingCsvEntity;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public void businessHandler(AmzReportTaskEntity taskEntity, AmzReportInfoEntity reportInfo, JSONArray jsonArray) {
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
        // 转换时间
        LocalDateTime parseTime = LocalDateTimeUtil.parse(taskEntity.getReqDataEndTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        jobTaskDTO.setUpdateTime(DateUtil.utcSamePlus8(parseTime));

        jobTaskDTO.setPlatformApiId(taskEntity.getReportId());
        jobTaskDTO.setPlatformCategory(PlatformCategoryEnum.THIRD_SYSTEM.getCode());
        jobTaskDTO.setBillType(BusinessTypeEnum.PRODUCT.getCode());
        jobTaskDTO.setOperateType("pull");

        List<ReportListingCsvEntity> list = JSONUtil.toList(jsonArray, ReportListingCsvEntity.class);
        // 日本/法国店铺特殊ASIN处理
//        if (AmazonMarketplaceEnum.JP.getMarketplaceId().equalsIgnoreCase(taskEntity.getFirstMarketplace()) ||
//            AmazonMarketplaceEnum.FR.getMarketplaceId().equalsIgnoreCase(taskEntity.getFirstMarketplace())
//        ){
            list.forEach(e -> {
                if (null != e.getProductIdType() && e.getProductIdType().contains("1") && StringUtils.isBlank(e.getAsin1())) {
                    e.setAsin1(e.getProductId());
                }
            });
//        }

        jobTaskDTO.setSourceList(list);

        PlatformApiEnum enumByType = PlatformApiEnum.getEnumByType(jobTaskDTO.getApiCode());
        RequestDTO dto = new RequestDTO();
        dto.setPlatformApiEnum(enumByType);
        dto.setJobTaskDTO(jobTaskDTO);

        // 事务处理
        businessService.pullProcessBusiness(jobTaskDTO.getPlatformCategory(), jobTaskDTO.getDictPlatform(), jobTaskDTO.getBillType(), jobTaskDTO, dto.getPlatformApiEnum());

    }
}
