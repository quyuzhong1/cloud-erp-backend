package com.erp.server.dmp.inout.handler.input.task.init.api.tiktok;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.dmp.constant.DmpInputConstant;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.sdk.oms.tiktok.constant.TikTokConstant;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.FullyOrderDTO;
import com.sdk.oms.tiktok.service.TikTokFullService;
import com.sdk.oms.tiktok.util.EncryptionUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@Scope("prototype")
public class TikTokFullyOrderApiInitHandler implements DmpInputApiInitHandler {

    @Resource
    private TikTokFullService tikTokFullService;

    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        String nextLevelId = dmpInputApiInitRequest.getNextLevelId();

        TikTokShopInfoDTO shopInfoDTO = tikTokFullService.getShopInfoByShopId(nextLevelId);
        if (ObjectUtil.isEmpty(shopInfoDTO)) {
            throw new ServiceException("TikTok全托管店铺id：" + nextLevelId + "未找到对应的店铺信息");
        }
        Integer pageSize = 50;
        //分页token
        String pageToken = "";
        //平台接口地址
        String url = TikTokConstant.URL;
        //服务密钥
        String toktikInfo = shopInfoDTO.getClientSecret();


        while (true) {
            StringBuffer sb = new StringBuffer();
            //组装授权url
            String path = dmpInputApiInitRequest.getApiType().replace("{version}", TikTokConstant.FULLY_ORDER_VERSION);

            // 定义查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", shopInfoDTO.getAccessToken());
            params.put("app_key", shopInfoDTO.getClientId());
            String timestamp = System.currentTimeMillis() / 1000 + "";
            params.put("timestamp", timestamp);


            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "application/json");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            //请求body，平台用于计算签名
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("page_size", pageSize);
            bodyMap.put("page_token", pageToken);
            
            List<String> orderIds = checkAndGetOrderIds(dmpInputApiInitRequest.getTaskExtendJson());
            if (CollectionUtils.isEmpty(orderIds)){
            	bodyMap.put("latest_status_update_ge", dmpInputApiInitRequest.getStartTime().toInstant(ZoneOffset.ofHours(8)).getEpochSecond());
                bodyMap.put("latest_status_update_lt", dmpInputApiInitRequest.getEndTime().toInstant(ZoneOffset.ofHours(8)).getEpochSecond());
            }else {
            	bodyMap.put("stockup_order_codes", orderIds);
            }
            bodyMap.put("order_types", Collections.singletonList("JIT"));
            String input = EncryptionUtils.urlParamsSort(params, path, headerMap, toktikInfo, JSONUtil.toJsonStr(bodyMap));
            // 追加请求路径获取签名
            String sign = EncryptionUtils.generateSHA256(input, toktikInfo);
            //加入sign签名入参
            params.put("sign", sign);

            //组装url
            sb.append(url);
            sb.append(path);
            sb.append("?access_token=" + shopInfoDTO.getAccessToken() + "");
            sb.append("&app_key=" + shopInfoDTO.getClientId() + "");
            sb.append("&sign=" + sign + "");
            sb.append("&timestamp=" + timestamp + "");

            //拉取数据
            ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, TikTok全托管查询订单数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok全托管查询订单数据失败，返回值 responseMap={}",
                        sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            FullyOrderDTO orderDTO = null;
            try {
                orderDTO = JSON.parseObject(apiResult.getData(),new TypeReference<FullyOrderDTO>() {}.getType());
            } catch (Exception e) {
                log.error("调用url={},入参params={}, 查询全托管订单数据解析失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 查询全托管订单数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            if (CollectionUtil.isEmpty(orderDTO.getData().getStockupOrders())) {
                break;
            }

            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(orderDTO.getData().getStockupOrders()));
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);

            pageToken = orderDTO.getData().getNextPageToken();

            if (StringUtil.isBlank(pageToken)) {
                break;
            }

        }
        return dmpInputTaskInitDTOList;
    }
    
    public List<String> checkAndGetOrderIds(String extendJson) {
        if(StringUtils.isBlank(extendJson)) {
            return Collections.emptyList();
        }
        JSONObject parseObject = JSON.parseObject(extendJson);
        if(null == parseObject) {
            return Collections.emptyList();
        }
        JSONArray jsonArray = parseObject.getJSONArray(DmpInputConstant.ORDER_ID_LIST);
        if (CollectionUtils.isEmpty(jsonArray)){
            return Collections.emptyList();
        }
        return jsonArray.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        long l = LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
        System.out.println(l);
        LocalDateTime updateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(1728914400 + "")), ZoneId.systemDefault());
        System.out.println(updateTime);

    }
}
