package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.wms.aliexpress.model.inbound.AliexpressInboundConfirmDTO;
import io.seata.common.util.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class CaiNiaoInboundInitHandler extends DmpInputInitHandler{

	@Resource
    private DmpHandlerCache dmpHandlerCache;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		String data = ThirdWarehouseContext.getData();
		if(StringUtils.isBlank(data)){
			return new ArrayList<>();
		}
		AliexpressInboundConfirmDTO aliexpressOrderConfirmDTO = JSONUtil.toBean(data, AliexpressInboundConfirmDTO.class);
		if(Objects.isNull(aliexpressOrderConfirmDTO) || Objects.isNull(aliexpressOrderConfirmDTO.getEntryOrder())){
			return new ArrayList<>();
		}
		aliexpressOrderConfirmDTO.getEntryOrder().setOrderLines(aliexpressOrderConfirmDTO.getOrderLines());
		aliexpressOrderConfirmDTO.getEntryOrder().setPackages(aliexpressOrderConfirmDTO.getTotalOrders());
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(aliexpressOrderConfirmDTO.getEntryOrder()));
		return Collections.singletonList(dmpInputTaskInitDTO);
	}
}
