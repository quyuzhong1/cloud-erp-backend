package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDetailDTO;
import com.erp.model.dmp.dto.DmpRestCloudDTO;
import com.erp.server.dmp.service.DmpRestCloudService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Collections;
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

    @Override
    public PagingVO<AdsErpOutstockDiffFlowDetailDTO.SourceSelfDTO> outstockSourceSelfPaging(PagingDTO<AdsErpOutstockDiffFlowDetailDTO.PagingParamDTO> dto) {
        // 对应RestCloud接口类：ERP_BEAN_FLOW_LIST
        Map<String, Object> map = new HashMap<>();
        map.put("currPage", dto.getCurrPage());
        map.put("pageSize", dto.getPageSize());
        JSONObject data = new JSONObject();
        data.put("ids", dto.getParams().getIds().stream()
                .collect(Collectors.joining("','", "'", "'")));
        map.put("data", Collections.singletonList(data));
        HttpResponse response = HttpRequest.post("http://"+ restcloudUrl + ":" + restcloudPort + "/restcloud/ods_dmp_clean/clean_so_outstock_source_self")
                .header("Content-Type", "application/json")
                .body(JSON.toJSONString(map))
                .timeout(60000)
                .execute();
        if (200 != response.getStatus()) {
            throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
        }else {
            String body = response.body();
            JSONObject responseJson = JSON.parseObject(body);
            Boolean state = responseJson.getBoolean("state");
            // 判断结果异常:ETLProcessRunResultCode
            if (null != state && state) {
                List<AdsErpOutstockDiffFlowDetailDTO.SourceSelfDTO> resultList = responseJson.getJSONArray("data")
                        .stream()
                        .map(e -> JSONUtil.toBean(JSONUtil.toJsonStr(e),AdsErpOutstockDiffFlowDetailDTO.SourceSelfDTO.class))
                        .collect(Collectors.toList());
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

    @Override
    public PagingVO<AdsErpOutstockDiffFlowDetailDTO.SourcePlatformDTO> outstockSourcePlatformPaging(PagingDTO<AdsErpOutstockDiffFlowDetailDTO.PagingParamDTO> dto) {
        // 对应RestCloud接口类：ERP_BEAN_FLOW_LIST
        Map<String, Object> map = new HashMap<>();
        map.put("currPage", dto.getCurrPage());
        map.put("pageSize", dto.getPageSize());
        JSONObject data = new JSONObject();
        data.put("ids", dto.getParams().getIds().stream()
                .collect(Collectors.joining("','", "'", "'")));
        map.put("data", Collections.singletonList(data));
        HttpResponse response = HttpRequest.post("http://"+ restcloudUrl + ":" + restcloudPort + "/restcloud/ods_dmp_clean/clean_so_outstock_source_platform")
                .header("Content-Type", "application/json")
                .body(JSON.toJSONString(map))
                .timeout(60000)
                .execute();
        if (200 != response.getStatus()) {
            throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
        }else {
            String body = response.body();
            JSONObject responseJson = JSON.parseObject(body);
            Boolean state = responseJson.getBoolean("state");
            // 判断结果异常
            if (null != state && state) {
                List<AdsErpOutstockDiffFlowDetailDTO.SourcePlatformDTO> resultList = responseJson.getJSONArray("data")
                        .stream()
                        .map(e -> JSONUtil.toBean(JSONUtil.toJsonStr(e),AdsErpOutstockDiffFlowDetailDTO.SourcePlatformDTO.class))
                        .collect(Collectors.toList());
                return new PagingVO<>(resultList,
                        responseJson.getInteger("total"),
                        responseJson.getInteger("pageSize"),
                        responseJson.getInteger("currPage")
                );
            }else {
                throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
            }
        }
    }

    @Override
    public PagingVO<AdsErpInventoryDiffFlowDetailDTO.SourceSelfDTO> inventorySourceSelfPaging(PagingDTO<AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO> dto) {
        // 对应RestCloud接口类：ERP_BEAN_FLOW_LIST
        Map<String, Object> map = new HashMap<>();
        map.put("currPage", dto.getCurrPage());
        map.put("pageSize", dto.getPageSize());
        JSONObject data = new JSONObject();
        data.put("ids", dto.getParams().getIds().stream()
                .collect(Collectors.joining("','", "'", "'")));
        map.put("data", Collections.singletonList(data));
        HttpResponse response = HttpRequest.post("http://"+ restcloudUrl + ":" + restcloudPort + "/restcloud/ods_dmp_clean/clean_inventory_source_platform")
                .header("Content-Type", "application/json")
                .body(JSON.toJSONString(map))
                .timeout(60000)
                .execute();
        if (200 != response.getStatus()) {
            throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
        }else {
            String body = response.body();
            JSONObject responseJson = JSON.parseObject(body);
            Boolean state = responseJson.getBoolean("state");
            // 判断结果异常:ETLProcessRunResultCode
            if (null != state && state) {
                List<AdsErpInventoryDiffFlowDetailDTO.SourceSelfDTO> resultList = responseJson.getJSONArray("data")
                        .stream()
                        .map(e -> JSONUtil.toBean(JSONUtil.toJsonStr(e),AdsErpInventoryDiffFlowDetailDTO.SourceSelfDTO.class))
                        .collect(Collectors.toList());
                return new PagingVO<>(resultList,
                        responseJson.getInteger("total"),
                        responseJson.getInteger("pageSize"),
                        responseJson.getInteger("currPage")
                );
            }else {
                throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
            }
        }
    }

    @Override
    public PagingVO<AdsErpInventoryDiffFlowDetailDTO.SourcePlatformDTO> inventorySourcePlatformPaging(PagingDTO<AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO> dto) {
        // 对应RestCloud接口类：ERP_BEAN_FLOW_LIST
        Map<String, Object> map = new HashMap<>();
        map.put("currPage", dto.getCurrPage());
        map.put("pageSize", dto.getPageSize());
        JSONObject data = new JSONObject();
        data.put("ids", dto.getParams().getIds().stream()
                .collect(Collectors.joining("','", "'", "'")));
        map.put("data", Collections.singletonList(data));
        HttpResponse response = HttpRequest.post("http://"+ restcloudUrl + ":" + restcloudPort + "/restcloud/ods_dmp_clean/clean_inventory_source_self")
                .header("Content-Type", "application/json")
                .body(JSON.toJSONString(map))
                .timeout(60000)
                .execute();
        if (200 != response.getStatus()) {
            throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
        }else {
            String body = response.body();
            JSONObject responseJson = JSON.parseObject(body);
            Boolean state = responseJson.getBoolean("state");
            // 判断结果异常:ETLProcessRunResultCode
            if (null != state && state) {
                List<AdsErpInventoryDiffFlowDetailDTO.SourcePlatformDTO> resultList = responseJson.getJSONArray("data")
                        .stream()
                        .map(e -> JSONUtil.toBean(JSONUtil.toJsonStr(e),AdsErpInventoryDiffFlowDetailDTO.SourcePlatformDTO.class))
                        .collect(Collectors.toList());
                return new PagingVO<>(resultList,
                        responseJson.getInteger("total"),
                        responseJson.getInteger("pageSize"),
                        responseJson.getInteger("currPage")
                );
            }else {
                throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
            }
        }
    }

    @Override
    public PagingVO<AdsErpDiffOutstockSyncDTO.SourcePlatformDTO> diffOutstockSyncSourcePlatformPaging(PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> dto) {
        // 对应RestCloud接口类：ERP_BEAN_FLOW_LIST
        Map<String, Object> map = new HashMap<>();
        map.put("currPage", dto.getCurrPage() - 1);
        map.put("pageSize", dto.getPageSize());
        StringBuffer sb = new StringBuffer();
        AdsErpDiffOutstockSyncDTO.PagingParamDTO params = dto.getParams();
        if(CollUtil.isNotEmpty(params.getIds())){
            sb.append(StrUtil.format(" and t.id in ({}) ",params.getIds().stream()
                    .collect(Collectors.joining("','", "'", "'"))));
        }
        if(StringUtils.isNotBlank(sb.toString())){
            map.put("sql", sb.toString());
        }
        HttpResponse response = HttpRequest.post("http://"+ restcloudUrl + ":" + restcloudPort + "/restcloud/ods_dmp_clean/"+params.getType())
                .header("Content-Type", "application/json")
                .body(JSON.toJSONString(map))
                .timeout(60000)
                .execute();
        if (200 != response.getStatus()) {
            throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
        }else {
            String body = response.body();
            JSONObject responseJson = JSON.parseObject(body);
            Boolean state = responseJson.getBoolean("state");
            // 判断结果异常:ETLProcessRunResultCode
            if (null != state && state) {
                List<AdsErpDiffOutstockSyncDTO.SourcePlatformDTO> resultList = responseJson.getJSONArray("data")
                        .stream()
                        .map(e -> JSONUtil.toBean(JSONUtil.toJsonStr(e),AdsErpDiffOutstockSyncDTO.SourcePlatformDTO.class))
                        .collect(Collectors.toList());
                return new PagingVO<>(resultList,
                        responseJson.getInteger("totalDataCount"),
                        responseJson.getInteger("pageSize"),
                        responseJson.getInteger("pageNo")
                );
            }else {
                throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
            }
        }
    }

    @Override
    public PagingVO<AdsErpDiffReturnInstockSyncDTO.SourcePlatformDTO> diffReturnInstockSourcePlatformPaging(PagingDTO<AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> dto) {
        // 对应RestCloud接口类：ERP_BEAN_FLOW_LIST
        Map<String, Object> map = new HashMap<>();
        map.put("currPage", dto.getCurrPage() - 1);
        map.put("pageSize", dto.getPageSize());
        StringBuffer sb = new StringBuffer();
        AdsErpDiffReturnInstockSyncDTO.PagingParamDTO params = dto.getParams();
        if(CollUtil.isNotEmpty(params.getIds())){
            sb.append(StrUtil.format(" and t.id in ({}) ",params.getIds().stream()
                    .collect(Collectors.joining("','", "'", "'"))));
        }

        if(StringUtils.isNotBlank(sb.toString())){
            map.put("sql", sb.toString());
        }
        HttpResponse response = HttpRequest.post("http://"+ restcloudUrl + ":" + restcloudPort + "/restcloud/ods_dmp_clean/"+params.getType())
                .header("Content-Type", "application/json")
                .body(JSON.toJSONString(map))
                .timeout(60000)
                .execute();
        if (200 != response.getStatus()) {
            throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
        }else {
            String body = response.body();
            JSONObject responseJson = JSON.parseObject(body);
            Boolean state = responseJson.getBoolean("state");
            // 判断结果异常:ETLProcessRunResultCode
            if (null != state && state) {
                List<AdsErpDiffReturnInstockSyncDTO.SourcePlatformDTO> resultList = responseJson.getJSONArray("data")
                        .stream()
                        .map(e -> JSONUtil.toBean(JSONUtil.toJsonStr(e),AdsErpDiffReturnInstockSyncDTO.SourcePlatformDTO.class))
                        .collect(Collectors.toList());
                return new PagingVO<>(resultList,
                        responseJson.getInteger("totalDataCount"),
                        responseJson.getInteger("pageSize"),
                        responseJson.getInteger("pageNo")
                );
            }else {
                throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
            }
        }
    }

    @Override
    public PagingVO<AdsErpReceiveFlowDiffDetailDTO.SourceTransferInfoDTO> transferInfoPaging(PagingDTO<AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO> dto) {
        // 对应RestCloud接口类：ERP_BEAN_FLOW_LIST
        Map<String, Object> map = new HashMap<>();
        map.put("currPage", dto.getCurrPage());
        map.put("pageSize", dto.getPageSize());
        JSONObject data = new JSONObject();
        data.put("ids", dto.getParams().getIds().stream()
                .collect(Collectors.joining("','", "'", "'")));
        map.put("data", Collections.singletonList(data));
        HttpResponse response = HttpRequest.post("http://"+ restcloudUrl + ":" + restcloudPort + "/restcloud/ods_dmp_clean/clean_source_transfer_info")
                .header("Content-Type", "application/json")
                .body(JSON.toJSONString(map))
                .timeout(60000)
                .execute();
        if (200 != response.getStatus()) {
            throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
        }else {
            String body = response.body();
            JSONObject responseJson = JSON.parseObject(body);
            Boolean state = responseJson.getBoolean("state");
            // 判断结果异常:ETLProcessRunResultCode
            if (null != state && state) {
                List<AdsErpReceiveFlowDiffDetailDTO.SourceTransferInfoDTO> resultList = responseJson.getJSONArray("data")
                        .stream()
                        .map(e -> JSONUtil.toBean(JSONUtil.toJsonStr(e),AdsErpReceiveFlowDiffDetailDTO.SourceTransferInfoDTO.class))
                        .collect(Collectors.toList());
                return new PagingVO<>(resultList,
                        responseJson.getInteger("total"),
                        responseJson.getInteger("pageSize"),
                        responseJson.getInteger("currPage")
                );
            }else {
                throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
            }
        }
    }

    @Override
    public PagingVO<AdsErpReceiveFlowDiffDetailDTO.SourcePlatformFlowDTO> sourcePlatformFlowPaging(PagingDTO<AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO> dto) {
        // 对应RestCloud接口类：ERP_BEAN_FLOW_LIST
        Map<String, Object> map = new HashMap<>();
        map.put("currPage", dto.getCurrPage());
        map.put("pageSize", dto.getPageSize());
        JSONObject data = new JSONObject();
        data.put("ids", dto.getParams().getIds().stream()
                .collect(Collectors.joining("','", "'", "'")));
        map.put("data", Collections.singletonList(data));
        HttpResponse response = HttpRequest.post("http://"+ restcloudUrl + ":" + restcloudPort + "/restcloud/ods_dmp_clean/clean_source_platform_flow")
                .header("Content-Type", "application/json")
                .body(JSON.toJSONString(map))
                .timeout(60000)
                .execute();
        if (200 != response.getStatus()) {
            throw new ServiceException("调用restCloud流程信息错误:{}", response.body());
        }else {
            String body = response.body();
            JSONObject responseJson = JSON.parseObject(body);
            Boolean state = responseJson.getBoolean("state");
            // 判断结果异常:ETLProcessRunResultCode
            if (null != state && state) {
                List<AdsErpReceiveFlowDiffDetailDTO.SourcePlatformFlowDTO> resultList = responseJson.getJSONArray("data")
                        .stream()
                        .map(e -> JSONUtil.toBean(JSONUtil.toJsonStr(e),AdsErpReceiveFlowDiffDetailDTO.SourcePlatformFlowDTO.class))
                        .collect(Collectors.toList());
                return new PagingVO<>(resultList,
                        responseJson.getInteger("total"),
                        responseJson.getInteger("pageSize"),
                        responseJson.getInteger("currPage")
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
