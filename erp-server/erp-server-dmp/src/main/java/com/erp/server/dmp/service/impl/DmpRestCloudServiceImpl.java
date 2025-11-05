package com.erp.server.dmp.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DmpRestCloudDTO;
import com.erp.server.dmp.service.DmpRestCloudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @param
 * @author Jim
 * @date 2025/10/30 17:11
 * @Return
 */
@Slf4j
@Service
public class DmpRestCloudServiceImpl implements DmpRestCloudService {

    @Value("${restcloud.url:172.16.100.96}")
    private String restcloudUrl;

    @Value("${restcloud.port:8080}")
    private String restcloudPort;


    @Override
    public PagingVO<DmpRestCloudDTO.ListDTO> flowPaging(PagingDTO<DmpRestCloudDTO.PagingParamDTO> dto) {
        // 调用RestCloud接口获取流程列表数据
        // 对应RestCloud接口类：ERP_BEAN_FLOW_LIST
        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", dto.getCurrPage());
        map.put("pageSize", dto.getPageSize());
        map.put("taskCfgType", dto.getParams().getTaskCfgType());
        map.put("flowName", dto.getParams().getFlowName());
        map.put("flowUrl", dto.getParams().getFlowUrl());
        map.put("searchKey", dto.getParams().getSearchKey());
        HttpResponse response = HttpRequest.post("http://"+ restcloudUrl + ":" + restcloudPort + "/restcloud/erp/flow/list")
                .header("Content-Type", "application/json")
                .body(JSON.toJSONString(map))
                .timeout(60000)
                .execute();
        if (200 != response.getStatus()) {
            throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
        }else {
            String body = response.body();
            JSONObject responseJson = JSON.parseObject(body);
            Integer errCode = responseJson.getInteger("errcode");
            // 判断结果异常:ETLProcessRunResultCode
            if (null != errCode && 0 == errCode) {
                List<DmpRestCloudDTO.ListDTO> resultList = responseJson.getJSONArray("rows")
                        .stream()
                        .map(e -> JSON.parseObject(JSON.toJSONString(e)))
                        .map(e -> new DmpRestCloudDTO.ListDTO(e.getString("configName").concat(e.getString("mapUrl")),
                                e.getString("configName"),
                                e.getString("mapUrl").toLowerCase(),
                                CharSequenceUtil.subAfter(e.getString("mapUrl").toLowerCase(), "/", true),
                                e.getString("appId").toLowerCase(),
                                parseAppCategory(e.getString("appId"))
                        )).collect(Collectors.toList());
                return new PagingVO<>(resultList,
                        responseJson.getInteger("total"),
                        responseJson.getInteger("pageSize"),
                        responseJson.getInteger("pageNo")
                );
            }else {
                throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
            }
        }
    }

    private String parseAppCategory(String appId) {
        if (StrUtil.isBlank(appId)) {
            return "";
        }
        // 截取_前面字段
        return StrUtil.subBefore(appId, "_", false).toLowerCase();
    }
}
