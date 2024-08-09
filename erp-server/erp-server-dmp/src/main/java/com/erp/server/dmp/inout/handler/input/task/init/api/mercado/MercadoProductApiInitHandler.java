package com.erp.server.dmp.inout.handler.input.task.init.api.mercado;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.mercado.constant.MercadoConstant;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercado.dto.mercado.listing.ListingDTO;
import com.sdk.oms.mercado.dto.mercado.listing.ListingViewDTO;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import com.sdk.oms.shopify.api.rest.model.ShopifyProduct;
import com.sdk.oms.shopify.api.rest.model.ShopifyProducts;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
@Scope("prototype")
public class MercadoProductApiInitHandler implements DmpInputApiInitHandler {

    @Resource
    private MercadoSdkClientService mercadoSdkClientService;

    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        String nextLevelId = dmpInputApiInitRequest.getNextLevelId();

        //  根据店铺ID获取授权
        MercadoShopInfoDTO shopInfoDTO = mercadoSdkClientService.getShopInfoByShopId(nextLevelId);
        if (null == shopInfoDTO) {
            log.error("[美客多产品下载]从缓存中获取美客多 token 失败: shopId={}", nextLevelId);
            return Collections.emptyList();
        }

        //每次最多获取200条
        Integer pageSize = 200;
        //当前页数
        Integer pageNo = 0;
        //总页数
        Integer pageCount = 1;

        //接口地址
        String url = MercadoConstant.URL;
        String path = dmpInputApiInitRequest.getApiType().replace("{userId}", shopInfoDTO.getUserId().toString());
        while(pageNo < pageCount) {

            //入参
            HashMap<String, Object> params = new HashMap<>(2);
            params.put("limit", pageSize);
            params.put("offset", pageNo);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            ObjectMapper objectMapper = new ObjectMapper();
            ListingDTO listingDTO = null;
            try {
                listingDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), ListingDTO.class);
            } catch (JsonProcessingException e) {
                log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            if (CollectionUtils.isEmpty(listingDTO.getResults())) {
                break;
            }
            pageCount = (listingDTO.getPaging().getTotal() + pageSize - 1) / pageSize;
            pageNo++;

            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(apiResult.getData().toString());
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
        }
        return dmpInputTaskInitDTOList;
    }
}
