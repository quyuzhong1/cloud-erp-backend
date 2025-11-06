package com.erp.server.dmp.inout.handler.input.task.init.api.weishi;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
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
import com.sdk.wms.weishi.dto.request.WeiShiGetReturnInstockRequest;
import com.sdk.wms.weishi.dto.request.WeiShiQueryInboundRequest;
import com.sdk.wms.weishi.dto.response.WeiShiBaseResp;
import com.sdk.wms.weishi.dto.response.WeiShiInboundResp;
import com.sdk.wms.weishi.dto.response.WeiShiReturnInstockResp;
import com.sdk.wms.weishi.service.WeiShiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * dmp输入init任务基础处理器下的纬狮api获取数据方式
 * @author Administrator
 *
 */
@Service
@Slf4j
@Scope("prototype")
public class WeiShiReturnInstockInitHandler extends DmpInputInitHandler {

	@Resource
    private DmpHandlerCache dmpHandlerCache;

	@Resource
	private WeiShiService weiShiService;
	
	@Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {

        List<WeiShiReturnInstockResp.RowsDTO> allResult = new ArrayList<>();


		List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class)
				.eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
				.eq(OverseasProviderEntity::getCode, DmpBasicSystemCodeEnum.WEI_SHI.getCode())
				.list();
		if (CollUtil.isEmpty(overseasProviderEntityList)) {
			throw new ServiceException("纬狮授权信息不存在");
		}
		// 取对应授权ID授权
		OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
				.filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
				.findFirst()
				.orElse(null);
		if (null == overseasProviderEntity) {
			throw new ServiceException("纬狮对应授权ID信息不存在");
		}
		WeiShiGetReturnInstockRequest weiShiGetReturnInstockRequest = new WeiShiGetReturnInstockRequest();
		weiShiGetReturnInstockRequest.setQueryTime(WeiShiGetReturnInstockRequest.QueryTimeDTO.builder()
				.startTime(dmpInputTaskEntity.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
				.endTime(dmpInputTaskEntity.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
				.eventType("UPDATE")
				.build());
		WeiShiBaseResp<WeiShiReturnInstockResp> resp = weiShiService.getReturnInstock(weiShiGetReturnInstockRequest,overseasProviderEntity.getAuthJson());
		if (null == resp) {
			throw new ServiceException("纬狮获取退货入库数据失败: 响应结果为空");
		}
		if(resp.getCode() != 200){
			log.warn("纬狮获取退货入库失败，code:{},msg:{}",resp.getCode(),resp.getMsg());
			throw new ServiceException("纬狮获取退货入库失败，code:"+resp.getCode()+",msg:"+resp.getMsg());
		}
		if (resp.getData() == null) {
			throw new ServiceException("纬狮获取退货入库数据失败: 数据体为空");
		}
		if(CollectionUtils.isEmpty(resp.getData().getRows())){
			log.info("纬狮获取退货入库数据为空");
			return Collections.emptyList();
		}
		allResult.addAll(resp.getData().getRows());
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(allResult));
		return Collections.singletonList(dmpInputTaskInitDTO);
	}

	
	
}
