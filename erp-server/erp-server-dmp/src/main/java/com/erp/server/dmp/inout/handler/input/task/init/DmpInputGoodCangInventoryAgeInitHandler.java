package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.dto.response.GoodCangTotalPageResponse;
import com.sdk.wms.goodcang.utils.GoodCangUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputGoodCangInventoryAgeInitHandler extends DmpInputInitHandler {

    @Resource
    private DmpHandlerCache dmpHandlerCache;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();

        List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(DmpBasicSystemCodeEnum.GOODCANG.getCode()));
        if (CollUtil.isEmpty(overseasProviderEntityList)) {
            throw new ServiceException("谷仓授权信息不存在");
        }
        // 取对应授权ID授权
        OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);
        if (null == overseasProviderEntity) {
            throw new ServiceException("谷仓对应授权ID信息不存在");
        }
        ThirdWarehouseContext.setAuthMap(overseasProviderEntity.getAuthJson());

        // 请求所有
        List<JSONObject> allResult = requestAllAgeList(apiType);

        String id = overseasProviderEntity.getId();
        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        allResult.forEach(p -> p.put("authId", id));
        dmpInputTaskInitDTO.setMsg(JSON.toJSONString(allResult));
        return Collections.singletonList(dmpInputTaskInitDTO);
    }

    /**
     * 请求所有信息
     */
    private List<JSONObject> requestAllAgeList(String apiType) {
        Map<String, Object> requestMap = new HashMap<>();
        int page = 1;
        int pageSize = 200;
        requestMap.put("page_size", pageSize);
        requestMap.put("page", page);
        String response = GoodCangUtils.sendPost(apiType, JSON.toJSONString(requestMap));
        GoodCangResponse<GoodCangTotalPageResponse> result = JSONObject.parseObject(response, new TypeReference<GoodCangResponse<GoodCangTotalPageResponse>>() {}.getType());
        if (!GoodCangUtils.SUCCESS.equalsIgnoreCase(result.getMessage())) {
            ServiceException.runError(response);
        }
        if (null == result.getData()) {
            log.warn("谷仓库龄获取到结果为空：{}", response);
            return Collections.emptyList();
        }
        List<JSONObject> allResult = new ArrayList<>(result.getData().getList());
        int size = result.getData().getList().size();
        if (0 == size || 0 == result.getData().getTotal()) {
            return Collections.emptyList();
        }
        int maxPageSize = result.getData().getTotal() / pageSize;
        // 从第二页开始
        for (int pageIndex = 1; pageIndex < maxPageSize + 1; pageIndex++) {
            // 当前页数
            int currentPage = pageIndex + 1;
            requestMap.put("page", currentPage);
            GoodCangResponse<GoodCangTotalPageResponse> curResult = requestAndGetGoodCangResponse(apiType, requestMap, result, response);
            if (null == curResult.getData()) {
                log.warn("谷仓库龄获取到结果为空：{}", response);
                break;
            }
            allResult.addAll(curResult.getData().getList());
            int curSize = curResult.getData().getList().size();
            if (pageSize != curSize) {
                break;
            }
        }
        return allResult;
    }

    /**
     * 单次请求
     */
    private static GoodCangResponse<GoodCangTotalPageResponse> requestAndGetGoodCangResponse(String apiType, Map<String, Object> requestMap, GoodCangResponse<GoodCangTotalPageResponse> result, String response) {
        // 请求
        String curResponse = GoodCangUtils.sendPost(apiType, JSON.toJSONString(requestMap));
        GoodCangResponse<GoodCangTotalPageResponse> curResult = JSONObject.parseObject(curResponse, new TypeReference<GoodCangResponse<GoodCangTotalPageResponse>>() {}.getType());
        if (!GoodCangUtils.SUCCESS.equalsIgnoreCase(result.getMessage())) {
            ServiceException.runError(response);
        }
        return curResult;
    }
}
