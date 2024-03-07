package com.erp.server.dmp.handler.report;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.*;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.entity.AmzReportInfoEntity;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.sdk.oms.amz.spapi.csv.ReportFulfilledShipmentsCsvEntity;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 亚马逊物流销售报告处理服务
 *
 * @author Jim
 * @date 2024/03/06
 */
@Slf4j
@Component("amzReportFulfilledShipmentsHandler")
public class AmzReportFulfilledShipmentsHandler extends AmzReportBusinessHandler {

    @Resource
    private BusinessServiceImpl businessService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void businessHandler(AmzReportTaskEntity taskEntity, AmzReportInfoEntity reportInfo, JSONArray jsonArray) {
        log.debug("亚马逊物流销售报告处理服务处理：jsonArray={}", JSONUtil.toJsonStr(jsonArray));
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setShopId(taskEntity.getShopId());
        jobTaskDTO.setShopName(taskEntity.getShopId());
        jobTaskDTO.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
        jobTaskDTO.setApiCode(BusinessTypeEnum.SO_OUT_STOCK.getCode());
        jobTaskDTO.setIntervalTime(86400);
        jobTaskDTO.setStatus(3);
        jobTaskDTO.setRetryTimes(0);
        jobTaskDTO.setApiName("亚马逊物流销售");
        jobTaskDTO.setCreateTime(LocalDateTime.now());
        // 转换时间
        LocalDateTime parseTime = LocalDateTimeUtil.parse(taskEntity.getReqDataEndTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        jobTaskDTO.setUpdateTime(DateUtil.utcSamePlus8(parseTime));

        jobTaskDTO.setPlatformApiId(taskEntity.getReportId());
        jobTaskDTO.setPlatformCategory(PlatformCategoryEnum.THIRD_SYSTEM.getCode());
        jobTaskDTO.setBillType(BusinessTypeEnum.SO_OUT_STOCK.getCode());
        jobTaskDTO.setOperateType("pull");

        List<ReportFulfilledShipmentsCsvEntity> list = JSONUtil.toList(jsonArray, ReportFulfilledShipmentsCsvEntity.class);
        jobTaskDTO.setSourceList(list);

        RequestDTO dto = new RequestDTO();
        dto.setJobTaskDTO(jobTaskDTO);

        // 事务处理
        businessService.pullProcessBusiness(jobTaskDTO.getPlatformCategory(), jobTaskDTO.getDictPlatform(), jobTaskDTO.getBillType(), jobTaskDTO, dto.getPlatformApiEnum());
    }
}
