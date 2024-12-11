package com.erp.server.dmp.inout.handler.input.task.init;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import com.common.business.constant.BusinessCommonConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import com.sdk.wms.goodcang.dto.request.GoodCangGetSkuReq;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.utils.GoodCangUtils;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputGoodCangInitHandler extends DmpInputInitHandler{

	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	@Resource
    private DmpHandlerCache dmpHandlerCache;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();

		// 请求页数:默认100
		int pageSize = checkAndGetPageSize();

		GoodCangGetSkuReq goodCangGetSkuReq = new GoodCangGetSkuReq();
        Integer page = 1;
        goodCangGetSkuReq.setPageSize(pageSize);
        int currTotal = 0;
        List<Object> allResult = new ArrayList<>();
        List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(DmpBasicSystemCodeEnum.GOODCANG.getCode()));
        if(CollUtil.isEmpty(overseasProviderEntityList)) {
        	throw new ServiceException("谷仓授权信息不存在");
        }
		// 取对应授权ID授权
		OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
				.filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
				.findFirst()
				.orElse(null);
		if(null == overseasProviderEntity) {
			throw new ServiceException("谷仓对应授权ID信息不存在");
		}
		ThirdWarehouseContext.setAuthMap(overseasProviderEntity.getAuthJson());
		// 非线上环境拉取当天
		if (!BusinessCommonConstants.hasProfile("prod")){
			LocalDateTime startTime = dmpInputTaskEntity.getStartTime();
			LocalDateTime endTime = dmpInputTaskEntity.getEndTime();
			goodCangGetSkuReq.setProductUpdateTimeFrom(startTime.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
			goodCangGetSkuReq.setProductUpdateTimeTo(endTime.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
		}

        while(true) {
        	goodCangGetSkuReq.setPage(page);
        	String response = GoodCangUtils.sendPost(apiType,JSON.toJSONString(goodCangGetSkuReq));
        	GoodCangResponse<List<?>> result = JSONObject.parseObject(response,new TypeReference<GoodCangResponse<List<Object>>>() {}.getType());
        	if (!GoodCangUtils.SUCCESS.equalsIgnoreCase(result.getAsk())){
				ServiceException.runError(response);
			}

			List<?> data = result.getData();
        	int size = data.size();
        	if(size == 0) {
        		break;
        	}
        	allResult.addAll(data);
			currTotal = currTotal + size;
        	Integer count = result.getCount();
        	if(count == null) {
        		count  = 0;
        	}
        	if(currTotal >= count) {
        		break;
        	}
        	page = page + 1;
        }
        String id = overseasProviderEntity.getId();
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		JSONArray parseArray = JSON.parseArray(JSONObject.toJSONString(allResult));
		parseArray.forEach(p -> {
			JSONObject j = (JSONObject)p;
			j.put("authId", id);
		});
		dmpInputTaskInitDTO.setMsg(parseArray.toJSONString());
		return Collections.singletonList(dmpInputTaskInitDTO);
	}

	/**
	 * 解析请求页数:默认100
	 */
	private int checkAndGetPageSize() {
		if (StringUtils.isNotBlank(dmpCfgInputEntity.getExtendJson())){
			// 配置指定数量
			JSONObject jsonObject = JSON.parseObject(dmpCfgInputEntity.getExtendJson());
			if (null != jsonObject){
				Integer cfgPageSize = jsonObject.getInteger("pageSize");
				if (null != cfgPageSize){
					return cfgPageSize;
				}
			}
		}
		return 100;
	}

}
