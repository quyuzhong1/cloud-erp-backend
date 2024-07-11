package com.erp.server.dmp.inout.handler.input.task.init.api;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.common.business.constant.UrlContant;
import com.common.business.dto.ParamHeaderVO;
import com.common.core.security.HmacSHA256Utils;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.enums.DmpInputTaskFileContentTypeEnum;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputKingdeeApiInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputMabangApiInitRequest;
import com.erp.server.dmp.utils.MabangApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * dmp输入init任务基础处理器下的金蝶api获取数据方式
 * @author Administrator
 *
 */
@Service
@Slf4j
@Scope("prototype")
public class DmpInputMabangApiInitHandler implements DmpInputApiInitHandler{
    private static final Integer NOT_SHIPPED_STATUS = 6;
    private static final Integer NOT_UNSHIPPED_STATUS = 7;
    private static Integer APP_KEY = 200780;

    private static String SECRET_KEY = "13c324fa18feaaeb0ebcc8a7746ebfca";
	@Override
	public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());

		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
        DmpInputMabangApiInitRequest dmpInputMabangApiInitRequest = (DmpInputMabangApiInitRequest) dmpInputApiInitRequest;

        String requestParam = dmpInputMabangApiInitRequest.getRequestParam();
        Map<String, Object> paramMap = JSON.parseObject(requestParam, Map.class);

        String pageSize = "1000";
        String pageIndex = "";
        //总页数
        Integer status = NOT_SHIPPED_STATUS;
        Boolean hasNext = false;

        while (hasNext || NOT_SHIPPED_STATUS.equals(status)) {
            paramMap.put("status", status);
            if (StrUtil.isNotBlank(pageIndex)){
                paramMap.put("cursor", pageIndex);
            }
            paramMap.put("updateTimeStart", dmpInputMabangApiInitRequest.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            paramMap.put("updateTimeEnd", dmpInputMabangApiInitRequest.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            paramMap.put("maxRows", pageSize);
            ParamHeaderVO paramVo = getParamMap(dmpInputMabangApiInitRequest.getApiType(), 0, paramMap);
            JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.MABANG_HOST, paramVo.getParamsStr(), null, paramVo.getHeaderMap(), RequestMethod.POST);
            if (!Objects.equals(responseMap.getInteger("code"), 200)) {
                log.error("调用url={} param={} {}马帮销售订单数据失败 responseMap={}", UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap));
                throw new RuntimeException(StrUtil.format("调用url={} param={} {}马帮销售订单数据失败 responseMap={}",
                        UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap)));
            }
            JSONObject dataJson = JSONObject.parseObject(responseMap.getString("data"));
            hasNext = dataJson.getBoolean("hasNext");
            pageIndex = dataJson.getString("nextCursor");

            if (!hasNext && NOT_SHIPPED_STATUS.equals(status)) {
                status = NOT_UNSHIPPED_STATUS;
                pageIndex = "";
                hasNext = true;
            }
            JSONArray data = dataJson.getJSONArray("data");
            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(data.toJSONString());
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
        }
		return dmpInputTaskInitDTOList;
	}

    private static ParamHeaderVO getParamMap(String method,Integer pageIndex, Map<String,Object> params) {
        params.put("page", pageIndex);
        Map<String, Object> paramMap = new HashMap(16);
        paramMap.put("api", method);
        paramMap.put("appkey", APP_KEY);
        paramMap.put("version", 1);
        paramMap.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        paramMap.put("data",params);
        String paramStr = JSONUtil.toJsonStr(paramMap);
        String sign = HmacSHA256Utils.hmacSHA256(paramStr, SECRET_KEY);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", sign);
        return new ParamHeaderVO(paramStr, headerMap);
    }

}
