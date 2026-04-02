package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpSoInfoService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
@Slf4j
public class DmpInputAliExpressOrderIssueDmpHandler extends DmpInputDbConvertDmpHandler{
	@Autowired
	private DmpSoInfoService dmpSoInfoService;
    @Autowired
    private AliExpressOrderService aliExpressOrderService;

	public static final String ALIEXPRESS_ISSUEDETAIL_DATA = "aliexpress_issueDetail_data";
    private static final String STORAGE_RETURN_INFO = "dmp_so_return_info";
    private static final String STORAGE_REFUND_INFO = "dmp_so_refund_info";
	
	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		if(!dmpInputDataDmpRelationMaps.isEmpty()) {
			Collection<List<TreeMap<String, Object>>> values = dmpInputDataDmpRelationMaps.values();
			if(CollUtil.isNotEmpty(values)) {
				List<String> orders = new ArrayList<>();
				List<Long> issueIds = new ArrayList<>();
				for(List<TreeMap<String, Object>> v : values) {
					orders.addAll(v.stream().map(a -> a.get("parent_order_id").toString()).collect(Collectors.toList()));
                    issueIds.addAll(v.stream()
                            .map(this::getIssueIdFromConvertedData)
                            .filter(StringUtils::isNotBlank)
                            .map(Long::valueOf)
                            .collect(Collectors.toList()));
				}
				Map<String, String> orderIdMaps = new HashMap<>();
				if(CollUtil.isNotEmpty(orders)) {
					List<DmpSoInfoEntity> list = dmpSoInfoService.lambdaQuery()
						.in(DmpSoInfoEntity::getThirdCode, orders)
						.eq(DmpSoInfoEntity::getSourcePlatform, DmpBasicSystemCodeEnum.ALI_EXPRESS.getCode())
						.eq(DmpSoInfoEntity::getNextLevelId, nextLevelId)
						.list();
                    if (CollUtil.isEmpty(list)) {
                        log.warn("AliExpress纠纷DMP未查询到任何来源订单，全部跳过。nextLevelId={}, inputTaskId={}, storageName={}, orderCount={}",
                                nextLevelId, inputTaskId, dmpCfgInputConvertEntity.getStorageName(), orders.size());
                    }
					orderIdMaps = list.stream().collect(Collectors.toMap(DmpSoInfoEntity::getThirdCode, DmpSoInfoEntity::getId , (v1 , v2) -> v1));
				}
				
				Map<String, IssueDetailSnapshot> issueDetailMap = new HashMap<>();
				if(CollUtil.isNotEmpty(issueIds)) {
                    List<ParamData> paramDataList = new ArrayList<>();
                    paramDataList.add(new ParamData("id", "id", PannoEnum.IN, issueIds));
                    paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, PannoEnum.EQ, nextLevelId));
                    List<Map<String, Object>> issueDetailList = mongoService.findMongoData(paramDataList, ALIEXPRESS_ISSUEDETAIL_DATA);
                    if (CollUtil.isNotEmpty(issueDetailList)) {
                        for (Map<String, Object> issueDetail : issueDetailList) {
                            String issueId = getIssueId(issueDetail);
                            if (StringUtils.isBlank(issueId)) {
                                continue;
                            }
                            issueDetailMap.put(issueId, buildIssueDetailSnapshot(issueDetail));
                        }
                    }
				}

