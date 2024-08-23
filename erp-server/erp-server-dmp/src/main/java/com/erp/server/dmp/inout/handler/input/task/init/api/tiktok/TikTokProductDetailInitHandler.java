package com.erp.server.dmp.inout.handler.input.task.init.api.tiktok;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.common.core.anno.ParamData;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.sdk.oms.tiktok.constant.TikTokConstant;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.listing.view.ListingViewDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.view.OrderViewDTO;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import com.sdk.oms.tiktok.util.EncryptionUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class TikTokProductDetailInitHandler extends DmpInputInitHandler {
    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {

        List<Map<String, Object>> findMongoData = null;
        String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
        if (StringUtils.isNotBlank(parentStorageName)) {
            List<ParamData> paramDataList = new ArrayList<>();
            paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
            findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
        }
        if (findMongoData == null) {
            return new ArrayList<>();
        }

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        List<String> productIds = findMongoData.stream().map(req -> req.get("fid").toString()).distinct().collect(Collectors.toList());

        TikTokShopInfoDTO shopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(findMongoData.get(0).get("nextLevelId").toString());
        if (ObjectUtil.isEmpty(shopInfoDTO)) {
            throw new ServiceException("TikTok店铺id：" + nextLevelId + "未找到对应的店铺信息");
        }


        //平台接口地址
        String url = TikTokConstant.URL;
        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);

        for (String productId : productIds) {
            //组装授权url
            String path = dmpCfgApiEntity.getApiType().replace("{version}", TikTokConstant.VERSION).replace("{productId}", productId);

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
            Integer code = 0;
            int count = 0;
			long sleepTime = 1000;
			ApiResult apiResult = new ApiResult();
			while (code != 200) {
                //拉取数据
                apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
                code = apiResult.getCode();
                if (!Objects.equals(code, 200)) {
                    if (count == 5) {
                        throw new ServiceException(StrUtil.format("调用TikTok={},接口重试{}次失败, 返回值 responseMap={}", dmpCfgApiEntity.getApiType(), count, JSONUtil.toJsonStr(apiResult)));
                    }
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                    }
                    sleepTime = sleepTime + 1000;
                    count = count + 1;
                }
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

            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(listingViewDTO.getData()));
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);

        }
        return dmpInputTaskInitDTOList;

    }
}
