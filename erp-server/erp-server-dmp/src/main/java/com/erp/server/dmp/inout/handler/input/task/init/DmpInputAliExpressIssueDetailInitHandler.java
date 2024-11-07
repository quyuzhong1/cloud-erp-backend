package com.erp.server.dmp.inout.handler.input.task.init;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAliExpressIssueDetailInitHandler extends DmpInputInitHandler{
	@Resource
    private AliExpressOrderService aliExpressOrderService;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<Map<String, Object>> findMongoData = null;
		String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
		if(StringUtils.isNotBlank(parentStorageName)) {
			List<ParamData> paramDataList = new ArrayList<>();
			paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
			findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
		}
		if(CollUtil.isEmpty(findMongoData)) {
			return new ArrayList<>();
		}
		String nextLevelId = findMongoData.get(0).get("nextLevelId").toString();
		AliExpressShopInfoDTO aliExpressShopInfoDTO = aliExpressOrderService.getShopInfoByShopId(nextLevelId);
		
		List<ParamData> paramDataList = new ArrayList<>();
		List<Long> orderIdList = new ArrayList<>();
		for(Map<String, Object> f : findMongoData) {
			Object product_list_obj = f.get("product_list");
			if(product_list_obj != null) {
				List<Map<String, Object>> product_list = (List<Map<String, Object>>) product_list_obj;
				for(Map<String, Object> p : product_list) {
					Object child_id = p.get("child_id");
					Object issue_status_obj = p.get("issue_status");
					if(issue_status_obj != null) {
						String issue_status = issue_status_obj.toString();
						if("IN_ISSUE".equals(issue_status) || "END_ISSUE".equals(issue_status)) {
							if(child_id != null) {
								orderIdList.add(Long.valueOf(child_id.toString()));
							}
						}
					}
				}
			}
		}
		paramDataList.add(new ParamData("order_id", "order_id", PannoEnum.IN, orderIdList));
		paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, PannoEnum.EQ, nextLevelId));
		findMongoData = mongoService.findMongoData(paramDataList, "aliexpress_issue_data");
		if(CollUtil.isEmpty(findMongoData)) {
			return new ArrayList<>();
		}
		
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
		
		String appKey = aliExpressShopInfoDTO.getClientId();
        String appSecret = aliExpressShopInfoDTO.getClientSecret();
        String baseUrl = aliExpressShopInfoDTO.getBaseUrl();
        String token = aliExpressShopInfoDTO.getToken();
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
		
        IopRequest request = new IopRequest();
        String typeId = dmpCfgInputEntity.getTypeId();
        
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        
        String apiType = dmpCfgApiEntity.getApiType();
		request.setApiName(apiType);
		
		List<JSONObject> result = new ArrayList<>();
        for(Map<String, Object> findMongo : findMongoData) {
            request.addApiParameter("buyer_login_id", findMongo.get("buyer_login_id").toString());
            request.addApiParameter("issue_id", findMongo.get("issue_id").toString());
            
        	JSONObject data = null;
        	long sleepTime = 1000;
        	int count = 0;
        	while(data == null) {
        		data = this.execute(client, request, token, apiType);
        		if(data == null) {
        			if(count == 10) {
        				throw new ServiceException("调用速卖通" + apiType + "接口重试" + count + "失败");
        			}
        			try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {}
        			sleepTime = sleepTime + 1000;
        			count = count + 1;
        		}
        	}
        	result.add(data.getJSONObject("result_object"));
        }
        
        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSON.toJSONString(result));
		dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
        
		return dmpInputTaskInitDTOList;
	}
	
	private JSONObject execute(IopClient client , IopRequest request , String token , String apiType){
		IopResponse response = null;
		try {
			response = client.execute(request, token, Protocol.TOP);
		} catch (ApiException e) {
			Throwable cause = e.getCause();
			if(cause instanceof SSLHandshakeException || cause instanceof SocketTimeoutException) {
				return null;
			}
			throw new ServiceException("调用速卖通" + apiType + "接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
		}
		JSONObject body = JSON.parseObject(response.getBody());
//		JSONObject body = JSON.parseObject("{\"aliexpress_issue_detail_get_response\":{\"result_object\":{\"gmt_create\":\"2024-10-28 04:23:19\",\"product_price_currency\":\"USD\",\"refund_money_max_local_currency\":\"BRL\",\"refund_money_max\":\"0.00\",\"product_price\":\"66.65\",\"platform_solution_list\":{\"solution_api_dto\":[{\"gmt_create\":\"2024-10-28 04:23:27\",\"refund_money_post_currency\":\"\",\"seller_accept_time\":\"\",\"issue_id\":6346213853171470,\"solution_type\":\"refund\",\"gmt_modified\":\"\",\"solution_owner\":\"platform\",\"refund_money_currency\":\"BRL\",\"refund_money\":\"407.47\",\"refund_money_post\":\"\",\"id\":-2,\"reached_time\":\"\",\"buyer_accept_time\":\"\",\"order_id\":8194715118461470}]},\"issue_reason_id\":115,\"product_id\":\"1005003600816013\",\"buyer_login_id\":\"mx170775425vyrae\",\"buyer_solution_list\":{\"solution_api_dto\":[{\"gmt_create\":\"2024-10-28 04:23:22\",\"refund_money_post_currency\":\"USD\",\"seller_accept_time\":\"\",\"issue_id\":6346213853171470,\"solution_type\":\"refund\",\"is_default\":true,\"gmt_modified\":\"2024-10-28 04:23:22\",\"solution_owner\":\"buyer\",\"content\":\"not receive good\",\"refund_money_currency\":\"BRL\",\"refund_money\":\"407.47\",\"refund_money_post\":\"66.65\",\"id\":6010915445171470,\"reached_time\":\"\",\"buyer_accept_time\":\"\",\"order_id\":8194715118461470,\"status\":\"wait_seller_accept\"}]},\"id\":6345753109519425,\"parent_order_id\":8194715118451470,\"refund_money_max_currency\":\"USD\",\"reverse_detail_status\":\"return_success\",\"issue_status\":\"finish\",\"refund_money_max_local\":\"0.00\",\"product_name\":\"Ulanzi Lino Universal SmartPhone Cage Protective Case with Side Handle Rig for iPhone16 15 14 13 12 Pro Max Samsung  Xiaomi OPPO\",\"order_id\":8194715118461470,\"process_dto_list\":{\"api_issue_process_dto\":[{\"gmt_create\":\"2024-10-28 19:23:14\",\"has_seller_video\":false,\"attachments\":{},\"issue_id\":6346213853171470,\"action_type\":\"perform_solution\",\"submit_member_type\":\"platform\",\"receive_goods\":false,\"has_buyer_video\":false},{\"gmt_create\":\"2024-10-28 19:23:11\",\"has_seller_video\":false,\"attachments\":{},\"issue_id\":6346213853171470,\"action_type\":\"platform_force_reach\",\"submit_member_type\":\"platform\",\"receive_goods\":false,\"has_buyer_video\":false},{\"gmt_create\":\"2024-10-28 04:23:27\",\"has_seller_video\":false,\"attachments\":{},\"issue_id\":6346213853171470,\"action_type\":\"buyer_initiate_arbitration\",\"submit_member_type\":\"buyer\",\"receive_goods\":false,\"has_buyer_video\":false},{\"gmt_create\":\"2024-10-28 04:23:22\",\"has_seller_video\":false,\"attachments\":{},\"issue_id\":6346213853171470,\"action_type\":\"initiate\",\"submit_member_type\":\"buyer\",\"receive_goods\":false,\"content\":\"not receive good\",\"has_buyer_video\":false},{\"gmt_create\":\"2024-10-28 04:23:19\",\"has_seller_video\":false,\"attachments\":{},\"issue_id\":6346213853171470,\"action_type\":\"initiate\",\"submit_member_type\":\"buyer\",\"receive_goods\":false,\"has_buyer_video\":false}]}},\"request_id\":\"2140eaab17301752515171777\"}}");
        JSONObject data = body.getJSONObject("aliexpress_issue_detail_get_response");
        if(data == null) {
        	JSONObject errorResponse = body.getJSONObject("error_response");
        	if(errorResponse == null) {
        		return null;
        	}
        	String code = errorResponse.getString("code");
        	if(!"ApiCallLimit".equals(code) && !"15".equals(code) && !"UnknownRuntimeException".equals(code)) {
        		throw new ServiceException("调用速卖通" + apiType + "接口报错，错误原因：" + errorResponse.getString("msg"));
        	}
        }
		return data;
	}
}
