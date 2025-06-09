package com.erp.server.dmp.inout.handler.input.task.init.api.tiktok;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTikTokApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.sdk.oms.tiktok.constant.TikTokConstant;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.listing.ListingDTO;
import com.sdk.oms.tiktok.dto.tiktok.listing.view.ListingViewDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.view.OrderViewDTO;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import com.sdk.oms.tiktok.util.EncryptionUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Scope("prototype")
public class TikTokProductApiInitHandler implements DmpInputApiInitHandler {
    @Resource
    private TikTokSdkClientService tikTokSdkClientService;


    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        String nextLevelId = dmpInputApiInitRequest.getNextLevelId();

        TikTokShopInfoDTO shopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(nextLevelId);
        if (ObjectUtil.isEmpty(shopInfoDTO)) {
            throw new ServiceException("TikTok店铺id：" + nextLevelId + "未找到对应的店铺信息");
        }

        //每次最多获取200条
        Integer pageSize = 100;
        //分页token
        String pageToken = "";
        //平台接口地址
        String url = TikTokConstant.URL;
        //服务密钥
        String secret = shopInfoDTO.getClientSecret();



        while (true) {
            StringBuffer sb = new StringBuffer();
            //组装授权url
            String path = dmpInputApiInitRequest.getApiType().replace("{version}", TikTokConstant.VERSION);

            // 定义查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", shopInfoDTO.getAccessToken());
            params.put("app_key", shopInfoDTO.getClientId());
            params.put("page_size", pageSize);
            params.put("page_token", pageToken);
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
            params.put("shop_id", "");
            params.put("sign", "");
            String timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);
            params.put("version", TikTokConstant.VERSION);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "application/json");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            //请求body，平台用于计算签名
            Map<String, String> bodyMap = new HashMap<>();

            String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, JSONUtil.toJsonStr(bodyMap));
            // 追加请求路径获取签名
            String sign = EncryptionUtils.generateSHA256(input, secret);
            //加入sign签名入参
            params.put("sign", sign);

            //组装url
            sb.append(url);
            sb.append(path);
            sb.append("?access_token=" + params.get("access_token") + "");
            sb.append("&app_key=" + params.get("app_key") + "");
            sb.append("&page_size=" + params.get("page_size") + "");
            sb.append("&page_token=" + params.get("page_token") + "");
            sb.append("&shop_cipher=" + params.get("shop_cipher") + "");
            sb.append("&shop_id=" + params.get("shop_id") + "");
            sb.append("&sign=" + params.get("sign") + "");
            sb.append("&timestamp=" + params.get("timestamp") + "");
            sb.append("&version=" + params.get("version") + "");

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}",
                        sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
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

            if (CollectionUtils.isEmpty(listingDTO.getData().getProducts())) {
                break;
            }
            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(listingDTO.getData().getProducts()));
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);

            pageToken = listingDTO.getData().getNextPageToken();
            if (StringUtil.isBlank(pageToken)) {
                break;
            }

        }

        return dmpInputTaskInitDTOList;

    }
}
