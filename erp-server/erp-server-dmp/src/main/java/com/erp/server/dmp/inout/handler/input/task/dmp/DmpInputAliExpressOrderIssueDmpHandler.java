package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.server.dmp.service.DmpSoInfoService;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOrderIssueDmpHandler extends DmpInputDbConvertDmpHandler{
	@Autowired
	private DmpSoInfoService dmpSoInfoService;
	
	public static final String ALIEXPRESS_ISSUEDETAIL_DATA = "aliexpress_issueDetail_data";
	
	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		if(!dmpInputDataDmpRelationMaps.isEmpty()) {
			Collection<List<TreeMap<String, Object>>> values = dmpInputDataDmpRelationMaps.values();
			if(CollUtil.isNotEmpty(values)) {
				List<String> orders = new ArrayList<>();
				List<Long> issueIds = new ArrayList<>();
				for(List<TreeMap<String, Object>> v : values) {
					orders.addAll(v.stream().map(a -> a.get("parent_order_id").toString()).collect(Collectors.toList()));
					issueIds.addAll(v.stream().filter(a -> a.get("thirdCode") != null).map(a -> Long.valueOf(a.get("thirdCode").toString())).collect(Collectors.toList()));
				}
				Map<String, String> orderIdMaps = new HashMap<>();
				if(CollUtil.isNotEmpty(orders)) {
					List<DmpSoInfoEntity> list = dmpSoInfoService.lambdaQuery()
						.in(DmpSoInfoEntity::getThirdCode, orders)
						.eq(DmpSoInfoEntity::getSourcePlatform, DmpBasicSystemCodeEnum.ALI_EXPRESS.getCode())
						.eq(DmpSoInfoEntity::getNextLevelId, nextLevelId)
						.list();
					if(CollUtil.isEmpty(list)) {
						throw new ServiceException("所有退货订单未查询到订单数据");
					}
					orderIdMaps = list.stream().collect(Collectors.toMap(DmpSoInfoEntity::getThirdCode, DmpSoInfoEntity::getId , (v1 , v2) -> v1));
				}
				
				Map<Long, Object> issueIdOrderMap = new HashMap<>();
				if(CollUtil.isNotEmpty(issueIds)) {
//					List<ParamData> paramDataList = new ArrayList<>();
//					paramDataList.add(new ParamData("id", "id", PannoEnum.IN, issueIds));
//					paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, PannoEnum.EQ, nextLevelId));
//					issueIdOrderMap = mongoService.findMongoData(paramDataList, ALIEXPRESS_ISSUEDETAIL_DATA).stream().collect(Collectors.toMap(a -> Long.valueOf(a.get("id").toString()), a -> a.get("buyer_return_no")));
				}
				
				for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
					List<TreeMap<String, Object>> value = dmpInputDataDmpRelationMap.getValue();
					for(TreeMap<String, Object> v : value) {
						String parent_order_id = v.get("parent_order_id").toString();
						String sourceId = orderIdMaps.get(parent_order_id);
						if(StringUtils.isBlank(sourceId)) {
							throw new ServiceException("退货订单"+ parent_order_id +"未查询到订单数据");
						}
						v.put("sourceId", sourceId);
						Object issue_status = v.get("status");
						if(issue_status != null) {
							if("finish".equals(issue_status.toString())) {
								v.put("status", "4");
							}else {
								v.put("status", "1");
							}
						}
						
						Long issueId = Long.valueOf(v.get("thirdCode").toString());
						Object buyer_return_no = issueIdOrderMap.get(issueId);
						if(buyer_return_no != null && StringUtils.isNotBlank(buyer_return_no.toString())) {
//							v.put("platformCode", buyer_return_no);
						}
					}
				}
			}
		}
	}
	
}
