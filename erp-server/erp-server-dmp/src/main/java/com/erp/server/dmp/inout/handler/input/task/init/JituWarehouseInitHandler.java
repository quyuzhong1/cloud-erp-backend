package com.erp.server.dmp.inout.handler.input.task.init;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.sdk.wms.jitu.service.JituService;
import com.sdk.wms.jitu.utils.AuthUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONUtil;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class JituWarehouseInitHandler extends DmpInputInitHandler{
	
	@Resource
	private JituService jituService;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(DmpBasicSystemCodeEnum.JITU.getCode()));
		if (CollUtil.isEmpty(overseasProviderEntityList)) {
			throw new ServiceException("极兔对应授权ID信息不存在");
		}
		// 取对应授权ID授权
		OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
				.filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
				.findFirst()
				.orElse(null);
		if (null == overseasProviderEntity) {
			throw new ServiceException("极兔对应授权ID信息不存在");
		}
		
		String typeId = dmpCfgInputEntity.getTypeId();
		DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
		String apiType = dmpCfgApiEntity.getApiType();
		
		Map<String, Object> authMap = overseasProviderEntity.getAuthJson();
        authMap.put("msg_type", "OBTAINWAREHOUSE");
        JSONObject request = new JSONObject();
        request.put("customerid", authMap.get("customerid"));
        authMap.put("logistics_interface", JSONUtil.toJsonStr(request));
		
		DmpInputTaskInitDTO result = new DmpInputTaskInitDTO();
		try {
			String bodyStr = AuthUtils.doPost(jituService.getPreUrl() + apiType, authMap);
            JSONObject responseJson = JSON.parseObject(bodyStr);
            JSONArray jsonArray = responseJson.getJSONArray("responseitems");
            Object object = jsonArray.get(0);
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(object));
            String success = jsonObject.getString("success");
            if ("true".equals(success)) {
                JSONArray baseList = jsonObject.getJSONArray("baseList");
                baseList.forEach(p -> {
        			JSONObject j = (JSONObject)p;
        			j.put("authId", nextLevelId);
        		});
                result.setMsg(baseList.toJSONString());
            } else {
                throw new ServiceException("极兔获取仓库接口失败: " + bodyStr);
            }
		} catch (IOException e1) {
			throw new ServiceException("极兔获取仓库接口报错" + ExceptionUtil.stacktraceToOneLineString(e1));
		}
		return Collections.singletonList(result);
	}
}
