package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.sdk.wms.weishi.dto.request.WeiShiProductRequest;
import com.sdk.wms.weishi.dto.response.WeiShiBaseResp;
import com.sdk.wms.weishi.dto.response.WeiShiProductResp;
import com.sdk.wms.weishi.dto.response.WeiShiWarehouseResp;
import com.sdk.wms.weishi.service.WeiShiService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputWeiShiInitHandler extends DmpInputInitHandler{

	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	@Resource
    private DmpHandlerCache dmpHandlerCache;

	@Resource
	private WeiShiService weiShiService;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {

        List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(DmpBasicSystemCodeEnum.WEI_SHI.getCode()));
        if(CollUtil.isEmpty(overseasProviderEntityList)) {
        	throw new ServiceException("纬狮授权信息不存在");
        }
		// 取对应授权ID授权
		OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
				.filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
				.findFirst()
				.orElse(null);
		if(null == overseasProviderEntity) {
			throw new ServiceException("纬狮对应授权ID信息不存在");
		}
		WeiShiProductRequest weiShiProductRequest = new WeiShiProductRequest();
		weiShiProductRequest.setAuthMap(overseasProviderEntity.getAuthJson());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		weiShiProductRequest.setStartTime(overseasProviderEntity.getEnableDate().atStartOfDay().format(formatter));
		weiShiProductRequest.setEndTime(LocalDateTime.now().format(formatter));
		WeiShiBaseResp<List<WeiShiProductResp.ListDTO>> resp = weiShiService.querySkuList(weiShiProductRequest);
		if(!resp.getCode().equals(200)){
			throw new ServiceException("纬狮获取产品列表失败: " + resp.getMsg());
		}
		if (CollUtil.isEmpty(resp.getData())) {
			return Collections.emptyList();
		}
        String id = overseasProviderEntity.getId();
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		JSONArray parseArray = JSON.parseArray(JSONObject.toJSONString(resp.getData()));
		parseArray.forEach(p -> {
			JSONObject j = (JSONObject)p;
			j.put("authId", id);
		});
		dmpInputTaskInitDTO.setMsg(parseArray.toJSONString());
		return Collections.singletonList(dmpInputTaskInitDTO);
	}

}
