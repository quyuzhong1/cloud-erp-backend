package com.erp.server.dmp.inout.handler.input.task.init;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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
import com.sdk.wms.goodcang.dto.request.GoodCangGetSkuReq;
import com.sdk.wms.goodcang.dto.response.GoodCangReceiptBatchResp;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.utils.GoodCangUtils;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputGoodCangInboundInitHandler extends DmpInputInitHandler{

	@Resource
    private DmpHandlerCache dmpHandlerCache;
	
	@Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        //查询待签收、部分签收状态的入库单
        List<String> receiveCodeList = overseasWarehouseFeign.getReceiptNumbersForStatus(Arrays.asList(OverseasInstockStatusEnum.TO_BE_SIGNED.getCode()
                ,OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode()
                ,OverseasInstockStatusEnum.MANUAL_COMPLETION.getCode()), OmsPlatformEnum.OMS_GOOD_CANG.getCode());
        List<GoodCangReceiptBatchResp> allResult = new ArrayList<>();
        
        if(CollUtil.isNotEmpty(receiveCodeList)) {
        	String typeId = dmpCfgInputEntity.getTypeId();
            DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
            String apiType = dmpCfgApiEntity.getApiType();
            
            List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(DmpBasicSystemCodeEnum.GOODCANG.getCode()));
            if(CollUtil.isEmpty(overseasProviderEntityList)) {
            	throw new ServiceException("谷仓授权信息不存在");
            }
            ThirdWarehouseContext.setAuthMap(overseasProviderEntityList.get(0).getAuthJson());
            
            for(String receiveCode : receiveCodeList) {
            	Map<String,Object> paramsMap = new HashMap<>();
                paramsMap.put("receiving_code",receiveCode);
            	String response = GoodCangUtils.sendPost(apiType,paramsMap);
            	GoodCangResponse<GoodCangReceiptBatchResp> respDto = JSONObject.parseObject(response,new TypeReference<GoodCangResponse<GoodCangReceiptBatchResp>>() {}.getType());
            	String ask = respDto.getAsk();
            	if(ask.equals("Failure") && respDto.getMessage().contains("入库单号不存在")) {
            		continue;
            	}
            	allResult.add(respDto.getData());
            }
        }
		
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(allResult));
		return Collections.singletonList(dmpInputTaskInitDTO);
	}

	
	
}
