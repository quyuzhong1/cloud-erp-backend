package com.erp.server.dmp.inout.handler.input.task.init.api.aiya;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.AsnTypeEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.AiyaInboundQueryDTO;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.sdk.wms.aiya.dto.response.AiyaInboundResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 爱亚（AIYA/百世 GLINK）退货入库 DMP 输入 InitHandler，对齐 Wego 退货入库总流程、
 * 复用 {@link AiyaInboundInitHandler} 的 ASN 拉取方式。
 * <p>
 * 调用 {@code GLINK_BATCH_QUERY_ASN_NOTIFY}，固定 {@code asnType=RETURN}，按「仓库 + 收货时间」窗口分页拉取，
 * 仅保留「完全上架」单据（{@code status=Fulfilled} 且 {@code putawayStage=COMPLETED}）。
 * 明细取 {@code asnLineItems} 的 {@code putawayedQuantity} + {@code skuStatus}（GOOD/DAMAGE），
 * 交由下游落 {@code dmp_third_return_inbound}，再经 MQ 走现有海外仓退货入库/预入库生成逻辑。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaReturnInstockInitHandler extends AbstractAiyaInitHandler {

    private static final String ACTION = "批量查询退货入库单";

    /** 完全上架：单据状态 */
    private static final String STATUS_FULFILLED = "Fulfilled";

    /** 完全上架：上架阶段 */
    private static final String PUTAWAY_STAGE_COMPLETED = "COMPLETED";

    /** 不良品 SKU 状态（爱亚） */
    private static final String SKU_STATUS_DAMAGE = "DAMAGE";

    private static final int DEFAULT_PAGE_SIZE = AiyaInboundQueryDTO.DEFAULT_PAGE_SIZE;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        AiyaAuth auth = resolveAuth();
        String beginTime = resolveBeginTime();
        String endTime = resolveEndTime();

        List<String> warehouseCodes = resolveWarehouseCodes(auth.getAuthId());
        if (CollUtil.isEmpty(warehouseCodes)) {
            log.warn("[爱亚退货入库] 服务商[id={}] 未配置可用仓库，跳过批量查询", auth.getAuthId());
            return Collections.emptyList();
        }

        JSONArray result = new JSONArray();
        for (String warehouseCode : warehouseCodes) {
            int page = 1;
            while (page <= MAX_PAGE_LIMIT) {
                AiyaInboundResp resp;
                try {
                    resp = aiyaOpenApiService.batchQueryAsn(
                            buildQueryReq(auth, warehouseCode, page, beginTime, endTime));
                } catch (Exception e) {
                    log.error("[爱亚退货入库] 服务商[id={}] 仓库[{}] 收货时间[{} ~ {}] 调用异常, page={}",
                            auth.getAuthId(), warehouseCode, beginTime, endTime, page, e);
                    throw new ServiceException(e, ApiError.WH_AIYA_PAGE_QUERY_ERROR, ACTION, page);
                }
                if (resp == null) {
                    throw new ServiceException(ApiError.WH_AIYA_RESPONSE_EMPTY, ACTION);
                }
                if (!Boolean.TRUE.equals(resp.getSuccess())) {
                    throw new ServiceException(ApiError.WH_AIYA_RESPONSE_FAILED, ACTION,
                            resp.getCode(), resp.getMessage());
                }

                List<AiyaInboundResp.AsnInfoDTO> asnInfoList = resp.getAsnInfoList();
                int pageCount = asnInfoList == null ? 0 : asnInfoList.size();
                if (pageCount > 0) {
                    for (AiyaInboundResp.AsnInfoDTO asnInfo : asnInfoList) {
                        JSONObject normalized = normalize(asnInfo);
                        if (normalized != null) {
                            result.add(normalized);
                        }
                    }
                }
                if (pageCount < DEFAULT_PAGE_SIZE) {
                    break;
                }
                page++;
            }

            if (page > MAX_PAGE_LIMIT) {
                log.error("[爱亚退货入库] 服务商[id={}] 仓库[{}] 已达最大翻页上限({})，任务中止",
                        auth.getAuthId(), warehouseCode, MAX_PAGE_LIMIT);
                throw new ServiceException(ApiError.WH_AIYA_PAGE_LIMIT_EXCEEDED, ACTION, MAX_PAGE_LIMIT, result.size());
            }
        }

        if (result.isEmpty()) {
            log.warn("[爱亚退货入库] 服务商[id={}] 仓库{} 收货时间[{} ~ {}] 未拉到完全上架退货单",
                    auth.getAuthId(), warehouseCodes, beginTime, endTime);
            return Collections.emptyList();
        }
        log.warn("[爱亚退货入库] 服务商[id={}] 仓库{} 收货时间[{} ~ {}] 共拉取完全上架退货单={}条",
                auth.getAuthId(), warehouseCodes, beginTime, endTime, result.size());
        return Collections.singletonList(buildInitDTO(result, auth.getAuthId()));
    }

    private AiyaInboundQueryDTO.QueryReqDTO buildQueryReq(AiyaAuth auth, String warehouseCode, int page,
                                                         String beginTime, String endTime) {
        AiyaInboundQueryDTO.QueryReqDTO req = new AiyaInboundQueryDTO.QueryReqDTO();
        req.setAccessToken(auth.getPartnerId());
        req.setSecret(auth.getPartnerKey());
        req.setCustomerCode(auth.getCustomerCode());
        req.setWarehouseCode(warehouseCode);
        req.setPageNum(page);
        req.setPageSize(DEFAULT_PAGE_SIZE);
        req.setReceiveTimeFrom(beginTime);
        req.setReceiveTimeTo(endTime);
        req.setAsnType(AsnTypeEnum.RETURN.getCode());
        return req;
    }

    private List<String> resolveWarehouseCodes(String authId) {
        List<OverseasProviderWarehouseEntity> warehouseList = FeignQuery.create(OverseasProviderWarehouseEntity.class)
                .eq(OverseasProviderWarehouseEntity::getMainId, authId)
                .list();
        if (CollUtil.isEmpty(warehouseList)) {
            return Collections.emptyList();
        }
        return warehouseList.stream()
                .filter(e -> !Boolean.TRUE.equals(e.getDisabled()))
                .map(OverseasProviderWarehouseEntity::getPlatformWarehouseCode)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 仅保留完全上架退货单，并归一化为下游 DMP 映射字段；无有效上架明细时返回 null（整单跳过）。
     */
    private JSONObject normalize(AiyaInboundResp.AsnInfoDTO asnInfo) {
        if (asnInfo == null || CharSequenceUtil.isBlank(asnInfo.getAsnNumber())) {
            return null;
        }
        if (!STATUS_FULFILLED.equalsIgnoreCase(CharSequenceUtil.nullToEmpty(asnInfo.getStatus()).trim())
                || !PUTAWAY_STAGE_COMPLETED.equalsIgnoreCase(
                CharSequenceUtil.nullToEmpty(asnInfo.getPutawayStage()).trim())) {
            return null;
        }

        List<JSONObject> lineItems = new ArrayList<>();
        if (CollUtil.isNotEmpty(asnInfo.getAsnLineItems())) {
            for (AiyaInboundResp.AsnLineItemDTO line : asnInfo.getAsnLineItems()) {
                if (line == null || CharSequenceUtil.isBlank(line.getSku())) {
                    continue;
                }
                Integer putawayedQty = line.getPutawayedQuantity();
                if (putawayedQty == null || putawayedQty <= 0) {
                    continue;
                }
                String skuStatus = CharSequenceUtil.nullToEmpty(line.getSkuStatus()).trim().toUpperCase(Locale.ROOT);
                JSONObject item = new JSONObject();
                item.put("sku", line.getSku());
                item.put("putawayedQuantity", putawayedQty);
                item.put("skuStatus", skuStatus);
                item.put("lineNo", line.getLineNo());
                // 明细幂等键：单号 + 行号 + 良/不良，避免同 SKU 同数量良品/不良冲突
                item.put("thirdDetailId", asnInfo.getAsnNumber() + "_"
                        + CharSequenceUtil.nullToDefault(line.getLineNo(), "0") + "_"
                        + (SKU_STATUS_DAMAGE.equals(skuStatus) ? SKU_STATUS_DAMAGE : "GOOD"));
                lineItems.add(item);
            }
        }
        if (lineItems.isEmpty()) {
            log.warn("[爱亚退货入库] 退货单[{}] 无有效上架明细（putawayedQuantity>0），跳过", asnInfo.getAsnNumber());
            return null;
        }

        JSONObject obj = new JSONObject();
        obj.put("asnNumber", asnInfo.getAsnNumber());
        obj.put("refNumber", asnInfo.getRefNumber());
        obj.put("trackingNumber", asnInfo.getTrackingNumber());
        obj.put("warehouseCode", asnInfo.getWarehouseCode());
        obj.put("putawayTime", asnInfo.getPutawayTime());
        obj.put("receiveTime", asnInfo.getReceiveTime());
        obj.put("status", asnInfo.getStatus());
        obj.put("putawayStage", asnInfo.getPutawayStage());
        obj.put("asnLineItems", lineItems);
        return obj;
    }

    private String resolveBeginTime() {
        LocalDateTime startTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getStartTime();
        LocalDate beginDate = startTime != null ? startTime.toLocalDate() : LocalDate.now().minusDays(1);
        return beginDate.atStartOfDay().format(DATETIME_FORMATTER);
    }

    private String resolveEndTime() {
        LocalDateTime endTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getEndTime();
        LocalDate endDate = endTime != null ? endTime.toLocalDate() : LocalDate.now();
        return endDate.atTime(LocalTime.MAX).format(DATETIME_FORMATTER);
    }
}
