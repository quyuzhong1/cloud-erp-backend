package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpSoReturnInfoService;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOrderIssueDetailDmpHandler extends DmpInputAliExpressOrderDoChildDmpHandler{
	
	@Resource
	private DmpSoReturnInfoService dmpSoReturnInfoService;
	
	@Override
	protected List<Map<String, Object>> afterDoDmpInputMongoChildEntityList(
			List<Map<String, Object>> dmpInputMongoChildList) {
		List<Map<String, Object>> resultDmpInputMongoChildList = new ArrayList<>();
		if(CollUtil.isNotEmpty(dmpInputMongoChildList)) {
			for(Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
				Object platform_solution_list_obj = dmpInputMongoChild.get("platform_solution_list");
				if(platform_solution_list_obj != null) {
					Boolean receiveGoods = false;
					Object process_dto_list_obj = dmpInputMongoChild.get("process_dto_list");
					if(process_dto_list_obj != null) {
						Map<String, Object> process_dto_list = (Map<String, Object>) process_dto_list_obj;
						Object api_issue_process_dto_obj = process_dto_list.get("api_issue_process_dto");
						if(api_issue_process_dto_obj != null) {
							List<Map<String, Object>> api_issue_process_dto = (List<Map<String, Object>>) api_issue_process_dto_obj;
							receiveGoods = api_issue_process_dto.stream().anyMatch(a -> Boolean.TRUE.equals(a.get("receive_goods")));
						}
					}
					
					Map<String, Object> platform_solution_list = (Map<String, Object>) platform_solution_list_obj;
					Object solution_api_dto_obj = platform_solution_list.get("solution_api_dto");
					if(solution_api_dto_obj != null) {
						List<Map<String, Object>> solution_api_dto_list = (List<Map<String, Object>>) solution_api_dto_obj;
						for(Map<String, Object> solution_api_dto : solution_api_dto_list) {
							Map<String, Object> resultDmpInputMongoChild = new HashMap<>();
							
							resultDmpInputMongoChild.put("buyer_login_id" , dmpInputMongoChild.get("buyer_login_id"));
							resultDmpInputMongoChild.put("issue_id" , dmpInputMongoChild.get("id"));
							resultDmpInputMongoChild.put("skuId" , dmpInputMongoChild.get("product_id"));
							resultDmpInputMongoChild.put("detailStatus" , dmpInputMongoChild.get("issue_status"));
							resultDmpInputMongoChild.put("returnLogisticsCompany" , dmpInputMongoChild.get("buyer_return_logistics_company"));
							resultDmpInputMongoChild.put("returnLogisticsNo" , dmpInputMongoChild.get("buyer_return_logistics_lp_no"));
							resultDmpInputMongoChild.put("platformDetailId" , dmpInputMongoChild.get("buyer_return_no"));
							resultDmpInputMongoChild.put("thirdOrderCode" , dmpInputMongoChild.get("parent_order_id"));
							resultDmpInputMongoChild.put("thirdDetailId" , solution_api_dto.get("id"));
							resultDmpInputMongoChild.put("logisticsFeeAmount" , solution_api_dto.get("logistics_fee_amount"));
							resultDmpInputMongoChild.put("logisticsFeeCurrency" , solution_api_dto.get("logistics_fee_amount_currency"));
							resultDmpInputMongoChild.put("logisticsFeeRole" , solution_api_dto.get("logistics_fee_bear_role"));
							resultDmpInputMongoChild.put("platformOrderCode" , solution_api_dto.get("order_id"));
							resultDmpInputMongoChild.put("amount" , solution_api_dto.get("refund_money"));
							resultDmpInputMongoChild.put("currency" , solution_api_dto.get("refund_money_currency"));
							resultDmpInputMongoChild.put("sellPrice" , solution_api_dto.get("refund_money_post"));
							resultDmpInputMongoChild.put("solutionType" , solution_api_dto.get("solution_type"));
							resultDmpInputMongoChild.put("isGift" , receiveGoods);
							
							
							
							resultDmpInputMongoChild.put(DmpInputMongoHandler.MONGO_BASE_ID, dmpInputMongoChild.get(DmpInputMongoHandler.MONGO_BASE_ID));
							resultDmpInputMongoChild.put(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, dmpInputMongoChild.get(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID));
							resultDmpInputMongoChildList.add(resultDmpInputMongoChild);
						}
					}
				}
			}
		}
		return resultDmpInputMongoChildList;
	}
	
	@Override
	protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList) {
		QueryWrapper<DmpSoReturnInfoEntity> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		List<Map<String, Object>> listMaps = dmpSoReturnInfoService.listMaps(wrapper);
		Map<String, String> billNoIdMap = new HashMap<>();
		if(CollUtil.isNotEmpty(listMaps)) {
			for(Map<String, Object> listMap : listMaps) {
				billNoIdMap.put(listMap.get("buyer_user_id").toString() + "_" + listMap.get("third_code").toString(), listMap.get(BaseEntity.ID).toString());
			}
		}
		for(Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
			String billNo = dmpInputMongoChildEntity.get("buyer_login_id").toString() + "_" + dmpInputMongoChildEntity.get("issue_id").toString();
			String dmpId = billNoIdMap.get(billNo);
			dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
		}
	}
}
