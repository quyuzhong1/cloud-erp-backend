package com.erp.server.dmp.inout.handler.input.task.init.api.mabang;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.UrlContant;
import com.common.business.dto.ParamHeaderVO;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputMabangApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 马帮历史订单接口API
 */
@Service
@Slf4j
@Scope("prototype")
public class MabangOrderHistoryApiInitHandler implements DmpInputApiInitHandler {

	@Override
	public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {

		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        String requestParam = dmpInputApiInitRequest.getRequestParam();
        Map<String, Object> paramMap = JSON.parseObject(requestParam, Map.class);


        String pageSize = "1000";
        String pageIndex = "";
        //总页数
        Boolean hasNext = true;
        List<OrderEntity> infoArrayList = new ArrayList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_d.toTimePattern());
        while (hasNext ) {
            if (StrUtil.isNotBlank(pageIndex)){
                paramMap.put("cursor", pageIndex);
            }
//            params.put("paidTime", sdf.format(endDate));
            paramMap.put("pageSize", pageSize);
            ParamHeaderVO paramVo = MabangTool.getParamMap(dmpInputApiInitRequest.getApiType(), 0, paramMap);
            JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.MABANG_HOST, paramVo.getParamsStr(), null, paramVo.getHeaderMap(), RequestMethod.POST);
            if (!Objects.equals(responseMap.getInteger("code"), 200)) {
                log.error("调用url={} param={} {}马帮历史销售订单数据失败 responseMap={}",UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap));
                throw new RuntimeException(StrUtil.format("调用url={} param={} {}马帮历史销售订单数据失败 responseMap={}",
                        UrlContant.MABANG_HOST, paramVo.getParamsStr(), JSONUtil.toJsonStr(responseMap)));
            }
            JSONObject dataJson = JSONObject.parseObject(responseMap.getString("data"));

//            List<OrderEntity> dataList = JSONObject.parseArray(dataJson.getString("list"), OrderEntity.class);
            hasNext = null != dataJson.getBoolean("hasNext") ?dataJson.getBoolean("hasNext"):Boolean.FALSE;
            pageIndex = dataJson.getString("nextCursor");


            JSONArray data = dataJson.getJSONArray("list");
            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(data.toJSONString());
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
        }
        return dmpInputTaskInitDTOList;
	}

}
