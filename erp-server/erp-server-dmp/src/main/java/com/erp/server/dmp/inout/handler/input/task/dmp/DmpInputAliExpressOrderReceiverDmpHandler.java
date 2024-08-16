package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOrderReceiverDmpHandler extends DmpInputAliExpressOrderDoChildDmpHandler{

	@Override
	protected List<Map<String, Object>> afterDoDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoChildList){
		if(CollUtil.isNotEmpty(dmpInputMongoChildList)) {
			Map<String, Map<String, Object>> orderIdDetailMaps = new HashMap<>();
			Map<String, Map<String, Object>> orderIdMaps = new HashMap<>();
			List<ParamData> paramDataList = new ArrayList<>();
			List<String> orderIdList = dmpInputMongoChildList.stream().map(d -> d.get("order_id").toString()).collect(Collectors.toList());

			paramDataList.add(new ParamData("order_id", "order_id", PannoEnum.IN, orderIdList));
			paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, PannoEnum.EQ, nextLevelId));
			List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, "aliexpress_orderDetail_data");
			if(CollUtil.isNotEmpty(findMongoData)) {
				orderIdDetailMaps = findMongoData.stream().collect(Collectors.toMap(f -> f.get("order_id").toString(), f -> f));
			}
			findMongoData = mongoService.findMongoData(paramDataList, "aliexpress_order_data");
			if(CollUtil.isNotEmpty(findMongoData)) {
				orderIdMaps = findMongoData.stream().collect(Collectors.toMap(f -> f.get("order_id").toString(), f -> f));
			}
			
			for(Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
				Object order_id = dmpInputMongoChild.get("order_id");
				Map<String, Object> orderDetail = orderIdDetailMaps.get(order_id);
				Map<String, Object> order = orderIdMaps.get(order_id);
				Object receipt_address_obj = orderDetail.get("receipt_address");
				dmpInputMongoChild.put("oaId", orderDetail.get("oaid"));
				if(receipt_address_obj != null) {
					Map<String , Object> receipt_address = (Map)receipt_address_obj;
					dmpInputMongoChild.put("country", receipt_address.get("country"));
					dmpInputMongoChild.put("postCode", receipt_address.get("zip"));
					dmpInputMongoChild.put("province", receipt_address.get("province"));
					dmpInputMongoChild.put("city", receipt_address.get("city"));
					dmpInputMongoChild.put("mainStreet", receipt_address.get("address"));
					dmpInputMongoChild.put("secondStreet", receipt_address.get("address2"));
					dmpInputMongoChild.put("mainPhone", receipt_address.get("mobile_no"));
					dmpInputMongoChild.put("district", receipt_address.get("phone_country"));
					dmpInputMongoChild.put("receiverTaxNo", receipt_address.get("cpf_no"));
				}
				dmpInputMongoChild.put("buyerId", order.get("buyer_login_id"));
				dmpInputMongoChild.put("buyerName", order.get("buyer_signer_fullname"));
				dmpInputMongoChild.put("receiverName", dmpInputMongoChild.get("contact_person"));
				dmpInputMongoChild.put("receiverTelNumber", dmpInputMongoChild.get("phone_number"));
				dmpInputMongoChild.put("fullAddress", dmpInputMongoChild.get("detail_address"));
				dmpInputMongoChild.put("mainStreet", dmpInputMongoChild.get("address"));
				dmpInputMongoChild.put("secondStreet", dmpInputMongoChild.get("address2"));
				dmpInputMongoChild.put("mainPhone", dmpInputMongoChild.get("mobile_no"));
			}
		}
		
		return dmpInputMongoChildList;
	}
	
	
}
