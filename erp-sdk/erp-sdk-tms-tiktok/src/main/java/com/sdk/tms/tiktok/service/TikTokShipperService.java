package com.sdk.tms.tiktok.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.utils.CollectionUtils;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.sdk.oms.tiktok.constant.TikTokConstant;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import com.sdk.tms.tiktok.channel.delivery.DeliveryOptionsBean;
import com.sdk.tms.tiktok.channel.delivery.DeliveryOptionsDTO;
import com.sdk.tms.tiktok.channel.provider.ShippingProviderDTO;
import com.sdk.tms.tiktok.channel.provider.ShippingProvidersBean;
import com.sdk.tms.tiktok.channel.warehouses.WarehousesBean;
import com.sdk.tms.tiktok.channel.warehouses.WarehousesDTO;
import com.sdk.tms.tiktok.util.EncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
public class TikTokShipperService {
    public static final String LOGISTICS = "/logistics/";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String APP_KEY = "app_key";
    public static final String SHOP_CIPHER = "shop_cipher";
    public static final String TIMESTAMP = "timestamp";
    public static final String VERSION = "version";
    public static final String X_TTS_ACCESS_TOKEN = "x-tts-access-token";
    public static final String CONTENT_TYPE = "content-type";
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    /**
     * 查询平台发货渠道
     * @param shopId 店铺信息
     */
    public List<ShippingProvidersBean> sendTikTokLogisticsChannel(String shopId) {
        TikTokShopInfoDTO tikTokShopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(shopId);

        String url = TikTokConstant.URL;
        String path = LOGISTICS + TikTokConstant.VERSION + "/warehouses";
        String clientSecret = tikTokShopInfoDTO.getClientSecret();
        String clientId = tikTokShopInfoDTO.getClientId();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put(ACCESS_TOKEN, tikTokShopInfoDTO.getAccessToken());
        params.put(APP_KEY, clientId);
        params.put(SHOP_CIPHER, tikTokShopInfoDTO.getShopCipher());
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put(TIMESTAMP, timestamp);
        params.put(VERSION, TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put(X_TTS_ACCESS_TOKEN, tikTokShopInfoDTO.getAccessToken());
        headerMap.put(CONTENT_TYPE, MULTIPART_FORM_DATA);

        //组装入参排序计算签名字符串
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, "");

        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);

        //加入sign签名入参
        params.put("sign", sign);

        //拉取数据
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok查询平台发货渠道失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new ServiceException(CharSequenceUtil.format("调用url={},入参params={}, TikTok订单拆分失败，返回值 responseMap={}",
                    url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        WarehousesDTO warehousesDTO = null;
        try {
            warehousesDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), WarehousesDTO.class);
        } catch (Exception e) {
            throw new ServiceException(CharSequenceUtil.format("调用url={},入参params={}, TikTok查询平台发货渠道返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }

        List<ShippingProvidersBean> providersBeanList = new ArrayList<>();
        for (WarehousesBean warehouse : warehousesDTO.getData().getWarehouses()) {
            //根据平台仓库id查询发货选项
            DeliveryOptionsDTO deliveryOptionsDTO = this.sendTikTokDeliveryOptions(tikTokShopInfoDTO, warehouse.getId());
            if (CollUtil.isEmpty(deliveryOptionsDTO.getData().getDeliveryOptions())) {
                continue;
            }
            for (DeliveryOptionsBean deliveryOption : deliveryOptionsDTO.getData().getDeliveryOptions()) {
                //根据发货选项查询物流渠道
                ShippingProviderDTO shippingProviderDTO = this.sendTikTokShippingProviders(tikTokShopInfoDTO, deliveryOption.getId());
                if (shippingProviderDTO.getCode() == 0) {
                    providersBeanList.addAll(shippingProviderDTO.getData().getShippingProviders());
                }
            }
        }
        return providersBeanList;
    }

    /**
     * 根据平台仓库id查询发货选项
     * @param tikTokShopInfoDTO 店铺信息
     * @param warehouseId 平台仓库id
     */
    public DeliveryOptionsDTO sendTikTokDeliveryOptions(TikTokShopInfoDTO tikTokShopInfoDTO, String warehouseId) {

        String url = TikTokConstant.URL;
        String path = LOGISTICS + TikTokConstant.VERSION + "/warehouses/" + warehouseId + "/delivery_options";
        String clientSecret = tikTokShopInfoDTO.getClientSecret();
        String clientId = tikTokShopInfoDTO.getClientId();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put(ACCESS_TOKEN, tikTokShopInfoDTO.getAccessToken());
        params.put(APP_KEY, clientId);
        params.put(SHOP_CIPHER, tikTokShopInfoDTO.getShopCipher());
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put(TIMESTAMP, timestamp);
        params.put(VERSION, TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put(X_TTS_ACCESS_TOKEN, tikTokShopInfoDTO.getAccessToken());
        headerMap.put(CONTENT_TYPE, MULTIPART_FORM_DATA);

        //组装入参排序计算签名字符串
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, "");

        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);

        //加入sign签名入参
        params.put("sign", sign);

        //拉取数据
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok查询发货选项失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new ServiceException(CharSequenceUtil.format("调用url={},入参params={}, TikTok查询发货选项失败，返回值 responseMap={}",
                    url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        DeliveryOptionsDTO deliveryOptionsDTO = null;
        try {
            deliveryOptionsDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), DeliveryOptionsDTO.class);
        } catch (Exception e) {
            throw new ServiceException(CharSequenceUtil.format("调用url={},入参params={}, TikTok查询发货选项返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }
        return deliveryOptionsDTO;
    }

    /**
     * 根据发货选项查询物流渠道
     * @param tikTokShopInfoDTO 店铺信息
     * @param deliveryOptionId 平台发货选项id
     */
    public ShippingProviderDTO sendTikTokShippingProviders(TikTokShopInfoDTO tikTokShopInfoDTO, String deliveryOptionId) {
        String url = TikTokConstant.URL;
        String path = LOGISTICS + TikTokConstant.VERSION + "/delivery_options/" + deliveryOptionId + "/shipping_providers";
        String clientSecret = tikTokShopInfoDTO.getClientSecret();
        String clientId = tikTokShopInfoDTO.getClientId();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put(ACCESS_TOKEN, tikTokShopInfoDTO.getAccessToken());
        params.put(APP_KEY, clientId);
        params.put(SHOP_CIPHER, tikTokShopInfoDTO.getShopCipher());
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put(TIMESTAMP, timestamp);
        params.put(VERSION, TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put(X_TTS_ACCESS_TOKEN, tikTokShopInfoDTO.getAccessToken());
        headerMap.put(CONTENT_TYPE, MULTIPART_FORM_DATA);

        //组装入参排序计算签名字符串
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, "");

        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);

        //加入sign签名入参
        params.put("sign", sign);

        //拉取数据
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok物流渠道失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new ServiceException(CharSequenceUtil.format("调用url={},入参params={}, TikTok物流渠道失败，返回值 responseMap={}",
                    url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        ShippingProviderDTO shippingProviderDTO = null;
        try {
            shippingProviderDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), ShippingProviderDTO.class);
        } catch (Exception e) {
            throw new ServiceException(CharSequenceUtil.format("调用url={},入参params={}, TikTok物流渠道返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }
        return shippingProviderDTO;
    }
}
