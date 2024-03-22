package com.erp.server.wms.handler.datacompare;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.DataCompareDTO;
import com.erp.model.wms.enums.WmsDataCompareTaskBillTypeEnum;
import com.erp.server.wms.service.WmsDataCompareBillService;
import com.erp.server.wms.service.WmsDataCompareDbService;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public abstract class WmsAbstractDataCompareHandler<T extends DataCompareDTO> implements WmsDataCompareBillService{
	
	@Override
	public List<T> getDataCompareByCondition(String systemDataCondition){
		return getWmsDataCompareDbService().getDataCompareByCondition(getDto(systemDataCondition));
	}
	
	@Override
	public Integer getSystemDataCount(String systemDataCondition) {
		List<T> billSystemDatas = this.getDataCompareByCondition(systemDataCondition);
		if(CollUtil.isEmpty(billSystemDatas)) {
			return 0;
		}else {
			return billSystemDatas.size();
		}
	}
	
	@Override
	public String uploadSystemDataByCondition(String systemDataCondition) {
		String url = "";
		File file = null;
    	String fileName = getWmsDataCompareTaskBillTypeEnum().getName() + "系统数据.xlsx";
        file = ExcelUtil.exportFile(fileName, "系统数据", this.getDataCompareByCondition(systemDataCondition) , getDto(systemDataCondition).getClass());
        if (file != null && !file.isDirectory()) {
            url = FastDFSClientUtil.uploadFile(file, fileName);
        }
        return url;
	}
	
	private T getDto(String systemDataCondition) {
		String className = "";
        Type type = this.getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) type;
        for (Type t : parameterizedType.getActualTypeArguments()) {
            className = t.getTypeName();
            if (StringUtils.isNotEmpty(className)) {
                break;
            }
        }
		try {
			Class<T> dataClass = (Class<T>) Class.forName(className);
			return JSON.parseObject(systemDataCondition, dataClass);
		} catch (ClassNotFoundException e) {
			log.error("数据对比抽象类获取实例失败");
		}
		return null;
	}
	
	abstract WmsDataCompareDbService<T> getWmsDataCompareDbService();
	
	abstract WmsDataCompareTaskBillTypeEnum getWmsDataCompareTaskBillTypeEnum();
}