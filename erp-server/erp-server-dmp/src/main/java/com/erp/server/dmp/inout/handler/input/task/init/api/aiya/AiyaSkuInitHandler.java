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
 * {@code com.erp.server.dmp.inout.handler.input.task.dmp.aiya.AiyaSkuInfoDmpHandler} 落入
 * {@code dmp_product_info}/{@code dmp_sku_info}，再经 Product Output MQ 推送到 OMS listing 消费端。
 * <p>
 * 注意：该接口响应结构为 {@code {code, message, success, itemList:[...]}}，与仓库/库存等接口的
 * {@code {success, result:...}} 结构不同，不能复用 {@link AbstractAiyaInitHandler#extractPageResult}，
 * 本类单独解析 {@code itemList}。
 * <p>
 * 2026-07-16 联调发现文档未列出的真实必填约束（爱亚网关底层 QERP Open API Platform
 * {@code GLINK_QUERY_ITEM_NOTIFY} 规范）——{@code skus}、createdTime 范围、updatedTime 范围三者
 * 至少要有一组非空，否则报 {@code INVALID_DATA: Created time and Updated time and SKUs cannot be
 * both empty}。SKU 主数据对齐 WEGO/纬狮/极风/大卖等海外仓，每次按<strong>全量快照</strong>拉取，
 * 不使用 DMP 任务 {@code startTime}/{@code endTime} 做时间窗过滤（任务时间仅适用于入出库等单据）。
 * 因接口强制要时间，固定传 {@code createdTimeFrom}={@link #DEV_START_TIME} ~
 * {@code createdTimeTo}=当前时间。已与产品/业务确认（2026-07-16）：该客户账号/SKU 不存在早于
 * {@link #DEV_START_TIME} 的创建记录，该固定锚点不会漏拉历史 SKU。
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
     * 全量拉取的 createdTimeFrom 固定锚点（本次爱亚对接开发起始日期）。
     * 已与产品/业务确认（2026-07-16）该账号/SKU 不存在早于此锚点的创建记录。
     */
    private static final LocalDateTime DEV_START_TIME = LocalDateTime.of(2026, 7, 1, 0, 0, 0);

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        AiyaAuth auth = resolveAuth();

        // 全量快照窗口：固定锚点 ~ 当前时间（不读任务 startTime/endTime，对齐其它海外仓 SKU 拉取语义）
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
            int skippedByStatus = 0;
            for (int i = 0; i < itemList.size(); i++) {
                JSONObject item = itemList.getJSONObject(i);
                if (item == null) {
                    skippedByStatus++;
                    continue;
                }
                if (!AiyaSkuStatusEnum.needSync(item.getString("status"))) {
                    skippedByStatus++;
                    if (pageNum == 1 && skippedByStatus <= 3) {
                        log.warn("[爱亚SKU] 服务商[id={}] 跳过SKU: sku={}, status={}",
                                auth.getAuthId(), item.getString("sku"), item.getString("status"));
                    }
                    continue;
                }
                allSkuList.add(item);
            }
            // 首屏必打：区分「接口本身返回空」与「有数据但被 status 过滤掉」
            if (pageNum == 1) {
                log.warn("[爱亚SKU] 服务商[id={}] 第1页响应: success={}, code={}, total={}, itemListSize={}, "
                                + "accepted={}, skippedByStatus={}, partnerId={}, customerCode={}, "
                                + "createdTimeFrom={}, createdTimeTo={}, responseKeys={}",
                        auth.getAuthId(),
                        response.get("success"),
                        response.get("code"),
                        response.get("total"),
                        itemList.size(),
                        itemList.size() - skippedByStatus,
                        skippedByStatus,
                        auth.getPartnerId(),
                        auth.getCustomerCode(),
                        createdTimeFrom,
                        createdTimeTo,
                        response.keySet());
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
        if (allSkuList.isEmpty()) {
            // 空结果也会把任务标成 finish（无 error_message），必须用 warn 留痕，否则会误判为「任务成功但 ERP 无数据」
            log.warn("[爱亚SKU] 服务商[id={}] 拉取结果为空，跳过后续FDS/Mongo/OMS。createdTimeFrom={}, createdTimeTo={}, declaredTotal={}, fetchedRawCount={}, pageNum={}",
                    auth.getAuthId(), createdTimeFrom, createdTimeTo, declaredTotal, fetchedRawCount, pageNum);
            return Collections.emptyList();
        }
        log.warn("[爱亚SKU] 服务商[id={}] 共拉取SKU={}条，页数={}, createdTimeFrom={}, createdTimeTo={}, declaredTotal={}",
                auth.getAuthId(), allSkuList.size(), pageNum, createdTimeFrom, createdTimeTo, declaredTotal);

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
