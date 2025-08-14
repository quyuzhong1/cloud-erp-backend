package com.erp.server.dmp.inout.handler.input.task.init.api.damai;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.sdk.wms.damai.dto.request.DaMaiInventoryTransRequest;
import com.sdk.wms.damai.dto.response.DaMaiInventoryTransResp;
import com.sdk.wms.damai.dto.response.DaMaiPageBaseResp;
import com.sdk.wms.damai.service.DaMaiService;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengInboundResp;
import com.sdk.wms.jifeng.service.JiFengService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的大卖仓api获取数据方式
 * @author Administrator
 *
 */
@Service
@Slf4j
@Scope("prototype")
public class DaMaiInboundInitHandler extends DmpInputInitHandler {

	@Resource
	private DaMaiService daMaiService;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		//查询待签收、部分签收状态的入库单
		List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class)
				.eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
				.eq(OverseasProviderEntity::getCode, DmpBasicSystemCodeEnum.DA_MAI.getCode())
				.list();
		if (CollUtil.isEmpty(overseasProviderEntityList)) {
			throw new ServiceException("大卖仓授权信息不存在");
		}
		// 取对应授权ID授权
		OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
				.filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
				.findFirst()
				.orElse(null);
		if (null == overseasProviderEntity) {
			throw new ServiceException("大卖仓对应授权ID信息不存在");
		}

		DaMaiInventoryTransRequest daMaiInventoryTransRequest = new DaMaiInventoryTransRequest();
		daMaiInventoryTransRequest.setStartOperationTime(dmpInputTaskEntity.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		daMaiInventoryTransRequest.setEndOperationTime(dmpInputTaskEntity.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		DaMaiPageBaseResp<List<DaMaiInventoryTransResp>> resp = daMaiService.getInventoryTrans(overseasProviderEntity.getAuthJson(), daMaiInventoryTransRequest);
		if (resp == null) {
			return Collections.emptyList();
		}
		if (StringUtils.isNotBlank(resp.getMsg())) {
			throw new ServiceException("大卖仓获取入库单列表失败,msg:" + resp.getMsg());
		}
		if (CollUtil.isEmpty(resp.getData())) {
			return Collections.emptyList();
		}
		List<DaMaiInventoryTransResp> daMaiInventoryTransResps = resp.getData();
		daMaiInventoryTransResps = daMaiInventoryTransResps.stream().filter(v -> v.getBillTypeName().equals("入库单")).collect(Collectors.toList());
		if (CollUtil.isEmpty(daMaiInventoryTransResps)) {
			return Collections.emptyList();
		}

		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(daMaiInventoryTransResps));
		return Collections.singletonList(dmpInputTaskInitDTO);
	}

	
	
}
