package com.sdk.oms.tiktok.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.sdk.oms.tiktok.constant.TikTokConstant;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.packages.CombinePackageGroupsBean;
import com.sdk.oms.tiktok.dto.tiktok.packages.CombinePackagePramDTO;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderUS;
import com.sdk.oms.tiktok.dto.tiktok.split.CombinePackageViewDTO;
import com.sdk.oms.tiktok.util.EncryptionUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.*;

/**
 * 美客多平台SDK
 *
 * @Author Luo_WG
 * @Date 2024/2/27 18:04
 **/
@Slf4j
@Component
public class TikTokPackageService {
    private static RedisUtil redisUtil;


    @Resource
    private ShopInfoFeign shopInfoFeign;


    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    public void setRedisUtil(RedisUtil redisUtil) {
        TikTokPackageService.redisUtil = redisUtil;
    }


    public static void main(String[] args) {
        String url = TikTokConstant.URL;
//        String path = "/fulfillment/" + TikTokConstant.VERSION + "/packages/" + "1159856476975565342" ;
        String path = "/fulfillment/" + TikTokConstant.VERSION + "/packages/" + "1160432935985842924" +"/shipping_documents";
        String clientSecret = "8ff628de24faf70c24855de4d967fb6a17a47e3f";
        String clientId = "6buinkjt3hmld";

        String shopCipher = "ROW_7UdPPQAAAAAGSBiq11mBcg8dYgNF1C5x";
        String token = "ROW_TW8lnAAAAACj-JAAAriAWjVtF2MrUIFdHRZvljXhCfG7h6gK9L_d7XUiGvUrvVcL5dTSfrmArwcZToZCJ724bMWtyrfq1KcdT5ve4dG2uiO_z2pUxpVdbGBEDIIIO1gX70t45vVDtyOe2QHddMFcsE_CavkKUoYq6xSY7FmN_DntMDBoOzmFilyVE6CvmRiYtqYcYDPdr6k0qfjZ-9SN1XwXHNbyogHz";
        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", token);
        params.put("document_type", "SHIPPING_LABEL");
        params.put("app_key", clientId);
        params.put("shop_cipher", shopCipher);
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.VERSION);


        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("x-tts-access-token", token);
        headerMap.put("content-type", "application/json");

