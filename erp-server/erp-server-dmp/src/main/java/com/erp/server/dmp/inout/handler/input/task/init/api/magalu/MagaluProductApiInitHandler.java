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

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@Scope("prototype")
public class MagaluProductApiInitHandler implements DmpInputApiInitHandler {

    private static final int DEFAULT_PAGE_SIZE = 100;

    @Resource
    private MagaluService magaluService;

    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
        String shopId = dmpInputApiInitRequest.getNextLevelId();
        MagaluShopInfoDTO shopInfoDTO = magaluService.getShopInfoByShopId(shopId);
        if (shopInfoDTO == null) {
            log.error("[Magalu产品下载]获取店铺授权失败: shopId={}", shopId);
            return Collections.emptyList();
        }

        List<DmpInputTaskInitDTO> resultList = new ArrayList<>();
        String apiPath = dmpInputApiInitRequest.getApiType();
        int pageSize = getPageSize(dmpInputApiInitRequest);
        int offset = 0;
        while (true) {
            List<JSONObject> pageList = magaluService.listSkuPage(shopInfoDTO, apiPath, offset, pageSize);
            if (CollectionUtils.isEmpty(pageList)) {
                break;
            }
            resultList.add(DmpInputTaskInitDTO.initMsg(JSONArray.toJSONString(pageList)));
            if (pageList.size() < pageSize) {
                break;
            }
            offset += pageSize;
        }
        return resultList;
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
            log.warn("[Magalu产品下载]requestParam解析失败，使用默认分页大小: {}", requestParam);
            return DEFAULT_PAGE_SIZE;
        }
    }
}
