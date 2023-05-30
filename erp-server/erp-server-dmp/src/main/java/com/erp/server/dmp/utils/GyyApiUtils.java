package com.erp.server.dmp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.Md5Util;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.UrlContant;
import com.erp.model.dmp.gyy.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管易API 处理类
 */
@Slf4j
@Component
public class GyyApiUtils {
    private static Integer APP_KEY = 135174;

    private static String SECRET_KEY = "7e10a52a116149d38a760c9bf4dd3cbc";

    private static String SESSION_KEY ="7daa147a1cea4ae189b57d95bc2a66ce";
    @Value("${openApi.gyy.appKey}")
    public void setAppKey(Integer appKey){
        GyyApiUtils.APP_KEY = appKey;
    }
    @Value("${openApi.gyy.secretKey}")
    public void setSecretKey(String secretKey) {
        GyyApiUtils.SECRET_KEY = secretKey;
    }
    @Value("${openApi.gyy.sessionKey}")
    public void setSessionKey(String sessionKey) {
        GyyApiUtils.SESSION_KEY = sessionKey;
    }

    /**
     * 查询管易销售订单列表
     * @param method
     * @param startDate
     * @param endDate
     * @return
     *
     * @throws Exception
     */
    public static List<GyyOrderEntity> querySalesList(String method, LocalDateTime startDate, LocalDateTime endDate, Boolean isHistory){
        Integer pageSize = 100;
        Integer pageIndex = 1;
        //总页数
        Integer pageCount = 1;
        List<GyyOrderEntity> infoArrayList = new ArrayList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        while (pageIndex <= pageCount) {
            HashMap<String, Object> params = new HashMap<>(6);
            params.put("date_type", 3);
            params.put("order_state", 0);
            params.put("start_date", sdf.format(startDate));
            params.put("end_date", sdf.format(endDate));
            Map<String, Object> paramMap = getParamMap(method, pageSize, pageIndex, params);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>(2);
            headerMap.put("Content-Type", "text/json");
            JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), null, headerMap, RequestMethod.POST);
            if (!responseMap.getBoolean("success")) {
                log.error("调用url={} param={} {}管易销售订单数据失败 responseMap={}",UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), isHistory?"历史":"", JSONUtil.toJsonStr(responseMap));
                throw new RuntimeException(StrUtil.format("调用url={} param={} {}管易销售订单数据失败 responseMap={}",
                        UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap),  isHistory?"历史":"", JSONUtil.toJsonStr(responseMap)));
            }
            JSONUtil.parseArray(responseMap.getString("orders"));
            List<GyyOrderEntity> dataList = JSONObject.parseArray(responseMap.getString("orders"), GyyOrderEntity.class);
            Integer totalCount = responseMap.getInteger("total");
            pageCount = (totalCount + pageSize - 1) / pageSize;
            if(CollectionUtil.isNotEmpty(dataList)){
                infoArrayList.addAll(dataList);
            }
            pageIndex ++;
        }
        return infoArrayList;
    }

    public static GyyOrderEntity querySalesOrderDetail(String method, String code){
        //总页数
        HashMap<String, Object> params = new HashMap<>(6);
        params.put("code", code);
        Map<String, Object> paramMap = getParamMap(method, 0, 0, params);
        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("Content-Type", "text/json");
        JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), null, headerMap, RequestMethod.POST);
        boolean isHistory = method.contains("history");
        if(null == responseMap || null == responseMap.getBoolean("success")){
            log.error(JSONObject.toJSONString(responseMap));
            return null;
        }
        if (!responseMap.getBoolean("success")) {
            log.error("调用url={} param={} {}管易销售详情订单数据失败 responseMap={}",UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), isHistory ? "历史" : "", JSONUtil.toJsonStr(responseMap));
            throw new RuntimeException(StrUtil.format("调用url={} param={} {}管易销售详情订单数据失败 responseMap={}",
                    UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap),  isHistory ? "历史" : "", JSONUtil.toJsonStr(responseMap)));
        }
        JSONArray orders = responseMap.getJSONArray("orders");
        if(CollectionUtil.isEmpty(orders)){
            return null;
        }
        return JSONObject.parseObject(JSONObject.toJSONString(orders.get(0)), GyyOrderEntity.class);
    }

    /**
     * 查询管易发货订单列表
     * @param method
     * @param startDate
     * @param endDate
     * @return
     *
     * @throws Exception
     */
    public static List<GyyDeliveryDetailEntity> queryDeliveryList(String method, LocalDateTime startDate, LocalDateTime endDate, Boolean isHistory){
        Integer pageSize = 100;
        Integer pageIndex = 1;
        //总页数
        Integer pageCount = 1;
        List<GyyDeliveryDetailEntity> infoArrayList = new ArrayList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        while (pageIndex <= pageCount) {
            HashMap<String, Object> params = new HashMap<>(6);
            if(isHistory){
                params.put("start_delivery_date", sdf.format(startDate));
                params.put("end_delivery_date", sdf.format(endDate));

            }else {
                params.put("start_modify_date", sdf.format(startDate));
                params.put("end_modify_date", sdf.format(endDate));
//                params.put("fields", Arrays.asList("deliveryInfo","goodsInfo","invoiceInfo","uniqueInfo","batchInfo"));
                params.put("delivery", 1);
            }
            Map<String, Object> paramMap = getParamMap(method, pageSize, pageIndex, params);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>(2);
            headerMap.put("Content-Type", "text/json");
            JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), null, headerMap, RequestMethod.POST);
            if (!responseMap.getBoolean("success")) {
                log.error("调用url={} param={} {}管易发货订单数据失败 responseMap={}",UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap),  isHistory?"历史":"", JSONUtil.toJsonStr(responseMap));
                throw new RuntimeException(StrUtil.format("调用url={} param={} {}管易发货订单数据失败 responseMap={}",
                        UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), isHistory?"历史":"", JSONUtil.toJsonStr(responseMap)));
            }
            List<GyyDeliveryDetailEntity> dataList = JSONObject.parseArray(responseMap.getString("deliverys"), GyyDeliveryDetailEntity.class);
            Integer totalCount = responseMap.getInteger("total");
            pageCount = (totalCount + pageSize - 1) / pageSize;
            if(CollectionUtil.isNotEmpty(dataList)){
                infoArrayList.addAll(dataList);
            }
            pageIndex ++;
        }
        return infoArrayList;
    }

    /**
     * 查询发送单详情信息
     * @param method
     * @param code
     * @return GyyDeliveryDetailEntity
     */
    public static GyyDeliveryDetailEntity queryDeliveryOrderDetail(String method, String code){
        //总页数
        HashMap<String, Object> params = new HashMap<>(6);
        params.put("code", code);
        Map<String, Object> paramMap = getParamMap(method, 0, 0, params);
        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("Content-Type", "text/json");
        JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), null, headerMap, RequestMethod.POST);
        boolean isHistory = method.contains("history");
        if (!responseMap.getBoolean("success")) {
            log.error("调用url={} param={} {}管易发货单详情订单数据失败 responseMap={}",UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), isHistory ? "历史" : "", JSONUtil.toJsonStr(responseMap));
            throw new RuntimeException(StrUtil.format("调用url={} param={} {}管易发货单详情订单数据失败 responseMap={}",
                    UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap),  isHistory ? "历史" : "", JSONUtil.toJsonStr(responseMap)));
        }
        JSONArray deliverys = responseMap.getJSONArray("deliverys");
        if(CollectionUtil.isEmpty(deliverys)){
            return null;
        }
        return JSONObject.parseObject(JSONObject.toJSONString(deliverys.get(0)), GyyDeliveryDetailEntity.class);
    }
    /**
     * 查询管易退款信息接口
     * @param method
     * @param startDate
     * @param endDate
     * @return
     *
     * @throws Exception
     */
    public static List<GyyRefundEntity> queryRefundList(String method, LocalDateTime startDate, LocalDateTime endDate) {
        Integer pageSize = 100;
        Integer pageIndex = 1;
        //总页数
        Integer pageCount = 1;
        List<GyyRefundEntity> infoArrayList = new ArrayList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        while (pageIndex <= pageCount) {
            HashMap<String, Object> params = new HashMap<>(6);
            params.put("start_modify_date", sdf.format(startDate));
            params.put("end_modify_date", sdf.format(endDate));
//            params.put("cancel", 0);
            Map<String, Object> paramMap = getParamMap(method,pageSize, pageIndex, params);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>(2);
            headerMap.put("Content-Type", "text/json");
            JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), null, headerMap, RequestMethod.POST);
            if (!responseMap.getBoolean("success")) {
                log.error("调用url={} param={}管易退款订单数据失败 responseMap={}",UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), JSONUtil.toJsonStr(responseMap));
                throw new RuntimeException(StrUtil.format("调用url={} param={}管易退款订单数据失败 responseMap={}",
                        UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), JSONUtil.toJsonStr(responseMap)));
            }
            List<GyyRefundEntity> dataList = JSONObject.parseArray(responseMap.getString("tradeRefunds"), GyyRefundEntity.class);
            Integer totalCount = responseMap.getInteger("total");
            pageCount = (totalCount + pageSize - 1) / pageSize;
            if(CollectionUtil.isNotEmpty(dataList)){
                infoArrayList.addAll(dataList);
            }
            pageIndex ++;
        }
        return infoArrayList;
    }
    /**
     * 查询管易退货信息接口
     * @param method
     * @param startDate
     * @param endDate
     * @return
     *
     * @throws Exception
     */
    public static List<GyyReturnOrderEntity> queryReturnOrderList(String method, LocalDateTime startDate, LocalDateTime endDate) {
        Integer pageSize = 100;
        Integer pageIndex = 1;
        //总页数
        Integer pageCount = 1;
        List<GyyReturnOrderEntity> infoArrayList = new ArrayList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        while (pageIndex <= pageCount) {
            HashMap<String, Object> params = new HashMap<>(6);
            params.put("modify_start_date", sdf.format(startDate));
            params.put("modify_end_date", sdf.format(endDate));
            params.put("receive", 1);
            Map<String, Object> paramMap = getParamMap(method, pageSize, pageIndex, params);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>(2);
            headerMap.put("Content-Type", "text/json");
            JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), null, headerMap, RequestMethod.POST);
            if (!responseMap.getBoolean("success")) {
                log.error("调用url={} param={}管易退货订单数据失败 responseMap={}",UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), JSONUtil.toJsonStr(responseMap));
                throw new RuntimeException(StrUtil.format("调用url={} param={}管易退货订单数据失败 responseMap={}",
                        UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), JSONUtil.toJsonStr(responseMap)));
            }
            List<GyyReturnOrderEntity> dataList = JSONObject.parseArray(responseMap.getString("tradeReturns"), GyyReturnOrderEntity.class);
            Integer totalCount = responseMap.getInteger("total");
            pageCount = (totalCount + pageSize - 1) / pageSize;
            if(CollectionUtil.isNotEmpty(dataList)){
                infoArrayList.addAll(dataList);
            }
            pageIndex ++;
        }
        return infoArrayList;
    }

    /**
     * 查询管易店铺信息接口
     * @param method
     * @param startDate
     * @param endDate
     * @return
     *
     * @throws Exception
     */
    public static List<GyyShopInfoEntity> queryShopList(String method, LocalDateTime startDate, LocalDateTime endDate) {
        Integer pageSize = 100;
        Integer pageIndex = 1;
        //总页数
        Integer pageCount = 1;
        List<GyyShopInfoEntity> infoArrayList = new ArrayList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        while (pageIndex <= pageCount) {
            HashMap<String, Object> params = new HashMap<>(6);
            params.put("modify_start_date", sdf.format(startDate));
            params.put("modify_end_date", sdf.format(endDate));
            Map<String, Object> paramMap = getParamMap(method, pageSize, pageIndex, params);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>(2);
            headerMap.put("Content-Type", "text/json");
            JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), null, headerMap, RequestMethod.POST);
            if (!responseMap.getBoolean("success")) {
                log.error("调用url={} param={}管易店铺数据失败 responseMap={}",UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), JSONUtil.toJsonStr(responseMap));
                throw new RuntimeException(StrUtil.format("调用url={} param={}管易店铺数据失败 responseMap={}",
                        UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), JSONUtil.toJsonStr(responseMap)));
            }
            List<GyyShopInfoEntity> dataList = JSONObject.parseArray(responseMap.getString("shops"), GyyShopInfoEntity.class);
            Integer totalCount = responseMap.getInteger("total");
            pageCount = (totalCount + pageSize - 1) / pageSize;
            if(CollectionUtil.isNotEmpty(dataList)){
                infoArrayList.addAll(dataList);
            }
            pageIndex ++;
        }
        return infoArrayList;
    }

    /**
     * 查询管易店铺信息接口
     * @param method
     * @param startDate
     * @param endDate
     * @return
     *
     * @throws Exception
     */
    public static List<GyySkuInfoEntity> querySkuList(String method, LocalDateTime startDate, LocalDateTime endDate) {
        Integer pageSize = 100;
        Integer pageIndex = 1;
        //总页数
        Integer pageCount = 1;
        List<GyySkuInfoEntity> infoArrayList = new ArrayList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        while (pageIndex <= pageCount) {
            HashMap<String, Object> params = new HashMap<>(6);
            params.put("start_date", sdf.format(startDate));
            params.put("end_date", sdf.format(endDate));
            Map<String, Object> paramMap = getParamMap(method, pageSize, pageIndex, params);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>(2);
            headerMap.put("Content-Type", "text/json");
            JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), null, headerMap, RequestMethod.POST);
            if (!responseMap.getBoolean("success")) {
                log.error("调用url={} param={}管易SKU数据失败 responseMap={}",UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), JSONUtil.toJsonStr(responseMap));
                throw new RuntimeException(StrUtil.format("调用url={} param={}管易SKU数据失败 responseMap={}",
                        UrlContant.GYY_HOST, JSONUtil.toJsonStr(paramMap), JSONUtil.toJsonStr(responseMap)));
            }
            List<GyySkuInfoEntity> dataList = JSONObject.parseArray(responseMap.getString("items"), GyySkuInfoEntity.class);
            Integer totalCount = responseMap.getInteger("total");
            pageCount = (totalCount + pageSize - 1) / pageSize;
            if(CollectionUtil.isNotEmpty(dataList)){
                infoArrayList.addAll(dataList);
            }
            pageIndex ++;
        }
        return infoArrayList;
    }

    private static Map<String, Object> getParamMap(String method, Integer pageSize, Integer pageIndex, Map<String,Object> params) {
        Map<String, Object> paramMap = new HashMap(16);
        paramMap.put("method", method);
        paramMap.put("appkey", GyyApiUtils.APP_KEY);
        paramMap.put("sessionkey", GyyApiUtils.SESSION_KEY);
        paramMap.put("page_no", pageIndex);
        paramMap.put("page_size", pageSize);
        paramMap.putAll(params);
        String paramStr = JSONUtil.toJsonStr(paramMap);
        paramMap.put("sign", sign(paramStr, GyyApiUtils.SECRET_KEY));
        return paramMap;
    }

    /**
     * 得到sign的字符串
     * @Author Luo_WG
     * @Date 2022/11/17 14:46
     * @param jsonDate 请求参数
     * @param secret 秘钥
     * @return java.lang.String
     **/
    public static String sign(String jsonDate, String secret) {
        String enValue = secret +
                jsonDate +
                secret;
        return Md5Util.md5(enValue);
    }

    public static void main(String[] args) {
//        GyyOrderEntity gyyOrder = querySalesOrderDetail("gy.erp.trade.history.detail.get", "SO375039116724");
        GyyOrderEntity gyyOrder1 = querySalesOrderDetail("gy.erp.trade.detail.get", "SO609669500496");
    }

}
