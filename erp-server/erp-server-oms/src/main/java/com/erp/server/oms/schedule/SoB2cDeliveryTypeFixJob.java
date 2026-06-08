package com.erp.server.oms.schedule;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.OrderLogisticTypeEnum;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.model.wms.enums.SoB2cWarehouseDeliveryStatusEnum;
import com.erp.rpc.wms.feign.ThirdWarehouseDeliveryFeign;
import com.erp.server.oms.service.SoB2cService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * B2C销售订单发货类型历史数据修复.
 */
@Component
@Slf4j
public class SoB2cDeliveryTypeFixJob {

    private static final long DEFAULT_PAGE_SIZE = 1000L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private SoB2cService soB2cService;

    @Resource
    private ThirdWarehouseDeliveryFeign thirdWarehouseDeliveryFeign;

    @XxlJob("fixSoB2cDeliveryTypeJob")
    public ReturnT<String> fixSoB2cDeliveryTypeJob() {
        String jobParam = XxlJobHelper.getJobParam();
        JobParam param = parseJobParam(jobParam);
        if (param == null) {
            XxlJobHelper.log("fixSoB2cDeliveryTypeJob 参数为空或格式错误，请传 yyyy-MM-dd,yyyy-MM-dd 或 JSON");
            return ReturnT.FAIL;
        }
        long totalUpdated = 0L;
        XxlJobHelper.log("fixSoB2cDeliveryTypeJob 开始执行，startDate={}，endDate={}，pageSize={}",
                param.getStartDate(), param.getEndDate(), param.getPageSize());
        for (LocalDate date = param.getStartDate(); !date.isAfter(param.getEndDate()); date = date.plusDays(1)) {
            long current = 1L;
            long dayUpdated = 0L;
            XxlJobHelper.log("fixSoB2cDeliveryTypeJob 开始处理日期 {}", date);
            while (true) {
                Page<SoB2cEntity> page = soB2cService.lambdaQuery()
                        .select(SoB2cEntity::getId, SoB2cEntity::getCode, SoB2cEntity::getDictPlatform,
                                SoB2cEntity::getLabelJson, SoB2cEntity::getDeliveryType)
                        .ge(SoB2cEntity::getCreateTime, date.atStartOfDay())
                        .lt(SoB2cEntity::getCreateTime, date.plusDays(1).atStartOfDay())
                        .page(new Page<>(current, param.getPageSize()));
                List<SoB2cEntity> records = page.getRecords();
                if (CollectionUtils.isEmpty(records)) {
                    XxlJobHelper.log("fixSoB2cDeliveryTypeJob 日期{}无更多数据", date);
                    break;
                }

                Map<String, String> thirdWarehouseSoIdMap = listActiveThirdWarehouseSoIdMap(records);
                int updated = updateDeliveryType(records, thirdWarehouseSoIdMap);
                dayUpdated += updated;
                totalUpdated += updated;
                XxlJobHelper.log("fixSoB2cDeliveryTypeJob 日期{} 第{}/{}页处理{}条，更新{}条，当天累计{}条，总累计{}条",
                        date, current, page.getPages(), records.size(), updated, dayUpdated, totalUpdated);
                if (current >= page.getPages()) {
                    break;
                }
                current++;
            }
            XxlJobHelper.log("fixSoB2cDeliveryTypeJob 日期{}处理完成，当天更新{}条", date, dayUpdated);
        }
        XxlJobHelper.log("fixSoB2cDeliveryTypeJob 执行完成，总更新{}条", totalUpdated);
        return ReturnT.SUCCESS;
    }

