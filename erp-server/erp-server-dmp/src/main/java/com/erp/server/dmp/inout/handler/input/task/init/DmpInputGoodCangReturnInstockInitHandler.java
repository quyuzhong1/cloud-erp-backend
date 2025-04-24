package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.sdk.wms.goodcang.dto.request.GoodCangGetOutBoundReq;
import com.sdk.wms.goodcang.dto.request.GoodCangGetReturnInstockReq;
import com.sdk.wms.goodcang.dto.response.GoodCangReceiptBatchResp;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.dto.response.GoodCangReturnInstockResp;
import com.sdk.wms.goodcang.utils.GoodCangUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputGoodCangReturnInstockInitHandler extends DmpInputInitHandler {

    @Resource
    private DmpHandlerCache dmpHandlerCache;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();

        GoodCangGetReturnInstockReq goodCangGetReturnInstockReq = new GoodCangGetReturnInstockReq();
        goodCangGetReturnInstockReq.setStartUpdateTime(dmpInputTaskEntity.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        goodCangGetReturnInstockReq.setEndUpdateTime(dmpInputTaskEntity.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        goodCangGetReturnInstockReq.setPageSize(100);
        int page = 1;
        int currTotal = 0;
        List<JSONObject> allResult = new ArrayList<>();
        List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(DmpBasicSystemCodeEnum.GOODCANG.getCode()));
        if (CollUtil.isEmpty(overseasProviderEntityList)) {
            throw new ServiceException("谷仓授权信息不存在");
        }
        // 取对应授权ID授权
        OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);
        if(null == overseasProviderEntity) {
            throw new ServiceException("谷仓对应授权ID信息不存在");
        }
        String authId = overseasProviderEntity.getId();
        ThirdWarehouseContext.setAuthMap(overseasProviderEntity.getAuthJson());
        while (true) {
            goodCangGetReturnInstockReq.setCurrentPage(page);
            log.debug("请求谷仓退货入库单请求:{}", JSON.toJSONString(goodCangGetReturnInstockReq));
            String response = GoodCangUtils.sendPost(apiType, JSON.toJSONString(goodCangGetReturnInstockReq));
            log.debug("请求谷仓退货入库单响应:{}", response);
            // 空数据处理
            // {"ask":"Failure","message":"没有数据(ERROR ID 99-UVU8HX)","Error":{"errCode":"400","errMessage":"没有数据(ERROR ID 99-UVU8HX)"}}
            JSONObject jsonObject = JSONObject.parseObject(response);
            JSONObject errorObj = jsonObject.getJSONObject("Error");
            if (null != errorObj) {
                String errCode = errorObj.getString("errCode");
                String errMessage = errorObj.getString("errMessage");
                if ("400".equalsIgnoreCase(errCode) && errMessage.contains("没有数据")) {
                    break;
                }
                ServiceException.runError("谷仓接口返回异常:" + response);
            }

            GoodCangResponse<List<GoodCangReturnInstockResp>> result = JSONObject.parseObject(response, new TypeReference<GoodCangResponse<List<Object>>>() {
            }.getType());
            List<?> data = result.getData();
            int size = data.size();
            if (size == 0) {
                break;
            }
            List<JSONObject> jsonObjList = data.stream().map(e -> {
                        JSONObject jsonItemObj = (JSONObject) JSON.toJSON(e);
                        jsonItemObj.put("authId", authId);
                        return jsonItemObj;}
            ).collect(Collectors.toList());
            allResult.addAll(jsonObjList);
            currTotal = currTotal + size;
            Integer count = result.getCount();
            if (count == null) {
                count = 0;
            }
            if (currTotal >= count) {
                break;
            }
            page = page + 1;
        }
        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(allResult));
        return Collections.singletonList(dmpInputTaskInitDTO);
    }

}
