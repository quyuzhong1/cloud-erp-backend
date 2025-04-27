package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpSoInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressBillOrderDmpHandler extends DmpInputDoNextDmpHandler{

	@Resource
	private DmpSoInfoService dmpSoInfoService;

	@Override
	protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(
			List<Map<String, Object>> dmpInputMongoEntityList) {
		Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps = new HashMap<>();
		if(CollUtil.isNotEmpty(dmpInputMongoEntityList)) {
			List<ParamData> paramDataList = new ArrayList<>();
			Map<String, Map<String, Object>> orderAddressMaps = new HashMap<>();
			List<String> orderIdList = dmpInputMongoEntityList.stream().map(d -> d.get("order_id").toString()).collect(Collectors.toList());

			paramDataList.add(new ParamData("order_id", "order_id", PannoEnum.IN, orderIdList));
			paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, PannoEnum.EQ, nextLevelId));
			List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, "aliexpress_orderDetail_data");
			List<Map<String, Object>> orderAddressData = mongoService.findMongoData(paramDataList, "aliexpress_orderAddress_data");

			if(CollUtil.isNotEmpty(orderAddressData)) {
				orderAddressMaps = orderAddressData.stream().collect(Collectors.toMap(f -> f.get("order_id").toString(), f -> f));
			}
			if(CollUtil.isNotEmpty(findMongoData)) {
				Map<String, Map<String, Object>> orderIdDetailMaps = findMongoData.stream().collect(Collectors.toMap(f -> f.get("order_id").toString(), f -> f));
				Map<String, DmpSoInfoEntity> maidIdThirdCodeMaps = dmpSoInfoService.lambdaQuery()
						.in(DmpSoInfoEntity::getThirdCode, orderIdDetailMaps.keySet())
						.eq(DmpSoInfoEntity::getInputTaskId, inputTaskId)
						.list().stream().collect(Collectors.toMap(DmpSoInfoEntity::getPlatformCode, Function.identity(),(v1,v2)->v1));

				for(Map.Entry<String, Map<String, Object>> orderIdDetailMap : orderIdDetailMaps.entrySet()) {
					String ordreId = orderIdDetailMap.getKey();
					DmpSoInfoEntity dmpSoInfoEntity = maidIdThirdCodeMaps.get(ordreId);
					Map<String, Object> orderDetails = orderIdDetailMap.getValue();
					Object receipt_address_obj = orderDetails.get("receipt_address");
					Map<String, Object> orderAddressMap = orderAddressMaps.get(ordreId);
					if(receipt_address_obj != null && Objects.nonNull(dmpSoInfoEntity)) {
						Map<String , Object> receipt_address = (Map)receipt_address_obj;
						TreeMap<String, Object> dmpInputDmpBaseEntity = new TreeMap<>();
						dmpInputDmpBaseEntity.put("nextLevelId", nextLevelId);
						dmpInputDmpBaseEntity.put("sourcePlatform", dmpSoInfoEntity.getSourcePlatform());
						dmpInputDmpBaseEntity.put("sourceSystem", dmpSoInfoEntity.getSourceSystem());
						dmpInputDmpBaseEntity.put("thirdCode", dmpSoInfoEntity.getThirdCode());
						dmpInputDmpBaseEntity.put("platformCode", dmpSoInfoEntity.getPlatformCode());
						dmpInputDmpBaseEntity.put("thirdDetailId", dmpSoInfoEntity.getPlatformCode());
						dmpInputDmpBaseEntity.put("platformDetailId", dmpSoInfoEntity.getPlatformCode());
						dmpInputDmpBaseEntity.put("shopId", dmpSoInfoEntity.getShopId());
						dmpInputDmpBaseEntity.put("postalCode", receipt_address.get("zip"));
						dmpInputDmpBaseEntity.put("city", receipt_address.get("city"));
						dmpInputDmpBaseEntity.put("country", receipt_address.get("country"));
						dmpInputDmpBaseEntity.put("taxNo", receipt_address.get("cpf_no"));
						dmpInputDmpBaseEntity.put("buyerEmail", orderAddressMap.get("contact_email"));
						dmpInputDmpBaseEntity.put("buyerPhoneNumber", orderAddressMap.get("mobile_no"));
						dmpInputDmpBaseEntity.put("buyerName", orderAddressMap.get("buyer_signer_fullname"));
						dmpInputDmpBaseEntity.put("address1", orderAddressMap.get("detail_address"));
						dmpInputDmpBaseEntity.put("state", receipt_address.get("province"));
						dmpInputDataDmpRelationMaps.put(Collections.singletonList(orderDetails), Collections.singletonList(dmpInputDmpBaseEntity));
					}
				}
			}
		}
		return dmpInputDataDmpRelationMaps;
	}
	
	
}
