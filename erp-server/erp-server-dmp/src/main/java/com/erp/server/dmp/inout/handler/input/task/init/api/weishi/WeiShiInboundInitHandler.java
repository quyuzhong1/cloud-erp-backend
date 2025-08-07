package com.erp.server.dmp.inout.handler.input.task.init.api.weishi;

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
import com.sdk.wms.weishi.dto.request.WeiShiQueryInboundRequest;
import com.sdk.wms.weishi.dto.response.WeiShiBaseResp;
import com.sdk.wms.weishi.dto.response.WeiShiInboundResp;
import com.sdk.wms.weishi.service.WeiShiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * dmp输入init任务基础处理器下的纬狮api获取数据方式
 * @author Administrator
 *
 */
@Service
@Slf4j
@Scope("prototype")
public class WeiShiInboundInitHandler extends DmpInputInitHandler {

	@Resource
    private DmpHandlerCache dmpHandlerCache;

	@Resource
	private WeiShiService weiShiService;
	
	@Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        //查询待签收、部分签收状态的入库单
        List<String> receiveCodeList = overseasWarehouseFeign.getReceiptNumbersForStatus(Arrays.asList(OverseasInstockStatusEnum.TO_BE_SIGNED.getCode()
                ,OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode()
                ,OverseasInstockStatusEnum.MANUAL_COMPLETION.getCode()), OmsPlatformEnum.WEI_SHI.getCode());
        List<WeiShiInboundResp.RowsDTO> allResult = new ArrayList<>();
        
        if(CollUtil.isNotEmpty(receiveCodeList)) {

			List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class)
					.eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
					.eq(OverseasProviderEntity::getCode, DmpBasicSystemCodeEnum.WEI_SHI.getCode())
					.list();
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
			List<List<String>> splitList = CollUtil.split(receiveCodeList, 100);
			for (List<String> codeList : splitList) {
				WeiShiQueryInboundRequest weiShiCancelInboundRequest = new WeiShiQueryInboundRequest();
				weiShiCancelInboundRequest.setOrderNoList(codeList);
				weiShiCancelInboundRequest.setPageSize(50);
				weiShiCancelInboundRequest.setPageNum(1);
				WeiShiBaseResp<WeiShiInboundResp> weiShiInboundResp = weiShiService.getInbound(weiShiCancelInboundRequest,overseasProviderEntity.getAuthJson());
				if (null == weiShiInboundResp || weiShiInboundResp.getData() == null) {
					throw new ServiceException("纬狮获取入库列表失败: 响应结果为空");
				}
				if(!weiShiInboundResp.getCode().equals(200)){
					throw new ServiceException("纬狮获取入库单列表失败: " + weiShiInboundResp.getMsg());
				}
				if (CollUtil.isEmpty(weiShiInboundResp.getData().getList())) {
					return Collections.emptyList();
				}
				weiShiInboundResp.getData().getList().forEach(v->{
					v.getInboundBoxList().forEach(box -> {
						box.setFinishPutawayTime(v.getReceiptLastTime());
					});
				});
				allResult.addAll(weiShiInboundResp.getData().getList());
			}
            
        }
		
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(allResult));
		return Collections.singletonList(dmpInputTaskInitDTO);
	}

	
	
}
