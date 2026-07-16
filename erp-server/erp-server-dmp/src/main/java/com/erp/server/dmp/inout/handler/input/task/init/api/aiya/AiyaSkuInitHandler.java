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
 * TODO：文档未提供 {@code total}/{@code pages}/{@code emptyFlag} 等分页终止字段，暂以
 * "本页返回条数 &lt; pageSize" 判断已到最后一页，需联调真实接口后确认。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaSkuInitHandler extends AbstractAiyaInitHandler {

    private static final String ACTION = "SKU";
    private static final int DEFAULT_PAGE_SIZE = 200;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        AiyaAuth auth = resolveAuth();

        List<Object> allSkuList = new ArrayList<>();
        int pageNum = 1;

        while (pageNum <= MAX_PAGE_LIMIT) {
            AiyaSkuQueryDTO.QueryReqDTO reqDTO = new AiyaSkuQueryDTO.QueryReqDTO();
            reqDTO.setAccessToken(auth.getPartnerId());
            reqDTO.setSecret(auth.getPartnerKey());
            reqDTO.setCustomerCode(auth.getCustomerCode());
            reqDTO.setPageNum(pageNum);
            reqDTO.setPageSize(DEFAULT_PAGE_SIZE);

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

            if (itemList.isEmpty() || itemList.size() < DEFAULT_PAGE_SIZE) {
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