    private JobParam parseJobParam(String jobParam) {
        if (StringUtils.isBlank(jobParam)) {
            return null;
        }
        try {
            if (JSONUtil.isTypeJSON(jobParam)) {
                JSONObject json = JSONUtil.parseObj(jobParam);
                return buildJobParam(json.getStr("startDate"), json.getStr("endDate"), json.getLong("pageSize", DEFAULT_PAGE_SIZE));
            }
            String[] params = StringUtils.split(jobParam, ",");
            if (params == null || params.length < 2) {
                return null;
            }
            long pageSize = params.length > 2 ? Long.parseLong(StringUtils.trim(params[2])) : DEFAULT_PAGE_SIZE;
            return buildJobParam(StringUtils.trim(params[0]), StringUtils.trim(params[1]), pageSize);
        } catch (Exception e) {
            XxlJobHelper.log("fixSoB2cDeliveryTypeJob 解析参数失败，param={}，error={}", jobParam, e.getMessage());
            return null;
        }
    }

    private JobParam buildJobParam(String startDate, String endDate, long pageSize) {
        if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)) {
            return null;
        }
        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
        if (start.isAfter(end)) {
            XxlJobHelper.log("fixSoB2cDeliveryTypeJob 开始日期不能大于结束日期，startDate={}，endDate={}", startDate, endDate);
            return null;
        }
        return new JobParam(start, end, pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE);
    }

    private Map<String, String> listActiveThirdWarehouseSoIdMap(List<SoB2cEntity> records) {
        List<String> soIds = records.stream()
                .map(SoB2cEntity::getId)
                .collect(Collectors.toList());
        List<ThirdWarehouseDeliveryEntity> thirdWarehouseDeliveryList = thirdWarehouseDeliveryFeign.listBySourceId(soIds);
        if (CollectionUtils.isEmpty(thirdWarehouseDeliveryList)) {
            return Collections.emptyMap();
        }
        return thirdWarehouseDeliveryList.stream()
                .filter(entity -> StringUtils.isNotBlank(entity.getSoId()))
                .filter(entity -> !SoB2cWarehouseDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(entity.getStatus()))
                .collect(Collectors.toMap(ThirdWarehouseDeliveryEntity::getSoId, ThirdWarehouseDeliveryEntity::getId, (v1, v2) -> v1));
    }

    private int updateDeliveryType(List<SoB2cEntity> records, Map<String, String> thirdWarehouseSoIdMap) {
        Map<String, List<String>> deliveryTypeToIds = new HashMap<>();
        for (SoB2cEntity entity : records) {
            String deliveryType = resolveDeliveryType(entity, thirdWarehouseSoIdMap);
            if (StringUtils.equals(deliveryType, entity.getDeliveryType())) {
                continue;
            }
            deliveryTypeToIds.computeIfAbsent(deliveryType, key -> new ArrayList<>()).add(entity.getId());
        }
        int updated = 0;
        for (Map.Entry<String, List<String>> entry : deliveryTypeToIds.entrySet()) {
            List<String> ids = entry.getValue();
            for (int i = 0; i < ids.size(); i += 500) {
                List<String> batch = ids.subList(i, Math.min(i + 500, ids.size()));
                boolean success = soB2cService.lambdaUpdate()
                        .set(SoB2cEntity::getDeliveryType, entry.getKey())
                        .in(SoB2cEntity::getId, batch)
                        .update();
                if (success) {
                    updated += batch.size();
                }
            }
        }
        return updated;
    }

    private String resolveDeliveryType(SoB2cEntity entity, Map<String, String> thirdWarehouseSoIdMap) {
        if (entity.hasPlatformWarehouseOrder()) {
            return OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode();
        }
        if (thirdWarehouseSoIdMap.containsKey(entity.getId())) {
            return OrderLogisticTypeEnum.THIRD_WAREHOUSE.getCode();
        }
        return OrderLogisticTypeEnum.SELF_SHIPMENT.getCode();
    }

    private static class JobParam {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final long pageSize;

        private JobParam(LocalDate startDate, LocalDate endDate, long pageSize) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.pageSize = pageSize;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public long getPageSize() {
            return pageSize;
        }
    }
}
