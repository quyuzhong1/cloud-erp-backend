package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.sdk.oms.mercado.constant.MercadoConstant;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 获取马帮订单详情数据
 */
@Service
@Scope("prototype")
public class MercadoReturnGetDetailDmpHandler extends DmpInputDoNextDmpHandler{

	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
		List<ParamData> orderParamList = new ArrayList<>();
		orderParamList.add(new ParamData(MercadoConstant.MONGO_BASE_FID, MercadoConstant.MONGO_BASE_FID, PannoEnum.EQ, dmpInputMongoEntity.get("resourceId")));
		List<Map<String, Object>> orderDetailMongoList = mongoService.findMongoData(orderParamList, "mercadolibre_orderDetail_data");
		return (List<Map<String, Object>>) orderDetailMongoList.get(0).get("orderItems");
	}

}
