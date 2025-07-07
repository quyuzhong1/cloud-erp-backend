package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.wms.aliexpress.model.order.AliexpressOrderConfirmDTO;
import com.sdk.wms.goodcang.dto.request.GoodCangGetOutBoundReq;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.utils.GoodCangUtils;
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
public class DmpInputCaiNiaoOutboundInitHandler extends DmpInputInitHandler{

	@Resource
    private DmpHandlerCache dmpHandlerCache;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		String data = ThirdWarehouseContext.getData();
		if(StringUtils.isBlank(data)){
			return new ArrayList<>();
		}
		AliexpressOrderConfirmDTO aliexpressOrderConfirmDTO = JSONUtil.toBean(data, AliexpressOrderConfirmDTO.class);
		if(Objects.isNull(aliexpressOrderConfirmDTO) || Objects.isNull(aliexpressOrderConfirmDTO.getDeliveryOrder())){
			return new ArrayList<>();
		}
		aliexpressOrderConfirmDTO.getDeliveryOrder().setOrderLines(aliexpressOrderConfirmDTO.getOrderLines());
		aliexpressOrderConfirmDTO.getDeliveryOrder().setPackages(aliexpressOrderConfirmDTO.getPackages());
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(aliexpressOrderConfirmDTO.getDeliveryOrder()));
		return Collections.singletonList(dmpInputTaskInitDTO);
	}
}
