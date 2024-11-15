package com.erp.server.dmp.inout.handler.input.task.init.api.mercado;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.common.core.anno.ParamData;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.mercado.constant.MercadoConstant;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercado.dto.mercado.order.OrderViewDTO;
import com.sdk.oms.mercado.dto.mercado.returnOrder.ReturnDTO;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import com.sdk.oms.tiktok.dto.tiktok.order.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@Scope("prototype")
public class MercadoReturnApiInitHandler implements DmpInputApiInitHandler {
    @Resource
    private MercadoSdkClientService mercadoSdkClientService;
    @Resource
    private MongoService mongoService;

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

        List<Map<String, Object>> findMongoData = null;
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(MercadoConstant.MONGO_BASE_USER_ID, MercadoConstant.MONGO_BASE_USER_ID, PannoEnum.EQ, shopInfoDTO.getUserId()));
        findMongoData = mongoService.findMongoData(paramDataList, "mercadolibre_marketplace_data");
        if (CollectionUtil.isEmpty(findMongoData)) {
            return new ArrayList<>();
        }


        List<String> userMarketplacesIds = new ArrayList<>();
        for (Map<String, Object> findMongoDatum : findMongoData) {
            Object marketplaces = findMongoDatum.get("marketplaces");
            if (ObjectUtil.isNotEmpty(marketplaces)) {
                List<Object> marketplacesList = (List<Object>) marketplaces;
                for (Object o : marketplacesList) {
                    Map<String, Object> map = (Map<String, Object>) o;
                    userMarketplacesIds.add(String.valueOf(map.get("user_id")));
                }
            }
        }

        //平台只支持单日查询，需要根据日期范围一天一天查
        List<String> dateDayList = LocalDateUtil.getDateDayList(dmpInputApiInitRequest.getStartTime(), dmpInputApiInitRequest.getEndTime());


        //平台接口地址
        String url = MercadoConstant.URL;
        //每次最多获取50条
        Integer pageSize = 50;
        //当前页数
        Integer pageNo = 0;

        for (String id : userMarketplacesIds) {
            String path = dmpInputApiInitRequest.getApiType();

            //平台只支持单日查询，需要根据日期范围一天一天查
            for (String time : dateDayList) {

                //每次最多获取50条
                pageSize = 50;
                //当前页数
                pageNo = 0;

                Boolean nexflag = true;

                while (nexflag) {
                    int offset = pageSize * pageNo;

                    StringBuffer sb = new StringBuffer();
                    sb.append(url);
                    sb.append(path);

                    //入参
                    HashMap<String, Object> returnParams = new HashMap<>();
                    returnParams.put("user_id", id);
                    returnParams.put("last_updated", time);
                    returnParams.put("limit", pageSize);
                    returnParams.put("offset", offset);

                    //设置请求头
                    Map<String, String> headerMap = new HashMap<>(1);
                    headerMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());

                    //拉取数据
                    ApiResult apiResult = new ApiResult();
                    Object data = null;
                    long sleepTime = 1000;
                    int count = 0;
                    while(ObjectUtil.isEmpty(data)) {
                        apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(returnParams), null, headerMap, RequestMethod.GET);
                        if(apiResult.getMsg().equalsIgnoreCase("Read timed out")) {
                            if(count == 10) {
                                nexflag = false;
                                throw new ServiceException("调用美客多" + url + path + "接口重试" + count + "失败");
                            }
                            try {
                                Thread.sleep(sleepTime);
                            } catch (InterruptedException e) {
                            	Thread.currentThread().interrupt();
                            }
                            sleepTime = sleepTime + 1000;
                            count = count + 1;
                        }
                        data = apiResult.getData();
                    }

                    if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
                        nexflag = false;
                        log.error("调用url={},入参params={}, 美客多退货退款数据失败，返回值 responseMap={}", sb.toString(), returnParams.toString(), JSONUtil.toJsonStr(apiResult));
                        throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                                sb.toString(), returnParams.toString(), JSONUtil.toJsonStr(apiResult)));
                    }

                    //解析数据
                    ObjectMapper objectMapper = new ObjectMapper();
                    ReturnDTO returnDTO = null;
                    try {
                        returnDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), ReturnDTO.class);
                    } catch (JsonProcessingException e) {
                        log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", url + path, returnParams.toString(), JSONUtil.toJsonStr(apiResult.getData()));
                        throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                                url + path, returnParams.toString(), JSONUtil.toJsonStr(apiResult.getData())));
                    }


                    //解析数据
                    if (CollectionUtils.isEmpty(returnDTO.getData())) {
                        nexflag = false;
                        break;
                    }
                    pageNo++;

                    DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
                    dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(returnDTO.getData()));
                    dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);

                }
            }
        }
        return dmpInputTaskInitDTOList;

    }


}
