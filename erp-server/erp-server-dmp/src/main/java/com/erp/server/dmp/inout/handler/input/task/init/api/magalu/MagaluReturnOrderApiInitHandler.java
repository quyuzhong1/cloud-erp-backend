package com.erp.server.dmp.inout.handler.input.task.init.api.magalu;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.sdk.oms.magalu.dto.MagaluShopInfoDTO;
import com.sdk.oms.magalu.service.MagaluService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import cn.hutool.core.text.CharSequenceUtil;
import javax.annotation.Resource;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@Scope("prototype")
public class MagaluReturnOrderApiInitHandler implements DmpInputApiInitHandler {

    private static final int DEFAULT_PAGE_SIZE = 50;

    @Resource
    private MagaluService magaluService;

    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
        String shopId = dmpInputApiInitRequest.getNextLevelId();
        MagaluShopInfoDTO shopInfoDTO = magaluService.getShopInfoByShopId(shopId);
        if (shopInfoDTO == null) {
            log.error("[Magalu售后订单下载]获取店铺授权失败: shopId={}", shopId);
            return Collections.emptyList();
        }

        List<DmpInputTaskInitDTO> resultList = new ArrayList<>();
        String apiPath = dmpInputApiInitRequest.getApiType();
        int pageSize = getPageSize(dmpInputApiInitRequest);
        int offset = 0;
        String startTime = formatUtc(dmpInputApiInitRequest.getStartTime());
        String endTime = formatUtc(dmpInputApiInitRequest.getEndTime());
        while (true) {
            List<JSONObject> pageList = magaluService.listTicketPage(shopInfoDTO, apiPath, offset, pageSize, startTime, endTime);
            if (CollectionUtils.isEmpty(pageList)) {
                break;
            }
            for (JSONObject ticket : pageList) {
                String ticketId = firstNotBlank(ticket.getString("id"), ticket.getString("code"));
                if (CharSequenceUtil.isBlank(ticketId)) {
                    continue;
                }
                JSONObject activitiesResp = magaluService.getTicketActivities(shopInfoDTO, ticketId);
                List<JSONObject> relevantActivities = extractRelevantActivities(activitiesResp);
                for (JSONObject activity : relevantActivities) {
                    String actType = activity == null ? "" : activity.getString("type");
                    if ("refunded".equalsIgnoreCase(actType)) {
                        if (!isTicketClosed(ticket)) {
                            // 按需求：refunded 必须 ticket.closed == true 才生成退款单
                            continue;
                        }
                    }
                    JSONObject enriched = new JSONObject();
                    enriched.put("ticket", ticket);
                    enriched.put("activity", activity);
                    // emit as singleton array to match downstream list processing
                    resultList.add(DmpInputTaskInitDTO.initMsg(JSONArray.toJSONString(Collections.singletonList(enriched))));
                }
            }
            if (pageList.size() < pageSize) {
                break;
            }
            offset += pageSize;
        }
        return resultList;
    }

    private String formatUtc(java.time.LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.atOffset(ZoneOffset.ofHours(8))
                .withOffsetSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private int getPageSize(DmpInputApiInitRequest dmpInputApiInitRequest) {
        String requestParam = dmpInputApiInitRequest.getRequestParam();
        if (requestParam == null || requestParam.trim().isEmpty()) {
            return DEFAULT_PAGE_SIZE;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestParam);
            Integer pageSize = jsonObject.getInteger("pageSize");
            if (pageSize == null) {
                pageSize = jsonObject.getInteger("limit");
            }
            return pageSize == null || pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        } catch (Exception e) {
            log.warn("[Magalu售后订单下载]requestParam解析失败，使用默认分页大小: {}", requestParam);
            return DEFAULT_PAGE_SIZE;
        }
    }

    private List<JSONObject> extractRelevantActivities(JSONObject response) {
        List<JSONObject> result = new ArrayList<>();
        if (response == null || response.isEmpty()) {
            return result;
        }
        JSONArray arr = findActivitiesArray(response);
        if (arr == null) {
            return result;
        }
        for (Object o : arr) {
            if (o instanceof JSONObject) {
                JSONObject act = (JSONObject) o;
                String t = act.getString("type");
                if ("return_info_sent".equalsIgnoreCase(t) || "refunded".equalsIgnoreCase(t)) {
                    result.add(act);
                }
            }
        }
        return result;
    }

    private JSONArray findActivitiesArray(Object parsed) {
        if (parsed instanceof JSONArray) {
            return (JSONArray) parsed;
        }
        if (!(parsed instanceof JSONObject)) {
            return null;
        }
        JSONObject jo = (JSONObject) parsed;
        for (String key : new String[]{"activities", "data", "items", "results", "events"}) {
            Object v = jo.get(key);
            if (v instanceof JSONArray) {
                return (JSONArray) v;
            }
            if (v instanceof JSONObject) {
                JSONArray nested = findActivitiesArray(v);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private String firstNotBlank(String... vals) {
        if (vals == null) return "";
        for (String v : vals) {
            if (CharSequenceUtil.isNotBlank(v)) {
                return v;
            }
        }
        return "";
    }

    /**
     * refunded 类型活动必须 ticket.closed == true 才生成退款单
     */
    private boolean isTicketClosed(JSONObject ticket) {
        if (ticket == null) return false;
        Object closed = ticket.get("closed");
        if (closed == null) return false;
        String s = String.valueOf(closed).trim().toLowerCase();
        return "true".equals(s) || "1".equals(s) || Boolean.TRUE.equals(closed);
    }
}
