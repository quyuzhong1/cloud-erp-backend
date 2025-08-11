package com.erp.server.dmp.inout.handler.input.task.init.api.jifeng;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.sdk.wms.goodcang.dto.response.GoodCangReceiptBatchResp;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.utils.GoodCangUtils;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengInboundResp;
import com.sdk.wms.jifeng.service.JiFengService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * dmp输入init任务基础处理器下的极风api获取数据方式
 * @author Administrator
 *
 */
@Service
@Slf4j
@Scope("prototype")
public class JiFengInboundInitHandler extends DmpInputInitHandler {

	@Resource
    private DmpHandlerCache dmpHandlerCache;

	@Resource
	private JiFengService jiFengService;
	
	@Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        //查询待签收、部分签收状态的入库单
        List<String> receiveCodeList = overseasWarehouseFeign.getReceiptNumbersForStatus(Arrays.asList(OverseasInstockStatusEnum.TO_BE_SIGNED.getCode()
                ,OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode()
                ,OverseasInstockStatusEnum.MANUAL_COMPLETION.getCode()), OmsPlatformEnum.JIFENG.getCode());
        List<JiFengInboundResp> allResult = new ArrayList<>();
        
        if(CollUtil.isNotEmpty(receiveCodeList)) {

			List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class)
					.eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
					.eq(OverseasProviderEntity::getCode, DmpBasicSystemCodeEnum.JIFENG.getCode())
					.list();
            if(CollUtil.isEmpty(overseasProviderEntityList)) {
            	throw new ServiceException("极风授权信息不存在");
            }
			// 取对应授权ID授权
			OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
					.filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
					.findFirst()
					.orElse(null);
			if(null == overseasProviderEntity) {
				throw new ServiceException("极风对应授权ID信息不存在");
			}
            
            for(String receiveCode : receiveCodeList) {
				JiFengBaseResp<JiFengInboundResp> resp = jiFengService.getInbound(overseasProviderEntity.getAuthJson(),receiveCode);
            	if(resp.getCode() != 0) {
					log.error("极风获取入库单失败，code:{},msg:{}",resp.getCode(),resp.getMessage());
            		continue;
            	}
				JiFengInboundResp jiFengInboundResp = resp.getData();
				if(jiFengInboundResp == null || Objects.isNull(jiFengInboundResp.getPutawayLastTime())) {
					log.error("极风获取入库单失败，入库单号：{}，入库单数据或时间为空",receiveCode);
					continue;
				}
				List<JiFengInboundResp.SkuListDTO> skuListDTOS = jiFengInboundResp.getSkuList();
				if(CollUtil.isEmpty(skuListDTOS)) {
					log.warn("极风获取入库单失败，入库单号：{}，sku列表信息为空",receiveCode);
					continue;
				}
				skuListDTOS.forEach(v->{
					v.setPutawayLastTime(jiFengInboundResp.getPutawayLastTime());
				});
            	allResult.add(resp.getData());
            }
        }
		
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(allResult));
		return Collections.singletonList(dmpInputTaskInitDTO);
	}

	
	
}
