package com.erp.server.dmp.inout.handler.input.task.init.api.aiya;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.sdk.wms.aiya.dto.response.AiyaInboundResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * 爱亚（AIYA/百世 GLINK）海外仓入库单验货明细回传 InitHandler，对齐 {@code WegoInboundInitHandler}。
 * <p>
 * 调用爱亚 {@code GLINK_QUERY_ASN_INSPECT_DETAIL_NOTIFY}，按「上架完成时间」范围（
 * {@code putawayCompletedTimeFrom}/{@code putawayCompletedTimeTo}）分页拉取入库单验货明细：
 * <ol>
 *   <li>按 {@link #dmpInputTaskEntity} 的 {@code nextLevelId} 定位已授权爱亚服务商（复用父类 {@link #resolveAuth()}）；</li>
 *   <li>时间窗口优先取 dmp 任务 startTime/endTime，缺省回退 [今天-1天 ~ 今天]（与 wego 一致）；</li>
 *   <li>分页拉取（无 total/pages 元数据，按「返回条数 &lt; pageSize」判断末页），循环翻页至末页；</li>
 *   <li>归一化字段（receivingCode/sourceCode/receivingStatus/asnItems）并把 ASN 级 receiveTime、asnNumber
 *       回填到每行验货明细，交由下游 mongo/{@code AiyaInBoundDmpHandler} 落 {@code dmp_third_inbound}。</li>
 * </ol>
 * <p>
 * {@code asnItems.skuStatus} 区分良品（GOOD）/不良品（DAMAGE），下游据此把良品/不良品分别写入
 * 签收记录、直接调拨单与即时库存。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaInboundInitHandler extends AbstractAiyaInitHandler {

    private static final String ACTION = "查询入库单验货明细";

    /**
     * 单页拉取条数（asnNumbers/asnInfo 数量上限，文档约束单次查询数量不超过 100）。
     */
    private static final int DEFAULT_PAGE_SIZE = 100;

    /**
     * 爱亚上架完成时间格式。
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        AiyaAuth auth = resolveAuth();

        String putawayFrom = resolveBeginTime();
        String putawayTo = resolveEndTime();

        JSONArray result = new JSONArray();
        int page = 1;
        while (page <= MAX_PAGE_LIMIT) {
            AiyaInboundResp resp;
            try {
                resp = aiyaOpenApiService.queryAsnInspectDetail(auth.getPartnerId(), auth.getPartnerKey(),
                        auth.getCustomerCode(), page, DEFAULT_PAGE_SIZE, putawayFrom, putawayTo);
            } catch (Exception e) {
                log.error("[爱亚入库] 服务商[id={}] 上架完成时间[{} ~ {}] 调用异常, page={}",
                        auth.getAuthId(), putawayFrom, putawayTo, page, e);
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
            // 无分页元数据，返回条数不足一页即视为末页
            if (pageCount < DEFAULT_PAGE_SIZE) {
                break;
            }
            page++;
        }

        if (page > MAX_PAGE_LIMIT) {
            log.error("[爱亚入库] 服务商[id={}] 已达最大翻页上限({})，存在未拉取数据，任务中止",
                    auth.getAuthId(), MAX_PAGE_LIMIT);
            throw new ServiceException(ApiError.WH_AIYA_PAGE_LIMIT_EXCEEDED, ACTION, MAX_PAGE_LIMIT, result.size());
        }
        if (result.isEmpty()) {
            log.info("[爱亚入库] 服务商[id={}] 上架完成时间[{} ~ {}] 未拉到任何验货明细",
                    auth.getAuthId(), putawayFrom, putawayTo);
            return Collections.emptyList();
        }
        log.info("[爱亚入库] 服务商[id={}] 上架完成时间[{} ~ {}] 共拉取入库单={}条",
                auth.getAuthId(), putawayFrom, putawayTo, result.size());
        return Collections.singletonList(buildInitDTO(result, auth.getAuthId()));
    }

    /**
     * 归一化爱亚 ASN 验货结果为下游 DMP 映射直接可用的字段名，并把 ASN 级 receiveTime/asnNumber
     * 回填到每行验货明细（拍平到 detail_list_json 后仍可取到收货时间、生成唯一签收流水 ID）。
     */
    private JSONObject normalize(AiyaInboundResp.AsnInfoDTO asnInfo) {
        List<AiyaInboundResp.AsnItemDTO> asnItems = asnInfo.getAsnItems();
        if (CollUtil.isNotEmpty(asnItems)) {
            for (AiyaInboundResp.AsnItemDTO item : asnItems) {
                if (item != null) {
                    item.setReceiveTime(asnInfo.getReceiveTime());
                    item.setAsnNumber(asnInfo.getAsnNumber());
                }
            }
        }
        JSONObject obj = new JSONObject();
        obj.put("receivingCode", asnInfo.getAsnNumber());
        obj.put("sourceCode", asnInfo.getAsnNumber());
        obj.put("receivingStatus", asnInfo.getStatus());
        obj.put("warehouseCode", asnInfo.getWarehouseCode());
        obj.put("receiveTime", asnInfo.getReceiveTime());
        obj.put("remark", asnInfo.getWarehouseNotes());
        obj.put("asnItems", asnItems);
        return obj;
    }

    /**
     * 上架完成时间起：dmp 任务有 startTime 则取之，否则回退「今天 - 1 天」。
     */
    private String resolveBeginTime() {
        LocalDateTime startTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getStartTime();
        LocalDateTime begin = startTime != null ? startTime : LocalDateTime.now().minusDays(1);
        return begin.format(DATETIME_FORMATTER);
    }

    /**
     * 上架完成时间止：dmp 任务有 endTime 则取之，否则回退「当前时间」。
     */
    private String resolveEndTime() {
        LocalDateTime endTime = dmpInputTaskEntity == null ? null : dmpInputTaskEntity.getEndTime();
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return end.format(DATETIME_FORMATTER);
    }
}
