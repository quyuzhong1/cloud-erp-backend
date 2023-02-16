package com.erp.server.dmp.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.security.HmacSHA256Utils;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.UrlContant;
import com.erp.model.dmp.mabang.*;
import com.erp.model.dmp.vo.ParamHeaderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 管易API 处理类
 */
@Slf4j
@Component
public class MabangApiUtils {

    private static final Integer NOT_SHIPPED_STATUS = 6;
    private static final Integer NOT_UNSHIPPED_STATUS = 7;


    private static Integer APP_KEY = 200780;

    private static String SECRET_KEY = "13c324fa18feaaeb0ebcc8a7746ebfca";

    @Value("${openApi.mabang.appKey}")
    public void setAppKey(Integer appKey){
        MabangApiUtils.APP_KEY = appKey;
    }

    @Value("${openApi.mabang.secretKey}")
    public void setSecretKey(String secretKey) {
        MabangApiUtils.SECRET_KEY = secretKey;
    }

    /**
     * 查询马帮销售订单列表
     * @param method
     * @param startDate
     * @param endDate
     * @return
     *
     * @throws Exception
     */
    public static List<OrderEntity> querySalesList(String method, LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        String pageSize = "1000";
        String pageIndex = "";
        //总页数
        Integer status = NOT_SHIPPED_STATUS;
        Boolean hasNext = false;
        List<OrderEntity> infoArrayList = new ArrayList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        while (hasNext || NOT_SHIPPED_STATUS.equals(status)) {
            HashMap<String, Object> params = new HashMap<>(10);
            params.put("status", status);
            params.put("canSend", "3");
            if (StrUtil.isNotBlank(pageIndex)){
                params.put("cursor", pageIndex);
            }
            params.put("updateTimeStart", sdf.format(startDate));
            params.put("updateTimeEnd", sdf.format(endDate));
            params.put("maxRows", pageSize);
            ParamHeaderVO paramVo = getParamMap(method, 0, params);
            JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.MABANG_HOST, paramVo.getParamsStr(), null, paramVo.getHeaderMap(), RequestMethod.POST);
            if (!Objects.equals(responseMap.getInteger("code"), 200)) {
                log.error("调用url={} param={} {}马帮销售订单数据失败 responseMap={}",UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap));
                throw new RuntimeException(StrUtil.format("调用url={} param={} {}马帮销售订单数据失败 responseMap={}",
                        UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap)));
            }
            JSONObject dataJson = JSONObject.parseObject(responseMap.getString("data"));
            List<OrderEntity> dataList = JSONObject.parseArray(dataJson.getString("data"), OrderEntity.class);
            hasNext = dataJson.getBoolean("hasNext");
            pageIndex = dataJson.getString("nextCursor");
            if(CollectionUtil.isNotEmpty(dataList)){
                infoArrayList.addAll(dataList);
            }
            if (!hasNext && NOT_SHIPPED_STATUS.equals(status)) {
                status = NOT_UNSHIPPED_STATUS;
                pageIndex = "";
                hasNext = true;
            }
        }
        return infoArrayList;
    }

    /**
     * 查询马帮退款信息接口
     * @param method
     * @param startDate
     * @param endDate
     * @return
     *
     * @throws Exception
     */
    public static List<RefundOrderEntity> queryRefundList(String method, LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        //每页显示的条数 最小10 最大2000
        Integer pageSize = 1000;
        Integer pageIndex = 1;
        //总页数
        Integer pageCount = 1;
        List<RefundOrderEntity> infoArrayList = new ArrayList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        while (pageIndex <= pageCount) {
            HashMap<String, Object> params = new HashMap<>(6);
            params.put("timeStart", sdf.format(startDate));
            params.put("timeEnd", sdf.format(endDate));
            params.put("pageSize", pageSize);
            ParamHeaderVO paramVo = getParamMap(method, pageIndex, params);
            JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.MABANG_HOST, paramVo.getParamsStr(), null, paramVo.getHeaderMap(), RequestMethod.POST);
            if (!Objects.equals(responseMap.getInteger("code"), 200)) {
                log.error("调用url={} param={}马帮退款订单数据失败 responseMap={}",UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap));
                throw new RuntimeException(StrUtil.format("调用url={} param={}马帮退款订单数据失败 responseMap={}",
                        UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap)));
            }
            JSONObject dataJson = JSONObject.parseObject(String.valueOf(responseMap.get("data")));
            List<RefundOrderEntity> dataList = JSONObject.parseArray(dataJson.getString("data"), RefundOrderEntity.class);
