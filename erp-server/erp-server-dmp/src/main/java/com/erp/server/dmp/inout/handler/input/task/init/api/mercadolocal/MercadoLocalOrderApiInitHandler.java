package com.erp.server.dmp.inout.handler.input.task.init.api.mercadolocal;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
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
import com.sdk.oms.mercadolocal.constant.MercadoConstant;
import com.sdk.oms.mercadolocal.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.order.OrderDataDTO;
import com.sdk.oms.mercadolocal.service.MercadoLocalSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@Slf4j
@Scope("prototype")
public class MercadoLocalOrderApiInitHandler implements DmpInputApiInitHandler {
    @Resource
    private MercadoLocalSdkClientService mercadoLocalSdkClientService;


    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        String nextLevelId = dmpInputApiInitRequest.getNextLevelId();


        MercadoShopInfoDTO shopInfoDTO = mercadoLocalSdkClientService.getShopInfoByShopId(nextLevelId);
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
            sb.append("?seller=");
            sb.append(shopInfoDTO.getUserId());
            sb.append("&limit=");
            sb.append(pageSize);
            sb.append("&offset=");
            sb.append(offset);
            sb.append("&order.date_last_updated.from=");
            sb.append(this.dateToStr(dmpInputApiInitRequest.getStartTime()));
            sb.append("&order.date_last_updated.to=");
            sb.append(this.dateToStr(dmpInputApiInitRequest.getEndTime()));
            sb.append("&order.status=");
            sb.append("cancelled,paid,invalid");
            //入参
            HashMap<String, Object> params = new HashMap<>(2);

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
                log.error("调用url={},入参params={}, 美客多marketplace/orders/search数据失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(CharSequenceUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            ObjectMapper objectMapper = new ObjectMapper();
            OrderDataDTO orderDTO = null;
            try {
                orderDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), OrderDataDTO.class);
            } catch (JsonProcessingException e) {
                nexflag = false;
                log.error("美客多orders/search接口数据解析错误，数据={}", apiResult.getData());
                throw new RuntimeException(CharSequenceUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
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

    private String dateToStr(LocalDateTime dateTime) {

        // 转换为UTC时区的OffsetDateTime
        OffsetDateTime utcTime = dateTime
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);

        // 自定义格式化
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String formatted = utcTime.format(formatter);

        // 字符串替换
        String target = formatted.replace("+00:00", "-00");
        return target;
    }
}
