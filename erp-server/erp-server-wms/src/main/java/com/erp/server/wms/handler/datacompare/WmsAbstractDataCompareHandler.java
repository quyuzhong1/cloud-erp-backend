package com.erp.server.wms.handler.datacompare;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.common.business.utils.ApplicationContextUtils;
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
public abstract class WmsAbstractDataCompareHandler<M extends WmsDataCompareDbService<T> , T extends DataCompareDTO> implements WmsDataCompareBillService<T>{
	
	private volatile M wmsDataCompareDbService;
	
	protected Class<T> entityClass = currentModelClass();
	
	private String billType;
	
	@Override
	public List<T> getDataCompareByCondition(String systemDataCondition){
		currentDbService();
		return wmsDataCompareDbService.getDataCompareByCondition(JSON.parseObject(systemDataCondition, entityClass));
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
    	String fileName = WmsDataCompareTaskBillTypeEnum.getName(billType) + "系统数据.xlsx";
        file = ExcelUtil.exportFile(fileName, "系统数据", this.getDataCompareByCondition(systemDataCondition) , entityClass);
        if (file != null && !file.isDirectory()) {
            url = FastDFSClientUtil.uploadFile(file, fileName);
        }
        return url;
	}
	
	@Override
	public T getCompareDTO(String dtoJson) {
		if(StringUtils.isBlank(dtoJson)) {
			try {
				return entityClass.newInstance();
			}catch (Exception e) {

			}
		}else {
			return JSON.parseObject(dtoJson, entityClass);
		}
		return null;
	}
	
	protected void currentDbService() {
		if(wmsDataCompareDbService == null) {
			synchronized (this) {
				wmsDataCompareDbService = (M)ApplicationContextUtils.getBean((Class<T>) ReflectionKit.getSuperClassGenericType(getClass(), 0));
			}
		}
    }
	
	protected Class<T> currentModelClass() {
        return (Class<T>) ReflectionKit.getSuperClassGenericType(getClass(), 1);
    }

	public void setBillType(String billType) {
		this.billType = billType;
	}
	
}