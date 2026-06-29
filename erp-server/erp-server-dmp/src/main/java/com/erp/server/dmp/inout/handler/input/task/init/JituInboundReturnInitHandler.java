package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.model.dmp.dto.JituInboundReturnDTO;
import io.seata.common.util.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class JituInboundReturnInitHandler extends DmpInputInitHandler{
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		String data = ThirdWarehouseContext.getData();
		if(StringUtils.isBlank(data)){
			return new ArrayList<>();
		}
		JituInboundReturnDTO returnDTO = JSONUtil.toBean(data, JituInboundReturnDTO.class);
		if(Objects.isNull(returnDTO) || Objects.isNull(returnDTO.getEntryOrderCode())){
			return new ArrayList<>();
		}
		returnDTO.setSourcePlatform(OmsPlatformEnum.JI_TU.getCode());
		returnDTO.setWarehousePlatformType(WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode());
		DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
		returnDTO.getOrderLines().forEach(line -> line.setOperateTime(returnDTO.getOperateTime()));
		dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(returnDTO));
		return Collections.singletonList(dmpInputTaskInitDTO);
	}
}
