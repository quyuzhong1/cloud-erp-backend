package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.model.dmp.entity.DmpSoRefundInfoEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpSoReturnInfoService;
import com.erp.server.dmp.service.DmpSoRefundInfoService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOrderIssueDetailDmpHandler extends DmpInputAliExpressOrderDoChildDmpHandler{
    private static final String STORAGE_RETURN_INFO = "dmp_so_return_info";
    private static final String STORAGE_RETURN_DETAIL = "dmp_so_return_detail";
    private static final String STORAGE_REFUND_INFO = "dmp_so_refund_info";
    private static final String STORAGE_REFUND_DETAIL = "dmp_so_refund_detail";
	
	@Resource
	private DmpSoReturnInfoService dmpSoReturnInfoService;
    @Resource
    private DmpSoRefundInfoService dmpSoRefundInfoService;
	
	@Override
	protected List<Map<String, Object>> afterDoDmpInputMongoChildEntityList(
			List<Map<String, Object>> dmpInputMongoChildList) {
        String storageName = dmpCfgInputConvertEntity.getStorageName();
        boolean isReturnStorage = STORAGE_RETURN_INFO.equals(storageName) || STORAGE_RETURN_DETAIL.equals(storageName);
        boolean isRefundStorage = STORAGE_REFUND_INFO.equals(storageName) || STORAGE_REFUND_DETAIL.equals(storageName);
		List<Map<String, Object>> resultDmpInputMongoChildList = new ArrayList<>();
		if(CollUtil.isNotEmpty(dmpInputMongoChildList)) {
			for(Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
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

                AliExpressIssueSolutionResolver.ResolvedIssueSolution resolvedIssueSolution = AliExpressIssueSolutionResolver.resolve(dmpInputMongoChild);
                if (isReturnStorage && !resolvedIssueSolution.isMatchedReturn()) {
                    continue;
                }
                if (isRefundStorage && !resolvedIssueSolution.isMatchedRefund()) {
                    continue;
                }

                AliExpressIssueSolutionResolver.SolutionRecord effectiveSolution = resolvedIssueSolution.getEffectiveSolution();
                if (effectiveSolution == null) {
                    continue;
                }

				Map<String, Object> resultDmpInputMongoChild = new HashMap<>();
                String issueId = ObjectUtil.defaultIfNull(dmpInputMongoChild.get("id"), "").toString();
                String thirdCode = issueId;
                String issueStatus = ObjectUtil.defaultIfNull(dmpInputMongoChild.get("issue_status"), "").toString();
                String orderId = ObjectUtil.defaultIfNull(dmpInputMongoChild.get("order_id"), "").toString();
                String parentOrderId = ObjectUtil.defaultIfNull(dmpInputMongoChild.get("parent_order_id"), "").toString();
                String productId = ObjectUtil.defaultIfNull(dmpInputMongoChild.get("product_id"), "").toString();
                String platformOrderCode = StringUtils.defaultIfBlank(parentOrderId, orderId);
                String reverseDetailStatus = resolvedIssueSolution.getReverseDetailStatus();
                String trackingNumber = AliExpressIssueSolutionResolver.getReturnTrackingNo(dmpInputMongoChild);
                String reasonChinese = StringUtils.defaultIfBlank(
                        ObjectUtil.defaultIfNull(dmpInputMongoChild.get("reason_chinese"), "").toString(),
                        AliExpressIssueSolutionResolver.getIssueContent(dmpInputMongoChild));
                String reasonEnglish = ObjectUtil.defaultIfNull(dmpInputMongoChild.get("reason_english"), "").toString();
                String issueReason = AliExpressIssueSolutionResolver.pickIssueTextForVarchar(reasonChinese, reasonEnglish);
                String issueReasonName = AliExpressIssueSolutionResolver.pickIssueTextForVarchar(reasonEnglish, reasonChinese);

				resultDmpInputMongoChild.put("buyer_login_id" , dmpInputMongoChild.get("buyer_login_id"));
                resultDmpInputMongoChild.put("sourcePlatform", "AliExpress");
				resultDmpInputMongoChild.put("issue_id" , issueId);
				resultDmpInputMongoChild.put("skuId" , productId);
				resultDmpInputMongoChild.put("skuNo" , productId);
                resultDmpInputMongoChild.put("skuName", AliExpressIssueSolutionResolver.trimForDb(dmpInputMongoChild.get("product_name")));
				resultDmpInputMongoChild.put("detailStatus" , issueStatus);
				resultDmpInputMongoChild.put("returnOriginalType" , reverseDetailStatus);
				resultDmpInputMongoChild.put("returnLogisticsCompany" , dmpInputMongoChild.get("buyer_return_logistics_company"));
				resultDmpInputMongoChild.put("returnLogisticsNo" , trackingNumber);
				resultDmpInputMongoChild.put("platformDetailId" , thirdCode);
				resultDmpInputMongoChild.put("thirdOrderCode" , parentOrderId);
				resultDmpInputMongoChild.put("thirdDetailId" , effectiveSolution.get("id"));
				resultDmpInputMongoChild.put("logisticsFeeAmount" , effectiveSolution.get("logistics_fee_amount"));
				resultDmpInputMongoChild.put("logisticsFeeCurrency" , effectiveSolution.get("logistics_fee_amount_currency"));
				resultDmpInputMongoChild.put("logisticsFeeRole" , effectiveSolution.get("logistics_fee_bear_role"));
				resultDmpInputMongoChild.put("platformOrderCode" , platformOrderCode);
				resultDmpInputMongoChild.put("amount" , effectiveSolution.get("refund_money"));
				resultDmpInputMongoChild.put("currency" , effectiveSolution.get("refund_money_currency"));
                resultDmpInputMongoChild.put("currencyCode", effectiveSolution.get("refund_money_currency"));
				resultDmpInputMongoChild.put("sellPrice" , effectiveSolution.get("refund_money_post"));
				resultDmpInputMongoChild.put("solutionType" , effectiveSolution.getSolutionType());
				resultDmpInputMongoChild.put("receiveGoods" , receiveGoods);
                resultDmpInputMongoChild.put("qty", 1);
                resultDmpInputMongoChild.put("thirdCode", thirdCode);
                resultDmpInputMongoChild.put("platformCode", StringUtils.defaultIfBlank(orderId, platformOrderCode));
                resultDmpInputMongoChild.put("platformStatus", reverseDetailStatus);
                resultDmpInputMongoChild.put("platformOriginalStatus", reverseDetailStatus);
                resultDmpInputMongoChild.put("remark", issueReason);
                resultDmpInputMongoChild.put("reason", issueReason);
                resultDmpInputMongoChild.put("remarkName", issueReasonName);
                resultDmpInputMongoChild.put("trackingNumber", AliExpressIssueSolutionResolver.trimForDb(trackingNumber));
                resultDmpInputMongoChild.put("logisticsSupplierCode", AliExpressIssueSolutionResolver.trimForDb(dmpInputMongoChild.get("buyer_return_logistics_company")));
                resultDmpInputMongoChild.put("logisticsSupplierName", AliExpressIssueSolutionResolver.trimForDb(dmpInputMongoChild.get("buyer_return_logistics_company")));
                resultDmpInputMongoChild.put("platformCreateTime", dmpInputMongoChild.get("gmt_create"));
                resultDmpInputMongoChild.put("platformUpdateTime", AliExpressIssueSolutionResolver.getLatestEventTime(dmpInputMongoChild));
                resultDmpInputMongoChild.put("returnTime", dmpInputMongoChild.get("gmt_create"));
                resultDmpInputMongoChild.put("refundTime", dmpInputMongoChild.get("gmt_create"));
                if (isRefundStorage) {
                    resultDmpInputMongoChild.put("status", AliExpressIssueSolutionResolver.resolveRefundStatus(reverseDetailStatus, issueStatus));
                } else {
                    resultDmpInputMongoChild.put("status", "finish".equals(issueStatus) ? "4" : "1");
                }
				resultDmpInputMongoChild.put(DmpInputMongoHandler.MONGO_BASE_ID, dmpInputMongoChild.get(DmpInputMongoHandler.MONGO_BASE_ID));
				resultDmpInputMongoChild.put(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, dmpInputMongoChild.get(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID));
				resultDmpInputMongoChildList.add(resultDmpInputMongoChild);
			}
		}
		return resultDmpInputMongoChildList;
	}
	
	@Override
	protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList) {
        String storageName = dmpCfgInputConvertEntity.getStorageName();
        if (STORAGE_RETURN_INFO.equals(storageName) || STORAGE_REFUND_INFO.equals(storageName)) {
            for (Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
                dmpInputMongoChildEntity.put(MAIN_ID, dmpInputMongoChildEntity.get(DmpInputMongoHandler.MONGO_BASE_ID));
            }
            return;
        }

        Map<String, String> billNoIdMap = new HashMap<>();
        if (STORAGE_RETURN_DETAIL.equals(storageName)) {
            QueryWrapper<DmpSoReturnInfoEntity> wrapper = new QueryWrapper<>();
            wrapper.eq(INPUT_TASK_ID, inputTaskId);
            List<Map<String, Object>> listMaps = dmpSoReturnInfoService.listMaps(wrapper);
            if(CollUtil.isNotEmpty(listMaps)) {
                for(Map<String, Object> listMap : listMaps) {
                    billNoIdMap.put(listMap.get("buyer_user_id").toString() + "_" + listMap.get("third_code").toString(), listMap.get(BaseEntity.FIELD_ID).toString());
                }
            }
        } else if (STORAGE_REFUND_DETAIL.equals(storageName)) {
            QueryWrapper<DmpSoRefundInfoEntity> wrapper = new QueryWrapper<>();
            wrapper.eq(INPUT_TASK_ID, inputTaskId);
            List<Map<String, Object>> listMaps = dmpSoRefundInfoService.listMaps(wrapper);
            if (CollUtil.isNotEmpty(listMaps)) {
                for (Map<String, Object> listMap : listMaps) {
                    billNoIdMap.put(listMap.get("buyer_user_id").toString() + "_" + listMap.get("third_code").toString(),
                            listMap.get(BaseEntity.FIELD_ID).toString());
                }
            }
        }

        for(Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
            String billNo = dmpInputMongoChildEntity.get("buyer_login_id").toString() + "_"
                    + dmpInputMongoChildEntity.get("platformDetailId").toString();
            String dmpId = billNoIdMap.get(billNo);
            dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
        }
	}
}
