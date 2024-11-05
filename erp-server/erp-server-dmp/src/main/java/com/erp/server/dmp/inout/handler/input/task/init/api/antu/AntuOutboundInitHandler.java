package com.erp.server.dmp.inout.handler.input.task.init.api.antu;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.sdk.wms.antu.dto.request.AntuGetOutboundReq;
import com.sdk.wms.antu.dto.response.AntuResponse;
import com.sdk.wms.antu.enums.AntuEnums;
import com.sdk.wms.antu.utils.AntuUtils;
import com.sdk.wms.goodcang.dto.request.GoodCangGetOutBoundReq;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.utils.GoodCangUtils;
import com.sdk.wms.iml.dto.request.ImlGetOutboundReq;
import com.sdk.wms.iml.dto.response.ImlResponse;
import com.sdk.wms.iml.enums.ImlEnums;
import com.sdk.wms.iml.utils.ImlUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * dmp输入init任务基础处理器下的安兔api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class AntuOutboundInitHandler extends DmpInputInitHandler {

	@Resource
    private DmpHandlerCache dmpHandlerCache;

	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		String typeId = dmpCfgInputEntity.getTypeId();
		DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
		String apiType = dmpCfgApiEntity.getApiType();

		//查询数据
		AntuGetOutboundReq antuGetOutboundReq = AntuGetOutboundReq.builder()
				.modifyDateFrom(dmpInputTaskEntity.getStartTime())
				.modifyDateTo(dmpInputTaskEntity.getEndTime())
				.pageSize(100)
//				.orderStatus(AntuEnums.OrderStatusEnum.INITIAL_RECEIVING.getCode())
				.build();
		Integer page = 1;
		int currTotal = 0;
		List<Object> allResult = new ArrayList<>();
		List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(DmpBasicSystemCodeEnum.ANTU.getCode()));
		if(CollUtil.isEmpty(overseasProviderEntityList)) {
			return Collections.emptyList();
		}
		if (overseasProviderEntityList.get(0).getEnableDate().compareTo(LocalDate.now()) > 0) {
			return Collections.emptyList();
		}

		ThirdWarehouseContext.setAuthMap(overseasProviderEntityList.get(0).getAuthJson());
		while(true) {
			antuGetOutboundReq.setPage(page);
			String response = AntuUtils.callService(apiType,antuGetOutboundReq);
			AntuResponse<List<?>> result = JSONObject.parseObject(response,new TypeReference<AntuResponse<List<Object>>>() {}.getType());
			List<?> data = result.getData();
			int size = data.size();
			if(size == 0) {
				break;
			}
			allResult.addAll(data);
			currTotal = currTotal + size;
			Integer count = result.getCount();
			if(count == null) {
				count  = 0;
			}
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
