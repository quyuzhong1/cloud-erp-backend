package com.erp.server.tms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO.CostViewDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO.UpdateDTO;
import com.erp.model.tms.dto.excel.FirstMileEstimatedBillExcelDTO;
import com.erp.model.tms.dto.excel.FmLogisticsBillCostExcelDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.TmsCfgCostEntity;
import com.erp.model.tms.entity.TmsCostDetailEntity;
import com.erp.model.tms.enums.DictCostAttributionEnum;
import com.erp.model.tms.enums.DictCostCategoryEnum;
import com.erp.model.tms.enums.LogisticsBillCostTypeEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.TmsCostDetailService;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class FmLogisticsBillCostExcelListener extends AnalysisEventListener<FmLogisticsBillCostExcelDTO> {


    private final TmsFirstMileLogisticService tmsFirstMileLogisticService = SpringUtil.getBean(TmsFirstMileLogisticService.class);

    private final LogisticsBillCostService logisticsBillCostService = SpringUtil.getBean(LogisticsBillCostService.class);

    private final TmsCostDetailService logisticsBillCostDetailService = SpringUtil.getBean(TmsCostDetailService.class);

    @Getter
    private List<FmLogisticsBillCostExcelDTO> dataList = new ArrayList<>();

    @Getter
    private List<FmLogisticsBillCostExcelDTO> errorList = new ArrayList<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(FmLogisticsBillCostExcelDTO excelDTO, AnalysisContext analysisContext) {
        if(StringUtils.isBlank(excelDTO.getOutstockCode()) && StringUtils.isBlank(excelDTO.getTransportNo())){
            excelDTO.setErrorMsg("发货单号和运单号不能都为空");
            errorList.add(excelDTO);
            return;
        }
        if(StringUtils.isBlank(excelDTO.getCurrency())) {
        	excelDTO.setErrorMsg("币种不能都为空");
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
        List<String> outstockCodeList = dataList.stream().map(FmLogisticsBillCostExcelDTO::getOutstockCode).distinct().collect(Collectors.toList());
        List<String> transportNoList = dataList.stream().map(FmLogisticsBillCostExcelDTO::getTransportNo).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillEntityList = new ArrayList<>();
        logisticsBillEntityList.addAll(tmsFirstMileLogisticService.listByOutstcockCode(outstockCodeList));
        logisticsBillEntityList.addAll(tmsFirstMileLogisticService.listByTransportNo(transportNoList));
        List<String> mainIdList = logisticsBillEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<LogisticsBillCostEntity> logisticsBillCostEntitieList = logisticsBillCostService.listByLogisticsBillIdList(mainIdList);
        List<String> costIdList = logisticsBillCostEntitieList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<TmsCostDetailDTO.CostViewDTO> allCostDetailEntityList = logisticsBillCostDetailService.listCostByMainIdList(costIdList);
        List<LogisticsBillCostEntity> updateCostList = new ArrayList<>();
        List<TmsCostDetailEntity> updateCostDetailList = new ArrayList<>();
        for (FmLogisticsBillCostExcelDTO excelDTO : dataList) {
            LogisticsBillEntity entity = logisticsBillEntityList.stream().filter(v->v.getOutstockCode().equals(excelDTO.getOutstockCode()) || v.getTransportNo().equals(excelDTO.getTransportNo())).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                excelDTO.setErrorMsg("未找到物流单");
                errorList.add(excelDTO);
                continue;
            }
            if(StringUtils.isNotBlank(excelDTO.getTransportNo()) && StringUtils.isNotBlank(excelDTO.getOutstockCode())
                &&(!entity.getTransportNo().equals(excelDTO.getTransportNo()) || !entity.getOutstockCode().equals(excelDTO.getOutstockCode()))){
                excelDTO.setErrorMsg("来源单号与运单号不匹配");
                errorList.add(excelDTO);
                continue;
            }
            
            LogisticsBillCostEntity costEntity = logisticsBillCostEntitieList.stream().filter(v->v.getLogisticsBillId().equals(entity.getId())).findFirst().orElse(null);
            if(Objects.isNull(costEntity)){
                excelDTO.setErrorMsg("未找到物流费用");
                errorList.add(excelDTO);
                continue;
            }
            if(!(costEntity.getReconciliationStatus().equals(ReconciliationStatusEnum.INVALID.getCode()) ||costEntity.getReconciliationStatus().equals(ReconciliationStatusEnum.TO_BE_GENERATED.getCode()))){
                excelDTO.setErrorMsg("已生成对账单，不能更新信息");
                errorList.add(excelDTO);
                continue;
            }
            if(Objects.nonNull(excelDTO.getWeightLogistics())){
                costEntity.setWeightLogistics(excelDTO.getWeightLogistics());
            }
            if(Objects.nonNull(excelDTO.getVolumeWeightLogistics())){
                costEntity.setVolumeWeightLogistics(excelDTO.getVolumeWeightLogistics());
            }
            updateCostList.add(costEntity);
            if(Objects.nonNull(excelDTO.getCostName()) && Objects.nonNull(excelDTO.getCost())){
                TmsCostDetailDTO.CostViewDTO costViewDTO = allCostDetailEntityList.stream().filter(v->v.getMainId().equals(costEntity.getId()) && v.getCostName().equals(excelDTO.getCostName())).findFirst().orElse(null);
                if(Objects.isNull(costViewDTO)){
                    excelDTO.setErrorMsg("未找到物流明细费用");
                    errorList.add(excelDTO);
                    continue;
                }
            }
            excelDTO.setTransportNo(entity.getTransportNo());
        }
        
        List<FmLogisticsBillCostExcelDTO> lastSuccessList = dataList.stream().filter(d -> StringUtils.isBlank(d.getErrorMsg())).collect(Collectors.toList());
        Map<String, List<FmLogisticsBillCostExcelDTO>> transportNoMaps = lastSuccessList.stream().collect(Collectors.groupingBy(FmLogisticsBillCostExcelDTO::getTransportNo));
    	Map<String, String> transportNoInfoMap = logisticsBillCostEntitieList.stream().collect(Collectors.toMap(LogisticsBillCostEntity::getTransportNo, LogisticsBillCostEntity::getId , (v1 , v2) -> v1));
    	Map<String, List<CostViewDTO>> mainCostMaps = allCostDetailEntityList.stream().collect(Collectors.groupingBy(CostViewDTO::getMainId));
    	Map<String, CostViewDTO> costNameIdMap = allCostDetailEntityList.stream().collect(Collectors.toMap(TmsCostDetailDTO.CostViewDTO::getCostName, t -> t , (v1 , v2) -> v1));
        if(CollUtil.isNotEmpty(lastSuccessList)) {
        	Map<String, List<String>> errorTransportNoDictMap = new HashMap<>();
        	for(Map.Entry<String, List<FmLogisticsBillCostExcelDTO>> transportNoMap : transportNoMaps.entrySet()) {
        		String key = transportNoMap.getKey();
        		String mainId = transportNoInfoMap.get(key);
        		Map<String, String> dictCurrency = new HashMap<>();
        		List<FmLogisticsBillCostExcelDTO> value = transportNoMap.getValue();
        		List<String> errorDictList = errorTransportNoDictMap.get(key);
        		if(errorDictList == null) {
        			errorDictList = new ArrayList<>();
        		}
        		for(FmLogisticsBillCostExcelDTO v : value) {
        			CostViewDTO tmsCfgCostEntity = costNameIdMap.get(v.getCostName());
        			String dictCostCategory = tmsCfgCostEntity.getDictCostCategory();
        			if(errorDictList.contains(dictCostCategory)) {
        				continue;
        			}
        			String currency = dictCurrency.get(dictCostCategory);
        			if(StringUtils.isBlank(currency)) {
        				dictCurrency.put(dictCostCategory, v.getCurrency());
        			}else {
        				if(!currency.equals(v.getCurrency())) {
        					errorDictList.add(dictCostCategory);
        				}
        			}
        		}
    			List<CostViewDTO> list = mainCostMaps.get(mainId);
    			if(CollUtil.isNotEmpty(list)) {
        			Map<String, String> dictCurrencyMaps = list.stream().filter(l -> dictCurrency.containsKey(l.getCurrency()))
        					.collect(Collectors.toMap(CostViewDTO::getDictCostCategory , CostViewDTO::getCurrency , (c1 , c2) -> c1));
        			for(Map.Entry<String, String> dictCostListMap : dictCurrencyMaps.entrySet()) {
        				String dictCostCategory = dictCostListMap.getKey();
        				if(errorDictList.contains(dictCostCategory)) {
        					continue;
        				}
        				String currency = dictCurrency.get(dictCostCategory);
            			if(!currency.equals(dictCostListMap.getValue())) {
        					errorDictList.add(dictCostCategory);
            			}
        			}
        		}
        		errorTransportNoDictMap.put(key, errorDictList);
        	}
        	
        	for (FmLogisticsBillCostExcelDTO dto : lastSuccessList) {
        		String transportNo = dto.getTransportNo();
        		List<String> errorDictList = errorTransportNoDictMap.get(transportNo);
        		CostViewDTO tmsCfgCostEntity = costNameIdMap.get(dto.getCostName());
        		String dictCostCategory = tmsCfgCostEntity.getDictCostCategory();
				if(CollUtil.isNotEmpty(errorDictList) && errorDictList.contains(dictCostCategory)) {
					dto.setErrorMsg("物流单【" + transportNo + "】"+ DictCostCategoryEnum.getName(dictCostCategory) +"费用分类下的币别不一致");
	                errorList.add(dto);
	                continue;
        		}
        		
        		String mainId = transportNoInfoMap.get(transportNo);
        		TmsCostDetailEntity updateCostDetailEntity = new TmsCostDetailEntity();
        		TmsCostDetailDTO.CostViewDTO costViewDTO = allCostDetailEntityList.stream().filter(v->v.getMainId().equals(mainId) && v.getCostName().equals(dto.getCostName())).findFirst().orElse(null);
                updateCostDetailEntity.setId(costViewDTO.getId());
                updateCostDetailEntity.setCostValue(dto.getCost());
                updateCostDetailEntity.setCurrency(dto.getCurrency());
                updateCostDetailList.add(updateCostDetailEntity);
        	}
        }
        
        tmsFirstMileLogisticService.updateImportCost(updateCostList,updateCostDetailList);
    }
}