                boolean isReturnInfo = STORAGE_RETURN_INFO.equals(dmpCfgInputConvertEntity.getStorageName());
                boolean isRefundInfo = STORAGE_REFUND_INFO.equals(dmpCfgInputConvertEntity.getStorageName());
                String shopName = "";
                try {
                    AliExpressShopInfoDTO shopInfoDTO = aliExpressOrderService.getShopInfoByShopId(nextLevelId);
                    if (shopInfoDTO != null) {
                        shopName = ObjectUtil.defaultIfNull(shopInfoDTO.getName(), "").toString();
                    }
                } catch (Exception e) {
                }
                Iterator<Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>>> iterator = dmpInputDataDmpRelationMaps.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry = iterator.next();
                    List<TreeMap<String, Object>> value = entry.getValue();
                    Map<String, String> finalOrderIdMaps = orderIdMaps;
                    String finalShopName = shopName;
                    value.removeIf(v -> {
                        String issueId = getIssueIdFromConvertedData(v);
                        IssueDetailSnapshot snapshot = issueDetailMap.get(issueId);
                        if (snapshot == null || !matchStorageCondition(snapshot, isReturnInfo, isRefundInfo)) {
                            return true;
                        }

                        String parentOrderId = ObjectUtil.defaultIfNull(v.get("parent_order_id"), "").toString();
                        String sourceId = finalOrderIdMaps.get(parentOrderId);
                        if(StringUtils.isBlank(sourceId)) {
                            log.warn("AliExpress纠纷DMP跳过未匹配来源订单的数据。parentOrderId={}, issueId={}, nextLevelId={}, inputTaskId={}, storageName={}",
                                    parentOrderId, issueId, nextLevelId, inputTaskId, dmpCfgInputConvertEntity.getStorageName());
                            return true;
                        }

                        String platformReturnOrRefundNo = StringUtils.defaultIfBlank(snapshot.buyerReturnNo, issueId);
                        String childOrderCode = StringUtils.defaultIfBlank(snapshot.orderId, parentOrderId);
                        String platformOrderCode = StringUtils.defaultIfBlank(parentOrderId, childOrderCode);
                        String buyerUserId = StringUtils.defaultIfBlank(
                                ObjectUtil.defaultIfNull(v.get("buyerUserId"), "").toString(),
                                snapshot.buyerLoginId);
                        v.put("sourceId", sourceId);
                        v.put("buyerUserId", buyerUserId);
                        v.put("shopId", nextLevelId);
                        v.put("shopName", finalShopName);
                        v.put("thirdCode", platformReturnOrRefundNo);
                        v.put("platformCode", childOrderCode);
                        v.put("platformOrderCode", platformOrderCode);
                        v.put("platformStatus", snapshot.reverseDetailStatus);
                        v.put("platformOriginalStatus", snapshot.reverseDetailStatus);
                        v.put("reason", StringUtils.defaultIfBlank(snapshot.reasonChinese, snapshot.reasonEnglish));
                        v.put("remark", StringUtils.defaultIfBlank(snapshot.reasonChinese, snapshot.reasonEnglish));
                        v.put("remarkName", StringUtils.defaultIfBlank(snapshot.reasonEnglish, snapshot.reasonChinese));
                        v.put("trackingNumber", snapshot.buyerReturnLogisticsNo);
                        v.put("logisticsSupplierCode", snapshot.buyerReturnLogisticsCompany);
                        v.put("logisticsSupplierName", snapshot.buyerReturnLogisticsCompany);
                        v.put("platformCreateTime", snapshot.gmtCreate);
                        v.put("platformUpdateTime", snapshot.gmtModified);
                        v.put("returnTime", snapshot.gmtCreate);
                        v.put("refundTime", snapshot.gmtCreate);
                        v.put("currencyCode", snapshot.refundCurrency);
                        v.put("allAmount", snapshot.refundAmount);
                        v.put("amount", snapshot.refundAmount);
                        if (isRefundInfo) {
                            v.put("status", "1");
                        } else {
                            String issueStatus = ObjectUtil.defaultIfNull(v.get("status"), "").toString();
                            v.put("status", "finish".equals(issueStatus) ? "4" : "1");
                        }
                        return false;
                    });
                    if (CollUtil.isEmpty(value)) {
                        iterator.remove();
                    }
                }
			}
		}
	}

    private String getIssueId(Map<String, Object> issueDetail) {
        Object issueId = issueDetail.get("id");
        if (issueId == null) {
            issueId = issueDetail.get("issue_id");
        }
        return issueId == null ? "" : issueId.toString();
    }

    private String getIssueIdFromConvertedData(Map<String, Object> dmpMap) {
        Object issueId = dmpMap.get("thirdCode");
        if (issueId == null || StringUtils.isBlank(issueId.toString())) {
            issueId = dmpMap.get("issue_id");
        }
        return issueId == null ? "" : issueId.toString();
    }

    private boolean matchStorageCondition(IssueDetailSnapshot snapshot, boolean isReturnInfo, boolean isRefundInfo) {
        if (isReturnInfo) {
            return snapshot.hasReturnSolution;
        }
        if (isRefundInfo) {
            return snapshot.hasRefundSolution;
        }
        return true;
    }

    private IssueDetailSnapshot buildIssueDetailSnapshot(Map<String, Object> issueDetail) {
        IssueDetailSnapshot snapshot = new IssueDetailSnapshot();
        snapshot.buyerLoginId = ObjectUtil.defaultIfNull(issueDetail.get("buyer_login_id"), "").toString();
        snapshot.buyerReturnNo = ObjectUtil.defaultIfNull(issueDetail.get("buyer_return_no"), "").toString();
        snapshot.reverseDetailStatus = ObjectUtil.defaultIfNull(issueDetail.get("reverse_detail_status"), "").toString().toLowerCase();
        snapshot.reasonChinese = StringUtils.defaultIfBlank(
                ObjectUtil.defaultIfNull(issueDetail.get("reason_chinese"), "").toString(),
                AliExpressIssueSolutionResolver.getIssueContent(issueDetail));
        snapshot.reasonEnglish = ObjectUtil.defaultIfNull(issueDetail.get("reason_english"), "").toString();
        snapshot.buyerReturnLogisticsCompany = ObjectUtil.defaultIfNull(issueDetail.get("buyer_return_logistics_company"), "").toString();
        snapshot.buyerReturnLogisticsNo = AliExpressIssueSolutionResolver.getReturnTrackingNo(issueDetail);
        snapshot.orderId = ObjectUtil.defaultIfNull(issueDetail.get("order_id"), "").toString();
        snapshot.gmtCreate = ObjectUtil.defaultIfNull(issueDetail.get("gmt_create"), "").toString();
        snapshot.gmtModified = AliExpressIssueSolutionResolver.getLatestEventTime(issueDetail);

        AliExpressIssueSolutionResolver.ResolvedIssueSolution resolvedIssueSolution = AliExpressIssueSolutionResolver.resolve(issueDetail);
        snapshot.hasReturnSolution = resolvedIssueSolution.isMatchedReturn();
        snapshot.hasRefundSolution = resolvedIssueSolution.isMatchedRefund();
        AliExpressIssueSolutionResolver.SolutionRecord effectiveSolution = resolvedIssueSolution.getEffectiveSolution();
        if (effectiveSolution != null) {
            snapshot.solutionType = effectiveSolution.getSolutionType();
            snapshot.refundAmount = ObjectUtil.defaultIfNull(effectiveSolution.get("refund_money"), "").toString();
            snapshot.refundCurrency = ObjectUtil.defaultIfNull(effectiveSolution.get("refund_money_currency"), "").toString();
        }
        return snapshot;
    }

    private static class IssueDetailSnapshot {
        private String solutionType = "";
        private String reverseDetailStatus = "";
        private boolean hasReturnSolution;
        private boolean hasRefundSolution;
        private String buyerLoginId = "";
        private String buyerReturnNo = "";
        private String reasonChinese = "";
        private String reasonEnglish = "";
        private String buyerReturnLogisticsCompany = "";
        private String buyerReturnLogisticsNo = "";
        private String orderId = "";
        private String gmtCreate = "";
        private String gmtModified = "";
        private String refundAmount = "";
        private String refundCurrency = "";
    }

}