        //组装入参排序计算签名字符串
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, "");

        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);

        //加入sign签名入参
        params.put("sign", sign);

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok查询发货选项失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询发货选项失败，返回值 responseMap={}",
                    url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        ShipOrderUS shipOrderUS = null;
        try {
            shipOrderUS = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), ShipOrderUS.class);
        } catch (Exception e) {
            throw new RuntimeException(CharSequenceUtil.format("调用url={},入参params={}, TikTok订单发货（美国站）返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }
    }

    /**
     * 组包预报
     */
    public CombinePackageViewDTO combinePackage(String shopId, CombinePackagePramDTO combinePackagePramDTO) {
        TikTokShopInfoDTO tikTokShopInfoDTO = this.getShopInfoByShopId(shopId);

        String url = TikTokConstant.URL;
        String path = "/fulfillment/" + TikTokConstant.VERSION + "/packages/combine";
        String clientSecret = tikTokShopInfoDTO.getClientSecret();
        String clientId = tikTokShopInfoDTO.getClientId();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", tikTokShopInfoDTO.getAccessToken());
        params.put("app_key", clientId);
        params.put("shop_cipher", tikTokShopInfoDTO.getShopCipher());
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("x-tts-access-token", tikTokShopInfoDTO.getAccessToken());
        headerMap.put("content-type", "application/json");

        //请求body，平台用于计算签名
        Map<String, Object> bodyMap = new HashMap<>();
        List<Object> objectList = new ArrayList<>();
        for (CombinePackageGroupsBean combinePackageGroupsBeanList : combinePackagePramDTO.getCombinablePackages()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", combinePackageGroupsBeanList.getId());
            map.put("order_ids", combinePackageGroupsBeanList.getOrderIds());
            objectList.add(map);
        }
        bodyMap.put("combinable_packages", objectList);
        String body = JSONUtil.toJsonStr(bodyMap);

        //组装入参排序计算签名字符串
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, body);

        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);

        //加入sign签名入参
        params.put("sign", sign);

        //组装url
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        sb.append(path);
        sb.append("?access_token=" + tikTokShopInfoDTO.getAccessToken() + "");
        sb.append("&app_key=" + tikTokShopInfoDTO.getClientId() + "");
        sb.append("&shop_cipher=" + tikTokShopInfoDTO.getShopCipher() + "");
        sb.append("&sign=" + sign + "");
        sb.append("&timestamp=" + timestamp + "");
        sb.append("&version=" + TikTokConstant.VERSION + "");

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), body, null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok组包预报失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok订单组包预报，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        CombinePackageViewDTO result = null;
        try {
            result = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), CombinePackageViewDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok组包预报返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }
        return result;
    }

    /**
     * 取消组包预报
     */
    public CombinePackageViewDTO uncombinePackage(String shopId,String package_id, List<String> orderIds) {
        TikTokShopInfoDTO tikTokShopInfoDTO = this.getShopInfoByShopId(shopId);

        String url = TikTokConstant.URL;
        String path = "/fulfillment/" + TikTokConstant.VERSION + "/packages/"+package_id+"/uncombine";
        String clientSecret = tikTokShopInfoDTO.getClientSecret();
        String clientId = tikTokShopInfoDTO.getClientId();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", tikTokShopInfoDTO.getAccessToken());
        params.put("app_key", clientId);
        params.put("shop_cipher", tikTokShopInfoDTO.getShopCipher());
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("x-tts-access-token", tikTokShopInfoDTO.getAccessToken());
        headerMap.put("content-type", "application/json");

        //请求body，平台用于计算签名
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("order_ids", orderIds);
        String body = JSONUtil.toJsonStr(bodyMap);

        //组装入参排序计算签名字符串
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, body);

        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);

        //加入sign签名入参
        params.put("sign", sign);

        //组装url
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        sb.append(path);
        sb.append("?access_token=" + tikTokShopInfoDTO.getAccessToken() + "");
        sb.append("&app_key=" + tikTokShopInfoDTO.getClientId() + "");
        sb.append("&shop_cipher=" + tikTokShopInfoDTO.getShopCipher() + "");
        sb.append("&sign=" + sign + "");
        sb.append("&timestamp=" + timestamp + "");
        sb.append("&version=" + TikTokConstant.VERSION + "");

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), body, null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok取消组包预报失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok取消组包预报失败，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        CombinePackageViewDTO result = null;
        try {
            result = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), CombinePackageViewDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok取消组包预报返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }
        return result;
    }

    /**
     * 查询店铺信息
     *
     * @param shopId
     * @return
     */
    public TikTokShopInfoDTO getShopInfoByShopId(String shopId) {
        if (StringUtil.isBlank(shopId)) {
            log.error("===============>TikTok查询店铺授权信息失败，店铺id：{}", shopId);
            return null;
        }
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.TIK_TOK.getCode(), shopId);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj) {
            if (tokenObj instanceof TikTokShopInfoDTO) {
                return (TikTokShopInfoDTO) tokenObj;
            }
        } else {
            ShopAuthEntity shopAuthEntity = shopInfoFeign.getShopAuthByShopId(shopId);
            ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(shopId);
            CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
            AppClientEnum appClientEnum = AppClientEnum.TIKTOK_ACCESS_TOKEN;
            findDTO.setBusinessType(appClientEnum.getBusinessType());
            findDTO.setDictPlatform(appClientEnum.getPlatform());
            findDTO.setPlatformType(appClientEnum.getPlatformType());
            CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            if (Objects.isNull(cfgAppClient)) {
                return null;
            }
            TikTokShopInfoDTO result = new TikTokShopInfoDTO();
            result.setBaseUrl(cfgAppClient.getUrl());
            result.setClientId(cfgAppClient.getClientId());
            result.setClientSecret(cfgAppClient.getClientSecret());
            result.setId(shopId);
            Map<String, Object> extendData = shopInfoEntity.getExtendData();
            result.setShopCipher(extendData.get("shopCipher") + "");
            result.setSite(extendData.get("region") + "");
            result.setSellerType(extendData.get("sellerType") + "");
            if (Objects.nonNull(shopAuthEntity)) {
                result.setAccessToken(shopAuthEntity.getAccessToken());
                redisUtil.set(tokenKey, result, shopAuthEntity.getExpiresIn());
            }
            return result;
        }
        return null;

    }
}
