package com.erp.server.dmp.inout.handler.input.task.init.api.aiya;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.AiyaSkuQueryDTO;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.sdk.wms.aiya.enums.AiyaSkuStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 爱亚海外仓 SKU（商品）拉取 InitHandler，对齐 {@code WegoSkuInitHandler}。
 * <p>
 * 分页调用爱亚 {@code GLINK_QUERY_ITEM_NOTIFY}（商品注册/查询）接口，将 SKU 明细写入
 * DMP Init 阶段，后续由
 * {@code com.erp.server.dmp.inout.handler.input.task.dmp.aiya.AiyaSkuOmsSyncDmpHandler} 消费，
 * 推送至 OMS 未匹配 SKU 对照表。
 * <p>
 * 注意：该接口响应结构为 {@code {code, message, success, itemList:[...]}}，与仓库/库存等接口的
 * {@code {success, result:...}} 结构不同，不能复用 {@link AbstractAiyaInitHandler#extractPageResult}，
 * 本类单独解析 {@code itemList}。
 * <p>
 * 2026-07-16 联调发现文档未列出的真实必填约束（爱亚网关底层 QERP Open API Platform
 * {@code GLINK_QUERY_ITEM_NOTIFY} 规范）——{@code skus}、createdTime 范围、updatedTime 范围三者
 * 至少要有一组非空，否则报 {@code INVALID_DATA: Created time and Updated time and SKUs cannot be
 * both empty}。业务目标是每次全量拉取所有 SKU（详见文档 6.2.2 排重逻辑 a-e，需要每次拿到 SKU 全量
 * 快照才能判断爱亚侧已删除/停用的 SKU），故固定传 {@code createdTimeFrom}={@link #DEV_START_TIME}
 * （本次爱亚对接开发起始日期）~ {@code createdTimeTo}=当前时间，覆盖迄今为止创建的所有 SKU。
 * 已与产品/业务确认（2026-07-16）：该客户账号/SKU 不存在早于 {@link #DEV_START_TIME} 的创建记录，
 * 该固定锚点不会漏拉历史 SKU，详见 docs/integrations/aiya-overseas-warehouse/README.md「已确认结论」。
 * <p>
 * 2026-07-16 联调实测确认：响应顶层实际带有 {@code total} 字段（文档未列出），本类据此在
 * "本页返回条数 &lt; pageSize" 之外，新增按累计拉取条数对比 {@code total} 的终止判断，两者任一满足即停止翻页，
 * 优先信任 {@code total}（更准确），size 判断作为兜底（防止 {@code total} 缺失/不可靠时死循环翻页）。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaSkuInitHandler extends AbstractAiyaInitHandler {

    private static final String ACTION = "SKU";
    private static final int DEFAULT_PAGE_SIZE = 200;

    /**
     * 爱亚 createdTime 范围请求字段的时间格式。
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 本次爱亚对接开发起始日期（首个爱亚相关提交日期），作为 createdTimeFrom 固定锚点，
     * 用于全量拉取「迄今为止创建的所有 SKU」。已与产品/业务确认（2026-07-16）该账号/SKU
     * 不存在早于此锚点的创建记录，不会漏拉，详见类注释。
     */
    private static final LocalDateTime DEV_START_TIME = LocalDateTime.of(2026, 7, 14, 0, 0, 0);

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        AiyaAuth auth = resolveAuth();

        // 全量拉取窗口：固定锚点 ~ 当前时间，在本次任务运行期间保持稳定（不逐页重新取"现在"）
        String createdTimeFrom = DEV_START_TIME.format(DATETIME_FORMATTER);
        String createdTimeTo = LocalDateTime.now().format(DATETIME_FORMATTER);

        List<Object> allSkuList = new ArrayList<>();
        int pageNum = 1;
        // 2026-07-16 联调实测响应带 total 字段：记录首页拿到的声明总数 + 累计已拉取的原始条目数（过滤前），
        // 用于比对 total 判断是否已拉完；total 缺失时 declaredTotal 保持 -1，退化为纯 size 判断
        long declaredTotal = -1L;
        long fetchedRawCount = 0L;

        while (pageNum <= MAX_PAGE_LIMIT) {
            AiyaSkuQueryDTO.QueryReqDTO reqDTO = new AiyaSkuQueryDTO.QueryReqDTO();
            reqDTO.setAccessToken(auth.getPartnerId());
            reqDTO.setSecret(auth.getPartnerKey());
            reqDTO.setCustomerCode(auth.getCustomerCode());
            reqDTO.setPageNum(pageNum);
            reqDTO.setPageSize(DEFAULT_PAGE_SIZE);
            reqDTO.setCreatedTimeFrom(createdTimeFrom);
            reqDTO.setCreatedTimeTo(createdTimeTo);

            JSONObject response;
            try {
                response = aiyaOpenApiService.querySku(reqDTO);
            } catch (Exception e) {
                log.error("[爱亚SKU] 服务商[id={}] 调用异常，pageNum={}", auth.getAuthId(), pageNum, e);
                throw new ServiceException(e, ApiError.WH_AIYA_PAGE_QUERY_ERROR, ACTION, pageNum);
            }

            JSONArray itemList = extractItemList(response, ACTION);
            if (itemList == null) {
                log.error("[爱亚SKU] 服务商[id={}] 第{}页响应解析失败", auth.getAuthId(), pageNum);
                throw new ServiceException(ApiError.WH_AIYA_PAGE_PARSE_FAILED, ACTION, pageNum);
            }
            if (declaredTotal < 0 && response.containsKey("total")) {
                declaredTotal = response.getLongValue("total");
            }
            fetchedRawCount += itemList.size();

            for (int i = 0; i < itemList.size(); i++) {
                JSONObject item = itemList.getJSONObject(i);
                if (item == null) {
                    continue;
                }
                if (!AiyaSkuStatusEnum.needSync(item.getString("status"))) {
                    continue;
                }
                allSkuList.add(item);
            }

            boolean lastPageBySize = itemList.isEmpty() || itemList.size() < DEFAULT_PAGE_SIZE;
            boolean lastPageByTotal = declaredTotal >= 0 && fetchedRawCount >= declaredTotal;
            if (lastPageBySize || lastPageByTotal) {
                break;
            }
            pageNum++;
        }

        if (pageNum > MAX_PAGE_LIMIT) {
            log.error("[爱亚SKU] 服务商[id={}] 已达最大翻页上限({})，存在未拉取数据，任务中止",
                    auth.getAuthId(), MAX_PAGE_LIMIT);
            throw new ServiceException(ApiError.WH_AIYA_PAGE_LIMIT_EXCEEDED, ACTION, MAX_PAGE_LIMIT, allSkuList.size());
        }
        log.info("[爱亚SKU] 服务商[id={}] 共拉取SKU={}条，页数={}", auth.getAuthId(), allSkuList.size(), pageNum);

        if (allSkuList.isEmpty()) {
            return Collections.emptyList();
        }

        JSONArray result = JSON.parseArray(JSONObject.toJSONString(allSkuList));
        return Collections.singletonList(buildInitDTO(result, auth.getAuthId()));
    }

    /**
     * 解析爱亚 SKU 查询响应，提取 {@code itemList} 数组。
     * <p>
     * 接口结构：{@code {code, message, success, itemList:[...]}}，success=true 为成功；
     * success=false 视为真实失败直接抛出 {@link ServiceException}，与
     * {@link AbstractAiyaInitHandler#extractPageResult} 保持一致的"不静默降级"原则。
     *
     * @return success=true 时返回 itemList（可能为空数组）；response 为 null 或结构异常时返回 null，由调用方判定为解析失败
     */
    private JSONArray extractItemList(JSONObject response, String actionName) {
        if (response == null) {
            return null;
        }
        if (!Boolean.TRUE.equals(response.getBoolean("success"))) {
            throw new ServiceException(ApiError.WH_AIYA_RESPONSE_FAILED, actionName,
                    String.valueOf(response.get("code")), String.valueOf(response.get("message")));
        }
        JSONArray itemList = response.getJSONArray("itemList");
        return itemList == null ? new JSONArray() : itemList;
    }
}
