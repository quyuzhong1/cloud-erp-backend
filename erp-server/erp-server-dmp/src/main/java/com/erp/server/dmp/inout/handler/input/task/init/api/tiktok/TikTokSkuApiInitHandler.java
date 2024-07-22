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
import com.sdk.oms.tiktok.dto.tiktok.listing.view.DataBean;
import com.sdk.oms.tiktok.dto.tiktok.listing.view.ListingViewDTO;
import com.sdk.oms.tiktok.dto.tiktok.listing.view.SkusBean;
import com.sdk.oms.tiktok.dto.tiktok.order.view.OrderViewDTO;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import com.sdk.oms.tiktok.util.EncryptionUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
@Scope("prototype")
public class TikTokSkuApiInitHandler implements DmpInputApiInitHandler {
    @Resource
    private TikTokSdkClientService tikTokSdkClientService;


    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
        DmpInputTikTokApiInitRequest dmpInputTikTokApiInitRequest = (DmpInputTikTokApiInitRequest) dmpInputApiInitRequest;

        String nextLevelId = dmpInputTikTokApiInitRequest.getNextLevelId();
        List<String> orderIds = dmpInputTikTokApiInitRequest.getOrderIds();

        TikTokShopInfoDTO shopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(nextLevelId);
        if (ObjectUtil.isEmpty(shopInfoDTO)) {
            throw new ServiceException("TikTok店铺id：" + nextLevelId + "未找到对应的店铺信息");
        }


        List<List<String>> partition = Lists.partition(orderIds, 50);
        //平台接口地址
        String url = TikTokConstant.URL;

        //组装授权url
        String path = dmpInputTikTokApiInitRequest.getApiType().replace("{version}", TikTokConstant.VERSION);

        //服务密钥
        String secret = shopInfoDTO.getClientSecret();


        for (List<String> list : partition) {

            // 定义查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", shopInfoDTO.getAccessToken());
            params.put("app_key", shopInfoDTO.getClientId());
            params.put("ids", StringUtil.join(list, ","));
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
            params.put("shop_id", "");
            String timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);
            params.put("version", TikTokConstant.VERSION);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "multipart/form-data");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            String input = EncryptionUtils.urlParamsSort(params, path, headerMap, secret, "");
            // 追加请求路径获取签名
            String sign = EncryptionUtils.generateSHA256(input, secret);
            //加入sign签名入参
            params.put("sign", sign);

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url+path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, TikTok查询订单详情数据失败，返回值 responseMap={}", url+path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询订单详情数据失败，返回值 responseMap={}",
                        url+path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            ObjectMapper objectMapper = new ObjectMapper();
            OrderViewDTO orderViewDTO = null;
            try {
                orderViewDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), OrderViewDTO.class);

            } catch (JsonProcessingException e) {
                e.printStackTrace();
                log.error("调用url={},入参params={}, TikTok订单详情数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok订单详情数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            if (CollectionUtil.isEmpty(orderViewDTO.getData().getOrders())) {
                break;
            }
            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(orderViewDTO.getData().getOrders()));
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
        }
        return dmpInputTaskInitDTOList;
    }




    private List<ListingViewDTO> listItemView(List<String> productIds, TikTokShopInfoDTO shopInfoDTO) {

        List<ListingViewDTO> resultList = new ArrayList<>();
        String url = TikTokConstant.URL;
        for (String productId : productIds) {
            //组装授权url
            String path = "/product/" + TikTokConstant.VERSION + "/products/" + productId + "";
            // 定义查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", shopInfoDTO.getAccessToken());
            params.put("app_key", shopInfoDTO.getClientId());
            params.put("shop_cipher", shopInfoDTO.getShopCipher());
            params.put("shop_id", "");
            String timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);
            params.put("version", TikTokConstant.VERSION);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "multipart/form-data");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            String input = EncryptionUtils.urlParamsSort(params, path, headerMap, shopInfoDTO.getClientSecret(), "");
            // 追加请求路径获取签名
            String sign = EncryptionUtils.generateSHA256(input, shopInfoDTO.getClientSecret());
            //加入sign签名入参
            params.put("sign", sign);

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}",
                        url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            ObjectMapper objectMapper = new ObjectMapper();
            ListingViewDTO listingViewDTO = null;
            try {
                listingViewDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), ListingViewDTO.class);
            } catch (JsonProcessingException e) {
                log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            DataBean dataBean = listingViewDTO.getData();

            List<SkusBean> skusBeanList = dataBean.getSkus();

            for (SkusBean skus : skusBeanList) {

                DataBean beanCopy = new DataBean();
                BeanUtils.copyProperties(dataBean, beanCopy);

                beanCopy.setSkus(Collections.singletonList(skus));

                ListingViewDTO dto = new ListingViewDTO();
                dto.setCode(listingViewDTO.getCode());
                dto.setData(beanCopy);
                dto.setMessage(listingViewDTO.getMessage());
                dto.setRequestId(listingViewDTO.getRequestId());
                resultList.add(dto);
            }
        }

        return resultList;
    }
}
