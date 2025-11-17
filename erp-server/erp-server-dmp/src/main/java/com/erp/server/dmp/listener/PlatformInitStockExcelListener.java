package com.erp.server.dmp.listener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.LoginUser;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.dmp.dto.excel.PlatformInitStockExcelDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.server.dmp.mapper.doris.AdsErpInventoryDiffFlowMapper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.Getter;

public class PlatformInitStockExcelListener extends AnalysisEventListener<PlatformInitStockExcelDTO> {
    @Getter
    private List<PlatformInitStockExcelDTO> dataList = new ArrayList<>();

    @Getter
    private List<PlatformInitStockExcelDTO> errorList = new ArrayList<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(PlatformInitStockExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        try {
			String checkMonth = excelDTO.getCheckMonth();
			DateUtil.parse(checkMonth, "yyyy年MM月");
		} catch (Exception e) {
			errorMsgList.add("周期格式不为yyyy年MM月");
		}
        try {
        	String initQty = excelDTO.getInitQty();
        	if(StringUtils.isNotBlank(initQty)) {
        		Integer.parseInt(initQty);
        	}
        }catch (Exception e){
            errorMsgList.add("期初数量不为整数");
        }
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        dataList.add(excelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if(CollectionUtils.isEmpty(dataList)){
            return;
        }
        List<String> warehouseNameList = dataList.stream().map(PlatformInitStockExcelDTO::getPlatformWarehouseName).distinct().collect(Collectors.toList());
        List<String> checkMonthList = dataList.stream().map(PlatformInitStockExcelDTO::getCheckMonth).distinct().collect(Collectors.toList());
        List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntityList = FeignQuery.create(OverseasProviderWarehouseEntity.class).in(OverseasProviderWarehouseEntity::getPlatformWarehouseName, warehouseNameList).list();
        Map<String, String> idAccountMap = FeignQuery.list(OverseasProviderEntity.class).stream().collect(Collectors.toMap(OverseasProviderEntity::getId, OverseasProviderEntity::getPlatformAccount));
        Map<String, List<OverseasProviderWarehouseEntity>> nameGroupMap = overseasProviderWarehouseEntityList.stream().filter(c -> idAccountMap.containsKey(c.getMainId()))
        		.collect(Collectors.groupingBy(OverseasProviderWarehouseEntity::getPlatformWarehouseName));
        LoginUser defaultLoginUser = UserContext.getNonLoginUser();
        IdentifierGenerator identifierGenerator = ApplicationContextUtils.getBean(IdentifierGenerator.class);
        AdsErpInventoryDiffFlowMapper adsErpInventoryDiffFlowMapper = ApplicationContextUtils.getBean(AdsErpInventoryDiffFlowMapper.class);
        Map<String, PlatformInitStockExcelDTO> checkMonthAndWarehouseMap = new HashMap<>();
        List<PlatformInitStockExcelDTO> listInit = adsErpInventoryDiffFlowMapper.listInit(warehouseNameList, checkMonthList);
        if(CollUtil.isNotEmpty(listInit)) {
        	checkMonthAndWarehouseMap = listInit.stream().collect(Collectors.toMap(c -> c.getPlatformWarehouseName() + "_" + c.getCheckMonth(), v -> v , (c1 , c2) -> c1));
        }
        List<PlatformInitStockExcelDTO> insertDbList = new ArrayList<>();
        for (PlatformInitStockExcelDTO excelDTO : dataList) {
        	String platformWarehouseName = excelDTO.getPlatformWarehouseName();
			List<OverseasProviderWarehouseEntity> list = nameGroupMap.get(platformWarehouseName);
        	if(CollUtil.isEmpty(list)) {
        		excelDTO.setErrorMsg(CharSequenceUtil.format("【{}】仓库名称在系统不存在", platformWarehouseName));
                errorList.add(excelDTO);
                continue;
        	}
        	if(list.size() > 1) {
        		excelDTO.setErrorMsg(CharSequenceUtil.format("【{}】仓库名称在系统存在多个，请联系实施处理", platformWarehouseName));
                errorList.add(excelDTO);
                continue;
        	}
        	OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = list.get(0);
        	PlatformInitStockExcelDTO dbExcelDTO = checkMonthAndWarehouseMap.get(excelDTO.getPlatformWarehouseName() + "_" + excelDTO.getCheckMonth());
        	if(dbExcelDTO != null) {
        		excelDTO.setId(dbExcelDTO.getId());
        		excelDTO.setCreateUserId(dbExcelDTO.getCreateUserId());
            	excelDTO.setCreateUserName(dbExcelDTO.getCreateUserName());
            	excelDTO.setCreateTime(dbExcelDTO.getCreateTime());
        	}else {
        		excelDTO.setId(identifierGenerator.nextId(excelDTO).toString());
        		excelDTO.setCreateUserId(defaultLoginUser.getUid());
            	excelDTO.setCreateUserName(defaultLoginUser.getUserName());
            	excelDTO.setCreateTime(LocalDateTime.now());
        	}
        	excelDTO.setAccountCode(idAccountMap.get(overseasProviderWarehouseEntity.getMainId()));
        	excelDTO.setPlatformWarehouseCode(overseasProviderWarehouseEntity.getPlatformWarehouseCode());
        	excelDTO.setUpdateUserId(defaultLoginUser.getUid());
        	excelDTO.setUpdateUserName(defaultLoginUser.getUserName());
        	excelDTO.setUpdateTime(LocalDateTime.now());
        	insertDbList.add(excelDTO);
        }
        if(CollUtil.isNotEmpty(insertDbList)) {
        	adsErpInventoryDiffFlowMapper.batchInsertInit(insertDbList);
        }
    }

}