//            pageCount = dataJson.getInteger("pageCount");
            Integer totalCount = dataJson.getInteger("total");
            pageCount = (totalCount + pageSize - 1) / pageSize;
            if(CollectionUtil.isNotEmpty(dataList)){
                infoArrayList.addAll(dataList);
            }
            pageIndex ++;
        }
        return infoArrayList;
    }
    /**
     * 查询马帮退货信息接口
     * @param method
     * @param startDate
     * @param endDate
     * @return
     *
     * @throws Exception
     */
    public static List<ReturnOrderEntity> queryReturnOrderList(String method, LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        Integer pageSize = 1000;
        Integer pageIndex = 1;
        //总页数
        Integer pageCount = 1;
        List<ReturnOrderEntity> infoArrayList = new ArrayList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        while (pageIndex <= pageCount) {
            HashMap<String, Object> params = new HashMap<>(6);
            params.put("updateDateStart", sdf.format(startDate));
            params.put("updateDateEnd", sdf.format(endDate));
            params.put("rowsPerPage", pageSize);
            ParamHeaderVO paramVo = getParamMap(method, pageIndex, params);
            JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.MABANG_HOST, paramVo.getParamsStr(), null, paramVo.getHeaderMap(), RequestMethod.POST);
            if (!Objects.equals(responseMap.getInteger("code"), 200)) {
                log.error("调用url={} param={}马帮退货订单数据失败 responseMap={}",UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap));
                throw new RuntimeException(StrUtil.format("调用url={} param={}马帮退货订单数据失败 responseMap={}",
                        UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap)));
            }
            JSONObject dataJson = JSONObject.parseObject(String.valueOf(responseMap.get("data")));
            List<ReturnOrderEntity> dataList = JSONObject.parseArray(dataJson.getString("data"), ReturnOrderEntity.class);
            pageCount = dataJson.getInteger("pageCount");
            if(CollectionUtil.isNotEmpty(dataList)){
                infoArrayList.addAll(dataList);
            }
            pageIndex ++;
        }
        return infoArrayList;
    }

    /**
     * 查询马帮店铺信息接口
     * @param method
     * @return
     *
     * @throws Exception
     */
    public static List<ShopEntity> queryShopList(String method) throws Exception {
        List<ShopEntity> infoArrayList = new ArrayList<>();
        HashMap<String, Object> params = new HashMap<>(6);
        ParamHeaderVO paramVo = getParamMap(method, 0, params);
        JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.MABANG_HOST, paramVo.getParamsStr(), null, paramVo.getHeaderMap(), RequestMethod.POST);
        if (!Objects.equals(responseMap.getInteger("code"), 200)) {
            log.error("调用url={} param={}马帮店铺数据失败 responseMap={}",UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap));
            throw new RuntimeException(StrUtil.format("调用url={} param={}马帮店铺数据失败 responseMap={}",
                    UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap)));
        }
        JSONObject dataJson = JSONObject.parseObject(String.valueOf(responseMap.get("data")));
        List<ShopEntity> dataList = JSONObject.parseArray(dataJson.getString("data"), ShopEntity.class);
        if(CollectionUtil.isNotEmpty(dataList)){
            infoArrayList.addAll(dataList);
        }
        return infoArrayList;
    }

    /**
     * 查询马帮SKU信息接口
     * @param method
     * @param startDate
     * @param endDate
     * @return
     *
     * @throws Exception
     */
    public static List<SkuInfoEntity> querySkuList(String method, LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        // 当前每页条数，默认20，最大值为100
        Integer pageSize = 100;
        Integer pageIndex = 1;
        //总页数
        Integer pageCount = 1;
        List<SkuInfoEntity> infoArrayList = new ArrayList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        while (pageIndex <= pageCount) {
            HashMap<String, Object> params = new HashMap<>(6);
            params.put("updateTimeStart", sdf.format(startDate));
            params.put("updateTimeEnd", sdf.format(endDate));
            params.put("rowsPerPage", pageSize);
            ParamHeaderVO paramVo = getParamMap(method, pageIndex, params);

            JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.MABANG_HOST, paramVo.getParamsStr(), null, paramVo.getHeaderMap(), RequestMethod.POST);
            if (!Objects.equals(responseMap.getInteger("code"), 200)) {
                log.error("调用url={} param={}马帮SKU数据失败 responseMap={}",UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap));
                throw new RuntimeException(StrUtil.format("调用url={} param={}马帮SKU数据失败 responseMap={}",
                        UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap)));
            }
            JSONObject jsonObject = JSONObject.parseObject(responseMap.getString("data"));
            List<SkuInfoEntity> dataList = JSONObject.parseArray(jsonObject.getString("data"), SkuInfoEntity.class);
            pageCount = jsonObject.getInteger("totalPage");
            if(CollectionUtil.isNotEmpty(dataList)){
                infoArrayList.addAll(dataList);
            }
            pageIndex ++;
        }
        return infoArrayList;
    }

    public static List<OrderEntity> queryHistorySalesList(String method, LocalDateTime endDate) throws Exception {
        String pageSize = "1000";
        String pageIndex = "";
        //总页数
        Boolean hasNext = true;
        List<OrderEntity> infoArrayList = new ArrayList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_d.toTimePattern());
        while (hasNext ) {
            HashMap<String, Object> params = new HashMap<>(10);
            if (StrUtil.isNotBlank(pageIndex)){
                params.put("cursor", pageIndex);
            }
            params.put("paidTime", sdf.format(endDate));
            params.put("pageSize", pageSize);
            ParamHeaderVO paramVo = getParamMap(method, 0, params);
            JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.MABANG_HOST, paramVo.getParamsStr(), null, paramVo.getHeaderMap(), RequestMethod.POST);
            if (!Objects.equals(responseMap.getInteger("code"), 200)) {
                log.error("调用url={} param={} {}马帮历史销售订单数据失败 responseMap={}",UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap));
                throw new RuntimeException(StrUtil.format("调用url={} param={} {}马帮历史销售订单数据失败 responseMap={}",
                        UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap)));
            }
            JSONObject dataJson = JSONObject.parseObject(responseMap.getString("data"));
            List<OrderEntity> dataList = JSONObject.parseArray(dataJson.getString("list"), OrderEntity.class);
            hasNext = dataJson.getBoolean("hasNext");
            pageIndex = dataJson.getString("nextCursor");
            if(CollectionUtil.isNotEmpty(dataList)){
                infoArrayList.addAll(dataList);
            }
        }
        return infoArrayList;
    }

    private static ParamHeaderVO getParamMap(String method,Integer pageIndex, Map<String,Object> params) {
        params.put("page", pageIndex);
        Map<String, Object> paramMap = new HashMap(16);
        paramMap.put("api", method);
        paramMap.put("appkey", MabangApiUtils.APP_KEY);
        paramMap.put("version", 1);
        paramMap.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        paramMap.put("data",params);
        String paramStr = JSONUtil.toJsonStr(paramMap);
        String sign = HmacSHA256Utils.hmacSHA256(paramStr, MabangApiUtils.SECRET_KEY);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", sign);
        return new ParamHeaderVO(paramStr, headerMap);
    }


}
