package com.erp.server.dmp.inout.handler.input.task.init.api.tiktok;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.DmpInputTaskHandler;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.tiktok.constant.TikTokConstant;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.OrderDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.WarehouseDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.view.OrdersBean;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import com.sdk.oms.tiktok.util.EncryptionUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@Scope("prototype")
public class TikTokWarehouseApiInitHandler extends DmpInputInitHandler {

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        String json = dmpInputTaskEntity.getExtendJson();

        JSONObject jsonObject = JSON.parseObject(json);

        String nextLevelId = jsonObject.getString("nextLevelId");

        TikTokShopInfoDTO tikTokShopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(nextLevelId);
        if (ObjectUtil.isEmpty(tikTokShopInfoDTO)) {
            throw new ServiceException("TikTok店铺id：" + nextLevelId + "未找到对应的店铺信息");
        }
        String url = TikTokConstant.URL;
        String path = "/logistics/" + TikTokConstant.VERSION + "/warehouses";
        String clientSecret = tikTokShopInfoDTO.getClientSecret();
        String clientId = tikTokShopInfoDTO.getClientId();

        String shopCipher = tikTokShopInfoDTO.getShopCipher();
        String token = tikTokShopInfoDTO.getAccessToken();

        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", token);
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
            log.error("调用url={},入参params={}, TikTok查询仓库失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询仓库失败，返回值 responseMap={}",
                    url + path, headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        WarehouseDTO warehouseDTO = null;
        try {
            warehouseDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), WarehouseDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询仓库返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }
        List<WarehouseDTO.DataDTO.WarehousesDTO> warehouses = warehouseDTO.getData().getWarehouses();
        warehouses = warehouses.stream().filter(v->v.getType().equals("SALES_WAREHOUSE")).collect(Collectors.toList());
        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(warehouses));
        dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);


        return dmpInputTaskInitDTOList;
    }
}
