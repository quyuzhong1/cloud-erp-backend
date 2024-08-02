package com.erp.server.dmp.inout.handler.input.task.init;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.sdk.wms.goodcang.dto.request.GoodCangGetSkuReq;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.dto.response.GoodCangSkuResp;
import com.sdk.wms.goodcang.utils.GoodCangUtils;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputGoodCangProductInitHandler extends DmpInputInitHandler{

	@Resource
    private AliExpressOrderService aliExpressOrderService;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();
        
        GoodCangGetSkuReq goodCangGetSkuReq = new GoodCangGetSkuReq();
        Integer page = 1;
        goodCangGetSkuReq.setPageSize(100);
        int currTotal = 0;
        List<GoodCangSkuResp> allResult = new ArrayList<>();
        ThirdWarehouseContext.setAuthMap(null);
        while(true) {
        	goodCangGetSkuReq.setPage(page);
        	String response = GoodCangUtils.sendPost(apiType,JSON.toJSONString(goodCangGetSkuReq));
        	GoodCangResponse<List<GoodCangSkuResp>> result = JSONObject.parseObject(response,new TypeReference<GoodCangResponse<List<GoodCangSkuResp>>>() {}.getType());
        	List<GoodCangSkuResp> data = result.getData();
        	int size = data.size();
        	if(size == 0) {
        		break;
        	}
        	allResult.addAll(data);
			currTotal = currTotal + size;
        	Integer count = result.getCount();
        	if(currTotal >= count) {
        		break;
        	}
        	page = page + 1;
        }
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(allResult));
		return Collections.singletonList(dmpInputTaskInitDTO);
	}

	
	
}
