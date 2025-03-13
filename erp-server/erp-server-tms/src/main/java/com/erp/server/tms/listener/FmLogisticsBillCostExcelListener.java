package com.erp.server.tms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ConfirmStatusEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO.CostViewDTO;
import com.erp.model.tms.dto.excel.FmLogisticsBillCostExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.DetailReconciliationTypeEnum;
import com.erp.model.tms.enums.DictCostCategoryEnum;
import com.erp.model.tms.enums.LogisticsBillCostTypeEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.service.*;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final FirstMileEstimatedBillService firstMileEstimatedBillService = SpringUtil.getBean(FirstMileEstimatedBillService.class);
    private final TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService = SpringUtil.getBean(TmsFirstMileReconciliationDetailService.class);
    private final TmsCfgCostService tmsCfgCostService = SpringUtil.getBean(TmsCfgCostService.class);
    private final DmpTaskFeign dmpTaskFeign = SpringUtil.getBean(DmpTaskFeign.class);

    @Getter
    private List<FmLogisticsBillCostExcelDTO> dataList = new ArrayList<>();

    @Getter
    private List<FmLogisticsBillCostExcelDTO> errorList = new ArrayList<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(FmLogisticsBillCostExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if(CharSequenceUtil.isAllBlank(excelDTO.getOutstockCode(),excelDTO.getTransportNo(),excelDTO.getBusinessCode())){
            errorMsgList.add("发货单号和业务单号和运单号不能都为空");
        }
        if(StringUtils.isBlank(excelDTO.getCurrency())) {
            errorMsgList.add("币种不能都为空");
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
        List<String> outstockCodeList = dataList.stream().map(FmLogisticsBillCostExcelDTO::getOutstockCode).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> businessCodeList = dataList.stream().map(FmLogisticsBillCostExcelDTO::getBusinessCode).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> transportNoList = dataList.stream().map(FmLogisticsBillCostExcelDTO::getTransportNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillEntityList = tmsFirstMileLogisticService.listBySourceCodeList(businessCodeList, outstockCodeList, transportNoList);
        List<String> mainIdList = logisticsBillEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<LogisticsBillCostEntity> logisticsBillCostEntitieList = logisticsBillCostService.listByLogisticsBillIdList(mainIdList);
        List<String> costIdList = logisticsBillCostEntitieList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<TmsCostDetailDTO.CostViewDTO> allCostDetailEntityList = logisticsBillCostDetailService.listCostByMainIdList(costIdList);
        List<TmsCfgCostEntity> tmsCfgCostEntityList = tmsCfgCostService.list();
        //暂估账单
        List<FirstMileEstimatedBillDTO.View> estimatedBillList = firstMileEstimatedBillService.listByLogisticsBillIds(mainIdList, ConfirmStatusEnum.CONFIRM.getCode());
        //对账单明细
        List<TmsFirstMileReconciliationDetailEntity> reconciliationDetailEntityList = tmsFirstMileReconciliationDetailService.listBySourceIdsAndStatus(mainIdList, null, DetailReconciliationTypeEnum.ACTUAL.getCode());
        List<LogisticsBillCostEntity> updateCostList = new ArrayList<>();
        List<TmsCostDetailEntity> updateCostDetailList = new ArrayList<>();
        for (FmLogisticsBillCostExcelDTO excelDTO : dataList) {
            //校验数据
            List<LogisticsBillEntity> entityList = logisticsBillEntityList.stream().filter(v -> {
                //同时不为空时，匹配来源单号和业务单号
                if (CharSequenceUtil.isAllNotBlank(excelDTO.getOutstockCode(), excelDTO.getBusinessCode(),excelDTO.getTransportNo())) {
                    if (v.getOutstockCode().equals(excelDTO.getOutstockCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                }
                //运单号不为空时，匹配运单号
                if (CharSequenceUtil.isNotBlank(excelDTO.getTransportNo()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                if (CharSequenceUtil.isAllNotBlank(excelDTO.getOutstockCode(), excelDTO.getBusinessCode())) {
                    if (v.getOutstockCode().equals(excelDTO.getOutstockCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode())) {return true;}
                }
                //同时不为空时，匹配来源单号和运单号
                if (CharSequenceUtil.isAllNotBlank(excelDTO.getOutstockCode(),excelDTO.getTransportNo())) {
                    if (v.getOutstockCode().equals(excelDTO.getOutstockCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                }
                //同时不为空时，匹配来源单号和业务单号
                if (CharSequenceUtil.isAllNotBlank(excelDTO.getBusinessCode(),excelDTO.getTransportNo())) {
                    if (v.getBusinessCode().equals(excelDTO.getBusinessCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                }
                //来源单号不为空时，匹配来源单号
                if (CharSequenceUtil.isNotBlank(excelDTO.getOutstockCode()) && v.getOutstockCode().equals(excelDTO.getOutstockCode())) {return true;}
                //业务单号不为空时，匹配业务单号
                if (CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode())) {return true;}
                //运单号不为空时，匹配运单号
                if (CharSequenceUtil.isNotBlank(excelDTO.getTransportNo()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
                return false;
            }).collect(Collectors.toList());
            if (CollUtil.isEmpty(entityList)) {
                excelDTO.setErrorMsg(CharSequenceUtil.format("来源单号【{}】或业务单号【{}】或物流运单号【{}】未匹配到物流单",excelDTO.getOutstockCode(), excelDTO.getBusinessCode(), excelDTO.getTransportNo()));
                errorList.add(excelDTO);
                continue;
            }
            if (entityList.size() > 1) {
                excelDTO.setErrorMsg(CharSequenceUtil.format("来源单号【{}】或业务单号【{}】或物流运单号【{}】存在多条物流单",excelDTO.getOutstockCode(), excelDTO.getBusinessCode(), excelDTO.getTransportNo()));
                errorList.add(excelDTO);
                continue;
            }
            LogisticsBillEntity entity = entityList.get(0);
            if(Objects.isNull(entity)){
                excelDTO.setErrorMsg("未找到物流单");
                errorList.add(excelDTO);
                continue;
            }
            excelDTO.setLogisticsBillId(entity.getId());
            if(StringUtils.isNotBlank(excelDTO.getTransportNo()) && StringUtils.isNotBlank(excelDTO.getOutstockCode())
                &&(!entity.getTransportNo().equals(excelDTO.getTransportNo()) || !entity.getOutstockCode().equals(excelDTO.getOutstockCode()))){
                excelDTO.setErrorMsg("来源单号与运单号不匹配");
                errorList.add(excelDTO);
                continue;
            }
            estimatedBillList.stream().filter(v->v.getLogisticsBillId().equals(entity.getId())).findFirst().ifPresent(v->{
                excelDTO.setErrorMsg("暂估账单已确认，不能更新信息");
                errorList.add(excelDTO);
            });
            reconciliationDetailEntityList.stream().filter(v->v.getSourceId().equals(entity.getId()) && !v.getStatus().equals(ReconciliationStatusEnum.TO_BE_GENERATED.getCode())).findFirst().ifPresent(v->{
                excelDTO.setErrorMsg("实际账单状态{已生成/已确认/已对账/差异确认}，不能更新信息");
                errorList.add(excelDTO);
            });
            LogisticsBillCostEntity costEntity = logisticsBillCostEntitieList.stream().filter(v->v.getLogisticsBillId().equals(entity.getId())).findFirst().orElse(null);
            if(Objects.isNull(costEntity)){
                excelDTO.setErrorMsg("未找到物流费用");
                errorList.add(excelDTO);
                continue;
            }
            excelDTO.setCostId(costEntity.getId());
            if(!(costEntity.getReconciliationStatus().equals(ReconciliationStatusEnum.INVALID.getCode()) ||costEntity.getReconciliationStatus().equals(ReconciliationStatusEnum.TO_BE_GENERATED.getCode()))){
                excelDTO.setErrorMsg("已生成对账单，不能更新信息");
                errorList.add(excelDTO);
                continue;
            }
            updateCostList.add(costEntity);
            TmsCfgCostEntity cfgCostEntity = tmsCfgCostEntityList.stream().filter(v->v.getCostName().equals(excelDTO.getCostName())).findFirst().orElse(null);
            if(Objects.isNull(cfgCostEntity)){
                excelDTO.setErrorMsg(CharSequenceUtil.format("未找到【{}】物流费用配置",excelDTO.getCostName()));
                errorList.add(excelDTO);
                continue;
            }
            excelDTO.setTransportNo(entity.getTransportNo());
        }
        
        List<FmLogisticsBillCostExcelDTO> lastSuccessList = dataList.stream().filter(d -> StringUtils.isBlank(d.getErrorMsg())).collect(Collectors.toList());
        Map<String, List<FmLogisticsBillCostExcelDTO>> transportNoMaps = lastSuccessList.stream().collect(Collectors.groupingBy(FmLogisticsBillCostExcelDTO::getTransportNo));
    	Map<String, String> transportNoInfoMap = logisticsBillCostEntitieList.stream().collect(Collectors.toMap(LogisticsBillCostEntity::getTransportNo, LogisticsBillCostEntity::getId , (v1 , v2) -> v1));
    	Map<String, List<CostViewDTO>> mainCostMaps = allCostDetailEntityList.stream().collect(Collectors.groupingBy(CostViewDTO::getMainId));
    	Map<String, CostViewDTO> costNameIdMap = allCostDetailEntityList.stream().collect(Collectors.toMap(TmsCostDetailDTO.CostViewDTO::getCostName, t -> t , (v1 , v2) -> v1));
        Map<String, TmsCfgCostEntity> costNameMap = tmsCfgCostEntityList.stream().collect(Collectors.toMap(TmsCfgCostEntity::getCostName, t -> t, (v1, v2) -> v1));
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
        			CostViewDTO costViewDTO = costNameIdMap.get(v.getCostName());
                    String dictCostCategory = "";
                    if (Objects.isNull(costViewDTO)) {
                        TmsCfgCostEntity tmsCfgCostEntity = costNameMap.get(v.getCostName());
                        dictCostCategory = Objects.nonNull(tmsCfgCostEntity) ? tmsCfgCostEntity.getDictCostCategory() : CharSequenceUtil.EMPTY;
                    }else {
                        dictCostCategory = costViewDTO.getDictCostCategory();
                    }
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
                TmsCfgCostEntity tmsCfgCostEntity = costNameMap.get(dto.getCostName());
                String dictCostCategory = tmsCfgCostEntity.getDictCostCategory();
				if(CollUtil.isNotEmpty(errorDictList) && errorDictList.contains(dictCostCategory)) {
					dto.setErrorMsg("物流单【" + transportNo + "】"+ DictCostCategoryEnum.getName(dictCostCategory) +"费用分类下的币别不一致");
	                errorList.add(dto);
	                continue;
        		}
        		
        		String mainId = transportNoInfoMap.get(transportNo);
        		TmsCostDetailEntity updateCostDetailEntity = new TmsCostDetailEntity();
        		TmsCostDetailDTO.CostViewDTO costViewDTO = allCostDetailEntityList.stream().filter(v->v.getMainId().equals(mainId) && v.getCostName().equals(dto.getCostName())).findFirst().orElse(null);
                updateCostDetailEntity.setId(Objects.nonNull(costViewDTO) ? costViewDTO.getId() : null);
                updateCostDetailEntity.setMainId(dto.getCostId());
                updateCostDetailEntity.setCfgCostId(tmsCfgCostEntity.getId());
                updateCostDetailEntity.setCostValue(dto.getCost());
                updateCostDetailEntity.setCurrency(dto.getCurrency());
                updateCostDetailEntity.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
                updateCostDetailEntity.setSourceType(tmsCfgCostEntity.getDictCostCategory());
                BigDecimal exchangeRate = BigDecimal.ZERO;
                try {
                    exchangeRate = dmpTaskFeign.getRate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), dto.getCurrency());
                }catch (Exception e){

                }
                updateCostDetailEntity.setExchangeRate(exchangeRate);
                updateCostDetailList.add(updateCostDetailEntity);
        	}
        }
        
        tmsFirstMileLogisticService.updateImportCost(updateCostList,updateCostDetailList);
    }
}
