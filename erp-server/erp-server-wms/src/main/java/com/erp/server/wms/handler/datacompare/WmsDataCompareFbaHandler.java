package com.erp.server.wms.handler.datacompare;

import java.util.List;
import java.util.Map;

import cn.hutool.core.text.CharSequenceUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.DataCompareDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.FbaShipmentDTO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.wms.mapper.FbaShipmentMapper;

import cn.hutool.core.collection.CollUtil;

import javax.annotation.Resource;

@Service
public class WmsDataCompareFbaHandler extends WmsAbstractDataCompareHandler{

	@Resource
	private FbaShipmentMapper baseMapper;
	
	@Resource
	private ShopInfoFeign shopInfoFeign;
	
	@Override
	public List<Map<String, String>> getDbData(String systemDataCondition , String taskId) {
		com.erp.model.wms.dto.WmsDataCompareTaskDTO.FbaShipmentDTO params = this.getParams(systemDataCondition);
		params.setTaskId(taskId);
		return baseMapper.getDataCompareByCondition(params);
	}

	@Override
	public Integer getDbSystemDataCount(String systemDataCondition) {
		Integer count = baseMapper.getDataCompareByConditionCount(this.getParams(systemDataCondition));
		if(count == null) {
			count = 0;
		}
		return count;
	}
	
	private com.erp.model.wms.dto.WmsDataCompareTaskDTO.FbaShipmentDTO  getParams(String systemDataCondition) {
		com.erp.model.wms.dto.WmsDataCompareTaskDTO.FbaShipmentDTO params = JSON.parseObject(systemDataCondition , com.erp.model.wms.dto.WmsDataCompareTaskDTO.FbaShipmentDTO.class);
		if(CollUtil.isEmpty(params.getReceiveDateList())) {
			throw new ServiceException("FBA货件签收的系统数据范围【签收日期】不能为空");
		}
		String shopId = params.getShopId();
		if(CharSequenceUtil.isNotBlank(shopId)) {
			ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(shopId);
			if(shopInfo != null) {
				params.setShopName(shopInfo.getName());
			}
		}
		return params;
	}
	
	@Override
	public <T extends DataCompareDTO> Class<T> getUploadDtoClass() {
		return (Class<T>) FbaShipmentDTO.class;
	}

}
