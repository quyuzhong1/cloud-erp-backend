package com.erp.server.dmp.inout.handler.input.task.init.api.mercado;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.mercado.constant.MercadoConstant;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercado.dto.mercado.order.OrderDTO;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@Scope("prototype")
public class MercadoOrderApiInitHandler implements DmpInputApiInitHandler {
    @Resource
    private MercadoSdkClientService mercadoSdkClientService;


    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        String nextLevelId = dmpInputApiInitRequest.getNextLevelId();


        MercadoShopInfoDTO shopInfoDTO = mercadoSdkClientService.getShopInfoByShopId(nextLevelId);
        if (ObjectUtil.isEmpty(shopInfoDTO)) {
            throw new ServiceException("美客多店铺id：" + nextLevelId + "未找到对应的店铺信息");
        }


        String url = MercadoConstant.URL;
        String path = dmpInputApiInitRequest.getApiType();

        //每次最多获取50条
        Integer pageSize = 50;
        //当前页数
        Integer pageNo = 0;

        Boolean nexflag = true;

        while (nexflag) {
            int offset = pageSize * pageNo;

            StringBuffer sb = new StringBuffer();
            sb.append(url);
            sb.append(path);

            //入参
            HashMap<String, Object> params = new HashMap<>(2);
//            params.put("seller.id", "1511265855");
//            params.put("seller.id", shopInfoDTO.getUserId());
            params.put("order.status", "cancelled,paid,invalid");
            params.put("last_updated.from", dmpInputApiInitRequest.getStartTime());
            params.put("last_updated.to", dmpInputApiInitRequest.getEndTime());
            params.put("limit", pageSize);
            params.put("offset", offset);
            //设置请求头
            Map<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());

            //拉取数据
            ApiResult apiResult = new ApiResult();
            Object data = null;
            long sleepTime = 1000;
            int count = 0;
            while(ObjectUtil.isEmpty(data)) {
                apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
                if(apiResult.getMsg().equalsIgnoreCase("Read timed out")) {
                    if(count == 10) {
                        nexflag = false;
                        throw new ServiceException("调用美客多" + url + path + "接口重试" + count + "失败");
                    }
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {}
                    sleepTime = sleepTime + 1000;
                    count = count + 1;
                }
                data = apiResult.getData();
            }

            if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
                nexflag = false;
                log.error("调用url={},入参params={}, 美客多marketplace/orders/search数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            ObjectMapper objectMapper = new ObjectMapper();
            OrderDTO orderDTO = null;
            try {
                orderDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), OrderDTO.class);
            } catch (JsonProcessingException e) {
                nexflag = false;
                log.error("美客多orders/search接口数据解析错误，数据={}", apiResult.getData());
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            //解析数据
//            OrderDTO orderDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), OrderDTO.class);
            if (CollectionUtils.isEmpty(orderDTO.getResults())) {
                nexflag = false;
                break;
            }
            pageNo++;

            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(orderDTO.getResults()));
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);

        }
        return dmpInputTaskInitDTOList;
    }
}
