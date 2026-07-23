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
import java.util.stream.Collectors;

/**
 * 爱亚（AIYA/百世 GLINK）海外仓入库单验货明细回传 InitHandler，对齐 {@code WegoInboundInitHandler}。
 * <p>
 * 调用爱亚 {@code GLINK_BATCH_QUERY_ASN_NOTIFY}，按「仓库 + 收货时间」范围（
 * {@code receiveTimeFrom}/{@code receiveTimeTo}）分页批量拉取入库单
 * （爱亚要求 createdTime/receiveTime/lastUpdatedTime/asnNumbers/refNumbers 至少一组非空，
 * putawayCompletedTime 不满足该约束，故用收货时间窗口增量拉取）：
 * <ol>
 *   <li>按 {@link #dmpInputTaskEntity} 的 {@code nextLevelId} 定位已授权爱亚服务商（复用父类 {@link #resolveAuth()}）；</li>
 *   <li>时间窗口优先取 dmp 任务 startTime/endTime，缺省回退 [今天-1天 ~ 今天]（与 wego 一致）；</li>
 *   <li>warehouseCode 为该接口必填，按服务商已配置仓库逐仓分页（pageSize 最大 200，无 total/pages 元数据，
 *       按「返回条数 &lt; pageSize」判断末页），循环翻页至末页；</li>
 *   <li>归一化字段（receivingCode/sourceCode/receivingStatus/asnItems）；签收明细取自
 *       {@code asnItemReceiveDetails} 中 detailId 前缀为 PV 的上架流水（RV 收货流水数量与之重复，不取），
 *       并把 ASN 级 receiveTime、asnNumber 回填到每行，交由下游 mongo/{@code AiyaInBoundDmpHandler}
 *       落 {@code dmp_third_inbound}。</li>
 * </ol>
 * <p>
 * PV 上架流水的 {@code skuStatus} 区分良品（GOOD）/不良品（DAMAGE），{@code putawayQty} 为上架数量，
 * 下游据此把良品/不良品分别写入签收记录、直接调拨单与即时库存。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaInboundInitHandler extends AbstractAiyaInitHandler {

    private static final String ACTION = "批量查询入库单";

    /**
     * 爱亚 {@code asnItemReceiveDetails} 上架流水 detailId 前缀（上架，签收以此为准，取 putawayQty；
     * 前缀为 {@code RV} 的收货流水数量与之重复，签收不取）。
     */
    private static final String PUTAWAY_DETAIL_ID_PREFIX = "PV";

    /**
     * 单页拉取条数（GLINK_BATCH_QUERY_ASN_NOTIFY 文档约束 pageSize 最大 200）。
     */
    private static final int DEFAULT_PAGE_SIZE = AiyaInboundQueryDTO.DEFAULT_PAGE_SIZE;

    /**
     * 爱亚时间参数格式（yyyy-MM-dd HH:mm:ss）。
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        AiyaAuth auth = resolveAuth();

        // 爱亚隐藏约束：createdTime/receiveTime/lastUpdatedTime/asnNumbers/refNumbers 至少一组非空，
        // putawayCompletedTime 不算；故本项目按「收货时间」窗口增量拉取。
        String beginTime = resolveBeginTime();
        String endTime = resolveEndTime();

        // GLINK_BATCH_QUERY_ASN_NOTIFY 的 warehouseCode 为必填，按服务商已配置的仓库逐仓分页拉取
        List<String> warehouseCodes = resolveWarehouseCodes(auth.getAuthId());
        if (CollUtil.isEmpty(warehouseCodes)) {
            log.warn("[爱亚入库] 服务商[id={}] 未配置可用仓库，跳过批量查询入库单", auth.getAuthId());
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
                    log.error("[爱亚入库] 服务商[id={}] 仓库[{}] 收货时间[{} ~ {}] 调用异常, page={}",
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
                        if (asnInfo == null || asnInfo.getAsnNumber() == null) {
                            continue;
                        }
                        result.add(normalize(asnInfo));
                    }
                }
                // 无分页元数据，返回条数不足一页即视为该仓末页
                if (pageCount < DEFAULT_PAGE_SIZE) {
                    break;
                }
                page++;
            }

            if (page > MAX_PAGE_LIMIT) {
                log.error("[爱亚入库] 服务商[id={}] 仓库[{}] 已达最大翻页上限({})，存在未拉取数据，任务中止",
                        auth.getAuthId(), warehouseCode, MAX_PAGE_LIMIT);
                throw new ServiceException(ApiError.WH_AIYA_PAGE_LIMIT_EXCEEDED, ACTION, MAX_PAGE_LIMIT, result.size());
            }
        }

        if (result.isEmpty()) {
            log.info("[爱亚入库] 服务商[id={}] 仓库{} 收货时间[{} ~ {}] 未拉到任何入库单",
                    auth.getAuthId(), warehouseCodes, beginTime, endTime);
            return Collections.emptyList();
        }
        log.info("[爱亚入库] 服务商[id={}] 仓库{} 收货时间[{} ~ {}] 共拉取入库单={}条",
                auth.getAuthId(), warehouseCodes, beginTime, endTime, result.size());
        return Collections.singletonList(buildInitDTO(result, auth.getAuthId()));
    }

    /**
     * 组装单页入库单批量查询请求：必填 customerCode/page/pageSize/warehouseCode + 「收货时间」窗口
     * （receiveTime，满足爱亚「至少一组时间/单号非空」约束）。
     */
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
        req.setAsnType(AsnTypeEnum.SUPPLIER_RECEIPT.getCode());
        return req;
    }

    /**
     * 按服务商（授权）ID 读取其已配置且未停用的仓库编码列表（{@code platformWarehouseCode}）。
     */
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
     * 归一化爱亚 ASN 结果为下游 DMP 映射直接可用的字段名。
     * <p>
     * 签收明细取自 {@code asnItemReceiveDetails}：该数组同时包含收货流水（detailId 前缀 {@code RV}）
     * 与上架流水（detailId 前缀 {@value #PUTAWAY_DETAIL_ID_PREFIX}），二者数量重复，签收以「上架流水（PV）」为准。
     * 因此仅保留 PV 流水，并把 ASN 级 receiveTime、asnNumber 回填到每行（PV 流水本身无收货时间），
     * 拍平到 detail_list_json 后下游仍能取到收货时间、生成唯一签收流水 ID。
     */
    private JSONObject normalize(AiyaInboundResp.AsnInfoDTO asnInfo) {
        List<AiyaInboundResp.AsnItemReceiveDetailDTO> putawayDetails = new ArrayList<>();
        List<AiyaInboundResp.AsnItemReceiveDetailDTO> receiveDetails = asnInfo.getAsnItemReceiveDetails();
        if (CollUtil.isNotEmpty(receiveDetails)) {
            for (AiyaInboundResp.AsnItemReceiveDetailDTO detail : receiveDetails) {
                if (detail == null || detail.getDetailId() == null
                        || !detail.getDetailId().startsWith(PUTAWAY_DETAIL_ID_PREFIX)) {
                    continue;
                }
                detail.setAsnNumber(asnInfo.getAsnNumber());
                // PV 上架流水无收货时间，回填 ASN 级收货时间用于下游生成签收流水时间
                if (CharSequenceUtil.isBlank(detail.getReceiveTime())) {
                    detail.setReceiveTime(asnInfo.getReceiveTime());
                }
                putawayDetails.add(detail);
            }
        }
        JSONObject obj = new JSONObject();
        obj.put("receivingCode", asnInfo.getAsnNumber());
        obj.put("sourceCode", asnInfo.getAsnNumber());
        obj.put("receivingStatus", asnInfo.getStatus());
        obj.put("warehouseCode", asnInfo.getWarehouseCode());
        obj.put("receiveTime", asnInfo.getReceiveTime());
        obj.put("remark", asnInfo.getWarehouseNotes());
        obj.put("asnItems", putawayDetails);
        return obj;
    }

    /**
     * 收货时间起：dmp 任务有 startTime 则取其「当天 00:00:00」，否则回退「今天 - 1 天」的 00:00:00。
     * <p>
     * 与 {@code WegoInboundInitHandler} 对齐：dmp 任务的 startTime/endTime 是「业务时间窗口」，NORMAL 任务
     * 常为很窄的增量窗口（甚至只有数十秒，见基类拆分逻辑），若按秒级窗口过滤 {@code receiveTime} 几乎命中不到
     * 任何实际收货数据，故统一放宽到「整天」边界（爱亚按 asnNumber 幂等 upsert，重复拉取安全）。
     */
    private String resolveBeginTime() {
        LocalDateTime startTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getStartTime();
        LocalDate beginDate = startTime != null ? startTime.toLocalDate() : LocalDate.now().minusDays(1);
        return beginDate.atStartOfDay().format(DATETIME_FORMATTER);
    }

    /**
     * 收货时间止：dmp 任务有 endTime 则取其「当天 23:59:59」，否则回退「今天」的 23:59:59。
     */
    private String resolveEndTime() {
        LocalDateTime endTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getEndTime();
        LocalDate endDate = endTime != null ? endTime.toLocalDate() : LocalDate.now();
        return endDate.atTime(LocalTime.MAX).format(DATETIME_FORMATTER);
    }
}
