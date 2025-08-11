package com.erp.server.dmp.inout.handler.input.task.init.api.jifeng;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpThirdWarehouseInfoEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.service.DmpThirdWarehouseInfoService;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengInventoryResp;
import com.sdk.wms.jifeng.service.JiFengService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Slf4j
@Service
@Scope("prototype")
public class JiFengInventoryInitHandler extends DmpInputInitHandler {

	public static final String SOURCE_PLATFORM = "sourcePlatform";
	public static final String AUTH_ID = "authId";

	@Resource
	private JiFengService jiFengService;

	@Resource
	private DmpThirdWarehouseInfoService dmpThirdWarehouseInfoService;

	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<DmpInputTaskInitDTO> resultList = new ArrayList<>();

		List<Object> allResult = new ArrayList<>();
		List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class)
				.eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
				.eq(OverseasProviderEntity::getCode, DmpBasicSystemCodeEnum.JIFENG.getCode())
				.list();
		if (CollUtil.isEmpty(overseasProviderEntityList)) {
			return Collections.emptyList();
		}
		// 取对应授权ID授权
		OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
				.filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
				.findFirst()
				.orElse(null);
		if (null == overseasProviderEntity) {
			throw new ServiceException(getPlatForm().getName() + "对应授权ID信息不存在");
		}
		if (overseasProviderEntity.getEnableDate().isAfter(LocalDate.now())) {
			return Collections.emptyList();
		}
		String id = overseasProviderEntity.getId();
		//查询服务商仓库
		List<DmpThirdWarehouseInfoEntity> dmpThirdWarehouseInfoEntityList = dmpThirdWarehouseInfoService.listByAuthId(id);
		if (CollUtil.isEmpty(dmpThirdWarehouseInfoEntityList)) {
			log.warn("{}授权ID下没有仓库信息", id);
			return Collections.emptyList();
		}
		for (DmpThirdWarehouseInfoEntity dmpThirdWarehouseInfoEntity : dmpThirdWarehouseInfoEntityList) {
			JiFengBaseResp<List<JiFengInventoryResp.RowsDTO>> resp = jiFengService.getInventoryList(overseasProviderEntity.getAuthJson(),dmpThirdWarehouseInfoEntity.getWarehouseCode());
			if(resp == null) {
				return Collections.emptyList();
			}
			if(resp.getCode() != 0) {
				throw new ServiceException("极风获取库存列表失败,code:"+resp.getCode()+",msg:"+resp.getMessage());
			}
			resp.getData().forEach(v->{
				v.setWarehouseCode(dmpThirdWarehouseInfoEntity.getWarehouseCode());
				v.setWarehouseName(dmpThirdWarehouseInfoEntity.getWarehouseName());
			});
			allResult.addAll(resp.getData());
		}

		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		JSONArray parseArray = JSON.parseArray(JSONObject.toJSONString(allResult));
		parseArray.forEach(p -> {
			JSONObject j = (JSONObject) p;
			j.put(AUTH_ID, id);
			j.put(SOURCE_PLATFORM, getDmpBasicSystemCodeEnum().getCode());
		});
		dmpInputTaskInitDTO.setMsg(parseArray.toJSONString());

		resultList.add(dmpInputTaskInitDTO);

		return resultList;
	}
	public OmsPlatformEnum getPlatForm(){
		return OmsPlatformEnum.getByCode(getDmpBasicSystemCodeEnum().getCode());
	}

	public  DmpBasicSystemCodeEnum getDmpBasicSystemCodeEnum(){
		return DmpBasicSystemCodeEnum.JIFENG;
	}
}
