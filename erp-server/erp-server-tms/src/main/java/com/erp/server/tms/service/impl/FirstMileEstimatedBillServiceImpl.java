package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ConfirmStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO.CostViewDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO.UpdateDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.dto.excel.FirstMileEstimatedBillExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.listener.FirstMileEstimatedBillExcelListener;
import com.erp.server.tms.mapper.FirstMileEstimatedBillMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_FM_ESTIMATED_BILL;

/**
 * 头程暂估账单业务类
 * @date 2024-08-16
 * @author tanmujin
 */
@Slf4j
@Service
public class FirstMileEstimatedBillServiceImpl extends SuperServiceImpl<FirstMileEstimatedBillMapper, FirstMileEstimatedBillEntity> implements FirstMileEstimatedBillService {

    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private TmsCostDetailService tmsCostDetailService;
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;
    @Resource
    private LogisticsChannelService logisticsChannelService;
    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private TmsCfgCostService tmsCfgCostService;
    @Resource
    private FirstMileCostAllocationService firstMileCostAllocationService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private TmsFirstMileReconciliationService tmsFirstMileReconciliationService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private LogisticsBillCostService logisticsBillCostService;
    @Override
    public PagingVO<FirstMileEstimatedBillDTO.View> paging(PagingDTO<FirstMileEstimatedBillDTO.PagingParam> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<?> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<FirstMileEstimatedBillDTO.View> pageData = baseMapper.paging(query, dto.getParams());
        fillData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void fillData(List<FirstMileEstimatedBillDTO.View> records) {
        if(records.isEmpty()){
            return;
        }
        List<String> countryCodeList = records.stream().map(item -> item.getToCountry()).distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByNames(countryCodeList);
        Map<String, String> countryMap = countryList.stream().collect(Collectors.toMap(item -> item.getNameCn(), item2 -> item2.getId()));

        //物流信息
        List<String> logisticsBillIds = records.stream().map(item -> item.getLogisticsBillId()).distinct().collect(Collectors.toList());
        List<TmsFirstMileLogisticDTO.ReconciliationDTO> reconciliationDTOList = tmsFirstMileReconciliationService.listReconciliationAndCostByBillIds(logisticsBillIds);        //预计费用
        List<FirstMileEstimatedBillDTO.EstimatedCost> estimatedCostList = this.baseMapper.listEstimatedCost(logisticsBillIds);
        Map<String, List<FirstMileEstimatedBillDTO.EstimatedCost>> estimatedCostMap = estimatedCostList.stream().collect(Collectors.groupingBy(item -> item.getLogisticsBillId()));
        List<String> currencyIds = estimatedCostList.stream().map(FirstMileEstimatedBillDTO.EstimatedCost::getCurrency).collect(Collectors.toList());
		Map<String, String> idSymbolMap = FeignQuery.getByIds(DictCurrencyEntity.class, currencyIds)
        	.stream().collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getSymbol));

        List<String> outStockIds = records.stream().map(FirstMileEstimatedBillDTO.View::getOutStockId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        FirstMileDeliveryDTO.GenerateLogisticReqDTO reqDto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
        reqDto.setIds(outStockIds);
        List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTO = wmsFirstMileDeliveryFeign.getGenerateLogisticDTO(reqDto);

		Map<String, BigDecimal> rateMap = new HashMap<>();
        for (FirstMileEstimatedBillDTO.View item : records) {
            item.setStatusName(ConfirmStatusEnum.getName(item.getStatus()));
            item.setToCountryName(item.getToCountry());
            item.setToCountry(countryMap.getOrDefault(item.getToCountryName(), ""));
            item.setFeeRuleName(ShippingFeeRuleEnum.getName(item.getFeeRule()));
            //对账单信息填充
            TmsFirstMileLogisticDTO.ReconciliationDTO reconciliationDTO = reconciliationDTOList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getLogisticsBillId()) && Objects.equals(e.getLogisticsBillId(), item.getLogisticsBillId())).findFirst().orElse(null);
            if (Objects.nonNull(reconciliationDTO)){
                item.setActualBillStatus(reconciliationDTO.getReconciliationStatus());
                item.setActualBillStatusName(ReconciliationStatusEnum.getName(item.getActualBillStatus()));
                item.setCurrency(reconciliationDTO.getCurrency());
                item.setCurrencySymbol(StringUtils.isBlank(item.getCurrency()) ? "" : CurrencyEnum.getSymbolByCode(item.getCurrency()));
            }
            //预计费用
            if(estimatedCostMap.containsKey(item.getLogisticsBillId())){
                List<FirstMileEstimatedBillDTO.EstimatedCost> estimatedCosts = estimatedCostMap.get(item.getLogisticsBillId());
                BigDecimal total = BigDecimal.ZERO;
                BigDecimal rate = BigDecimal.ONE;
                String date = item.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                //物流运费
                FirstMileEstimatedBillDTO.EstimatedCost logisticsDTO = estimatedCosts.stream().filter(v -> v.getDictCostCategory().equals(DictCostCategoryEnum.SHIPPING_COST.getCode())).findFirst().orElse(null);
                item.setLogisticsCost(logisticsDTO != null ? logisticsDTO.getCostValue() : BigDecimal.ZERO);
                if(logisticsDTO != null) {
                	String currency = logisticsDTO.getCurrency();
                	item.setLogisticsCostCurrency(currency);
					item.setLogisticsCostCurrencySymbol(idSymbolMap.get(currency));
                	
					if(!"CNY".equals(currency)) {
						String key = date + "_" + currency;
	            		rate = rateMap.get(key);
	            		if(rate == null){
	            			rate = dmpTaskFeign.getRate(date, currency);
	            			if(rate == null) {
	            				rate = BigDecimal.ONE;
//	            				throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
	            			}
	    	            }
	            		rateMap.put(key, rate);
	                	total = total.add(logisticsDTO.getCostValue().multiply(rate));
					}else {
						total = total.add(logisticsDTO.getCostValue());
					}
                }
                //报关费
                FirstMileEstimatedBillDTO.EstimatedCost declareDTO = estimatedCosts.stream().filter(v -> v.getDictCostCategory().equals(DictCostCategoryEnum.DECLARE_COST.getCode())).findFirst().orElse(null);
                item.setCustomsClearanceCost(declareDTO != null ? declareDTO.getCostValue() : BigDecimal.ZERO);
                if(declareDTO != null) {
                	String currency = declareDTO.getCurrency();
                	item.setCustomsClearanceCostCurrency(currency);
					item.setCustomsClearanceCostCurrencySymbol(idSymbolMap.get(currency));
                	
                	if(!"CNY".equals(currency)) {
						String key = date + "_" + currency;
	            		rate = rateMap.get(key);
	            		if(rate == null){
	            			rate = dmpTaskFeign.getRate(date, currency);
	            			if(rate == null) {
	            				rate = BigDecimal.ONE;
//	            				throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
	            			}
	    	            }
	            		rateMap.put(key, rate);
	                	total = total.add(declareDTO.getCostValue().multiply(rate));
					}else {
						total = total.add(declareDTO.getCostValue());
					}
                }
                //其它税费
                FirstMileEstimatedBillDTO.EstimatedCost otherTaxDTO = estimatedCosts.stream().filter(v -> v.getDictCostCategory().equals(DictCostCategoryEnum.OTHER_TAX_FEE.getCode())).findFirst().orElse(null);
                item.setOtherTaxCost(otherTaxDTO != null ? otherTaxDTO.getCostValue() : BigDecimal.ZERO);
                if(otherTaxDTO != null) {
                	String currency = otherTaxDTO.getCurrency();
                	item.setOtherTaxCostCurrency(currency);
					item.setOtherTaxCostCurrencySymbol(idSymbolMap.get(currency));
                	
                	if(!"CNY".equals(currency)) {
						String key = date + "_" + currency;
	            		rate = rateMap.get(key);
	            		if(rate == null){
	            			rate = dmpTaskFeign.getRate(date, currency);
	            			if(rate == null) {
	            				rate = BigDecimal.ONE;
//	            				throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
	            			}
	    	            }
	            		rateMap.put(key, rate);
	                	total = total.add(otherTaxDTO.getCostValue().multiply(rate));
					}else {
						total = total.add(otherTaxDTO.getCostValue());
					}
                }
                //其它费用
                FirstMileEstimatedBillDTO.EstimatedCost otherDTO = estimatedCosts.stream().filter(v -> v.getDictCostCategory().equals(DictCostCategoryEnum.OTHER_COST.getCode())).findFirst().orElse(null);
                item.setOtherCost(otherDTO != null ? otherDTO.getCostValue() : BigDecimal.ZERO);
                if(otherDTO != null) {
                	String currency = otherDTO.getCurrency();
                	item.setOtherCostCurrency(currency);
					item.setOtherCostCurrencySymbol(idSymbolMap.get(currency));
                	
                	if(!"CNY".equals(currency)) {
						String key = date + "_" + currency;
	            		rate = rateMap.get(key);
	            		if(rate == null){
	            			rate = dmpTaskFeign.getRate(date, currency);
	            			if(rate == null) {
	            				rate = BigDecimal.ONE;
//	            				throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
	            			}
	    	            }
	            		rateMap.put(key, rate);
	                	total = total.add(otherDTO.getCostValue().multiply(rate));
					}else {
						total = total.add(otherDTO.getCostValue());
					}
                }
                item.setCostTotal(total.setScale(4, RoundingMode.DOWN));
                
                item.setCostTotalStr(item.getCostTotalCurrencySymbol() + item.getCostTotal());
                item.setLogisticsCostStr(item.getLogisticsCostCurrencySymbol() + item.getLogisticsCost());
                item.setCustomsClearanceCostStr(item.getCustomsClearanceCostCurrencySymbol() + item.getCustomsClearanceCost());
                item.setOtherTaxCostStr(item.getOtherTaxCostCurrencySymbol() + item.getOtherTaxCost());
                item.setOtherCostStr(item.getOtherCostCurrencySymbol() + item.getOtherCost());
            }

            //预计重量
            FirstMileDeliveryDTO.GenerateLogisticDTO deliveryDTO = generateLogisticDTO.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getOutstockId())).findFirst().orElse(null);
            if(Objects.nonNull(deliveryDTO)){
                if(CollectionUtils.isNotEmpty(deliveryDTO.getPackingDTOList())){
                    LogisticsChannelEntity channelEntity = logisticsChannelService.getById(item.getLogisticsChannelId());
                    if(Objects.nonNull(channelEntity) && channelEntity.getVolumeSetting() != null && channelEntity.getVolumeSetting() > 0){
                        deliveryDTO.getPackingDTOList().forEach(v -> {
                            v.setVolumeWeight(v.getMultiplySize().divide(BigDecimal.valueOf(channelEntity.getVolumeSetting()), 4, RoundingMode.HALF_UP));
                        });
                    }
                    List<WmsCartonDetailDTO.ListPackingDetailDTO> packingDTOList = deliveryDTO.getPackingDTOList();
                    BigDecimal actualWeight = packingDTOList.stream().filter(v -> Objects.nonNull(v.getPackageWeight())).map(v ->  v.getPackageWeight().setScale(BigDecimal.ROUND_DOWN, RoundingMode.CEILING)).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal volumeWeight = packingDTOList.stream().map(v -> v.getVolumeWeight() == null ? BigDecimal.ZERO : v.getVolumeWeight()).reduce(BigDecimal.ZERO, BigDecimal::add);
                    item.setActualWeight(actualWeight);
                    item.setVolumeWeight(volumeWeight);
                    item.setChargedWeight(actualWeight.max(volumeWeight));
                    item.setWeightUnit("kg");
                }
            }
            FmLogisticTrackStatusEnum nameByCode = FmLogisticTrackStatusEnum.getNameByCode(item.getTransportStatus());
            item.setTransportStatusName(Objects.nonNull(nameByCode) ? nameByCode.getName() : CharSequenceUtil.EMPTY);
        }
    }

    @Override
    public BatchResultDTO updateStatus(String id, String status) {
        if(status.equals(ConfirmStatusEnum.CONFIRM.getCode())){
            this.lambdaUpdate().set(FirstMileEstimatedBillEntity::getStatus, status).set(FirstMileEstimatedBillEntity::getConfirmTime, LocalDateTime.now()).eq(FirstMileEstimatedBillEntity::getId, id).update();
        }
        if(status.equals(ConfirmStatusEnum.WAIT_CONFIRM.getCode())){
            this.lambdaUpdate().set(FirstMileEstimatedBillEntity::getStatus, status).set(FirstMileEstimatedBillEntity::getConfirmTime, null).eq(FirstMileEstimatedBillEntity::getId, id).update();
        }

        return BatchResultDTO.success(id, id);
    }

    @Override
    public BaseResultDTO.AddDTO add(String id) {
        FirstMileEstimatedBillEntity entity = new FirstMileEstimatedBillEntity();
        entity.setLogisticsBillId(id);
        entity.setStatus(ConfirmStatusEnum.WAIT_CONFIRM.getCode());
        save(entity);
        return new BaseResultDTO.AddDTO(entity.getId(), null);
    }

    @Override
    public List<FirstMileEstimatedBillDTO.Tab> tabList(FirstMileEstimatedBillDTO.PagingParam dto) {
        List<FirstMileEstimatedBillDTO.Tab> list = new ArrayList<>();
        list.add(getTabCount(ConfirmStatusEnum.WAIT_CONFIRM.getCode(), "物流商待确认", dto.getPermissionSql()));
        list.add(getTabCount(ConfirmStatusEnum.CONFIRM.getCode(), "物流商已确认", dto.getPermissionSql()));
        return list;
    }

    private FirstMileEstimatedBillDTO.Tab getTabCount(String status, String tabFlagName, String permissionSql) {
        int count = 0;
        if(ConfirmStatusEnum.WAIT_CONFIRM.getCode().equals(status)){
            count = this.baseMapper.countByParam(ConfirmStatusEnum.WAIT_CONFIRM.getCode(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(),permissionSql);
        }
        if(ConfirmStatusEnum.CONFIRM.getCode().equals(status)){
            count = this.baseMapper.countByParam(null, ReconciliationStatusEnum.CONFIRMED.getCode(), permissionSql);
        }

        return new FirstMileEstimatedBillDTO.Tab(status, tabFlagName, count);
    }

    @Override
    public boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        FirstMileEstimatedBillExcelListener listener = new FirstMileEstimatedBillExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), FirstMileEstimatedBillExcelDTO.class, listener).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        if (listener.getDataList().isEmpty()) {
            throw new ServiceException(ApiError.ERROR_95123, "基础数据");
        }
        //物流费用配置
        List<TmsCfgCostEntity> tmsCfgCostList = tmsCfgCostService.lambdaQuery().eq(TmsCfgCostEntity::getDictCostAttribution, DictCostAttributionEnum.FIRST_MILE.getCode()).list();
        //物流费用分摊
        List<FirstMileCostAllocationEntity> firstMileCostAllocationList = firstMileCostAllocationService.list();

        List<FirstMileEstimatedBillExcelDTO> successList = listener.getSuccessList();
        List<FirstMileEstimatedBillExcelDTO> errorList = listener.getErrorList();

        List<String> successBusCodeList = successList.stream().filter(v -> StringUtils.isNotBlank(v.getBusinessCode())).map(FirstMileEstimatedBillExcelDTO::getBusinessCode).collect(Collectors.toList());
        //业务单号查询
        List<String> outstockIdList = new ArrayList<>();
        Map<String, String> outstockIdBusinessCodeMap =new HashMap<>();
        List<FirstMileDeliveryDTO.BusinessDTO> businessDTOList = wmsFirstMileDeliveryFeign.getDeliveryCodeByBusinessCodes(successBusCodeList);
        if(CollectionUtils.isNotEmpty(businessDTOList)){
            outstockIdBusinessCodeMap = businessDTOList.stream().distinct().collect(Collectors.toMap(FirstMileDeliveryDTO.BusinessDTO::getCode, FirstMileDeliveryDTO.BusinessDTO::getBusinessCode,(existingValue, newValue) -> existingValue));
        }
        //物流单信息
        List<FirstMileEstimatedBillDTO.LogisticsInfoDTO> logisticsInfoList = this.baseMapper.listLogisticsInfo(outstockIdList);
        if(CollectionUtils.isNotEmpty(logisticsInfoList)){
            for (FirstMileEstimatedBillDTO.LogisticsInfoDTO logisticsInfoDTO : logisticsInfoList) {
                if(outstockIdBusinessCodeMap.containsKey(logisticsInfoDTO.getSourceCode())){
                    logisticsInfoDTO.setBusinessCode(outstockIdBusinessCodeMap.get(logisticsInfoDTO.getSourceCode()));
                }
            }
        }
        for (FirstMileEstimatedBillExcelDTO dto : successList) {
            Optional<FirstMileEstimatedBillDTO.LogisticsInfoDTO> existBusinessCodeOptional = logisticsInfoList.stream().filter(item -> item.getBusinessCode().equals(dto.getBusinessCode())).findFirst();
            if(StringUtils.isNotBlank(dto.getBusinessCode()) && !existBusinessCodeOptional.isPresent()){
                dto.setErrorMsg("该业务单号所在的物流单未下推暂估账单，");
                errorList.add(dto);
                continue;
            }
            Optional<FirstMileEstimatedBillDTO.LogisticsInfoDTO> existTransportNoOptional = logisticsInfoList.stream().filter(item -> item.getTransportNo().equals(dto.getTransportNo())).findFirst();
            if(StringUtils.isNotBlank(dto.getTransportNo()) && !existTransportNoOptional.isPresent()){
                dto.setErrorMsg("该物流单号未下推暂估账单，");
                errorList.add(dto);
                continue;
            }
            FirstMileEstimatedBillDTO.LogisticsInfoDTO infoDTO = logisticsInfoList.stream().filter(item -> item.getTransportNo().equals(dto.getTransportNo())).findFirst().orElse(null);
//            if (!dto.getCurrency().equals(infoDTO.getCurrency())){
//                dto.setErrorMsg("导入币种与物流币种不一致，");
//                errorList.add(dto);
//                continue;
//            }
            if(! infoDTO.getEstimatedStatus().equals(ConfirmStatusEnum.WAIT_CONFIRM.getCode())){
                dto.setErrorMsg("仅暂估账单状态为【待确认】允许导入费用，");
                errorList.add(dto);
                continue;
            }
            if(! infoDTO.getActualStatus().equals(ReconciliationStatusEnum.TO_BE_GENERATED.getCode())){
                dto.setErrorMsg("仅实际账单状态为【待生成】允许导入费用，");
                errorList.add(dto);
                continue;
            }
            Optional<TmsCfgCostEntity> tmsCfgCostOptional = tmsCfgCostList.stream().filter(item -> item.getCostName().equals(dto.getCostName().trim())).findFirst();
            if(! tmsCfgCostOptional.isPresent()){
                dto.setErrorMsg("费用名称错误，");
                errorList.add(dto);
                continue;
            }
            List<FirstMileCostAllocationEntity> costAllocationList = firstMileCostAllocationList.stream().filter(item -> item.getLogisticsBillId().equals(infoDTO.getLogisticsBillId()) && Objects.nonNull(item.getReportPeriodMonth())).collect(Collectors.toList());
            costAllocationList.sort((Comparator.comparing(FirstMileCostAllocationEntity::getReportPeriodMonth).reversed()));
            if(! costAllocationList.isEmpty() && costAllocationList.get(0).getStatus().equals("confirm")){
                dto.setErrorMsg("费用分摊核算【已确认】不允许导入");
                errorList.add(dto);
            }
        }
        
        List<FirstMileEstimatedBillExcelDTO> lastSuccessList = successList.stream().filter(s -> StringUtils.isBlank(s.getErrorMsg())).collect(Collectors.toList());
        List<String> costIds = lastSuccessList.stream().map(FirstMileEstimatedBillExcelDTO::getCostId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        Map<String, List<FirstMileEstimatedBillExcelDTO>> sourceCodeMap = lastSuccessList.stream().collect(Collectors.groupingBy(FirstMileEstimatedBillExcelDTO::getSourceCode));
        Map<String, String> sourceCodeInfoMap = logisticsInfoList.stream().filter(l -> sourceCodeMap.keySet().contains(l.getSourceCode()))
    			.collect(Collectors.toMap(FirstMileEstimatedBillDTO.LogisticsInfoDTO::getSourceCode, FirstMileEstimatedBillDTO.LogisticsInfoDTO::getCostId , (v1 , v2) -> v1));
        Map<String, TmsCfgCostEntity> costNameIdMap = tmsCfgCostList.stream().collect(Collectors.toMap(TmsCfgCostEntity::getCostName, t -> t , (v1 , v2) -> v1));
        if(CollUtil.isNotEmpty(lastSuccessList)) {
        	Map<String, List<String>> errorSourceCodeDictMap = new HashMap<>();
        	Map<String, List<CostViewDTO>> mainCostMaps = tmsCostDetailService.listCostByMainIdList(costIds).stream()
        			.collect(Collectors.groupingBy(CostViewDTO::getMainId));
        	for(Map.Entry<String, List<FirstMileEstimatedBillExcelDTO>> sourceCodeMap1 : sourceCodeMap.entrySet()) {
        		String key = sourceCodeMap1.getKey();
        		String mainId = sourceCodeInfoMap.get(key);
        		Map<String, String> dictCurrency = new HashMap<>();
        		List<FirstMileEstimatedBillExcelDTO> value = sourceCodeMap1.getValue();
        		List<String> errorDictList = errorSourceCodeDictMap.get(key);
        		if(errorDictList == null) {
        			errorDictList = new ArrayList<>();
        		}
        		for(FirstMileEstimatedBillExcelDTO v : value) {
        			TmsCfgCostEntity tmsCfgCostEntity = costNameIdMap.get(v.getCostName());
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
        		errorSourceCodeDictMap.put(key, errorDictList);
        	}
        	
        	Map<String, List<TmsCostDetailDTO.UpdateDTO>> updateMaps = new HashMap<>();
        	for (FirstMileEstimatedBillExcelDTO dto : lastSuccessList) {
        		String sourceCode = dto.getSourceCode();
        		List<String> errorDictList = errorSourceCodeDictMap.get(sourceCode);
        		TmsCfgCostEntity tmsCfgCostEntity = costNameIdMap.get(dto.getCostName());
        		String dictCostCategory = tmsCfgCostEntity.getDictCostCategory();
				if(CollUtil.isNotEmpty(errorDictList) && errorDictList.contains(dictCostCategory)) {
					dto.setErrorMsg("来源单号【" + sourceCode + "】"+ DictCostCategoryEnum.getName(dictCostCategory) +"费用分类下的币别不一致");
	                errorList.add(dto);
	                continue;
        		}
        		
        		String mainId = sourceCodeInfoMap.get(sourceCode);
        		List<UpdateDTO> list = updateMaps.get(mainId);
        		if(CollUtil.isEmpty(list)) {
        			list = new ArrayList<>();
        		}
        		TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                updateDTO.setCostValue(dto.getCostValue());
                updateDTO.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
                updateDTO.setCfgCostId(tmsCfgCostEntity.getId());
                updateDTO.setSourceType(SourceTypeEnum.FIRST_MILE_ESTIMATED.getCode());
                updateDTO.setCurrency(dto.getCurrency());
				updateDTO.setDictCostCategory(dictCostCategory);
                list.add(updateDTO);
                updateMaps.put(mainId, list);
        	}
        	for(Map.Entry<String, List<TmsCostDetailDTO.UpdateDTO>> updateMap : updateMaps.entrySet()) {
        		tmsCostDetailService.batchUpdate(updateMap.getValue(), updateMap.getKey(), DictCostAttributionEnum.FIRST_MILE, false);
        	}
        }
        
        
        if(! errorList.isEmpty()){
            try {
                String fileName = "头程暂估账单-导入错误" + DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                String excelPath = "excel/firstMileEstimatedBillImportError.xlsx";
                new ExcelPrintUtils().patchExport(errorList, response, fileName, excelPath);
            } catch (IOException e) {
                log.error("仓位安全库存导出错误：{}", e);
                return Boolean.FALSE;
            }
        }
        return true;
    }

    @Override
    public void exportExcel(FirstMileEstimatedBillDTO.ExportParam dto) {
        downloadTaskFeign.saveDownloadTask("头程暂估账单导出", EXPORT_TMS_FM_ESTIMATED_BILL.getCode(), dto);
    }

    @Override
    public void removeByLogisticsBillId(String logisticsBillId) {
        if (CharSequenceUtil.isNotBlank(logisticsBillId)){
            this.lambdaUpdate().eq(FirstMileEstimatedBillEntity::getLogisticsBillId,logisticsBillId).remove();
        }
    }

    @Override
    public List<FirstMileEstimatedBillDTO.View> listByLogisticsBillIds(List<String> ids, String status) {
        if(ids.isEmpty() && CharSequenceUtil.isBlank(status)){
            return Collections.emptyList();
        }
        List<FirstMileEstimatedBillDTO.View> list = baseMapper.listByLogisticsBillIds(ids, status);
        fillData(list);
        return list;
    }

    @Override
    public List<FirstMileEstimatedBillDTO.View> listEstimatedDetail(List<String> billIds, String status) {
        if(billIds.isEmpty() && CharSequenceUtil.isBlank(status)){
            return Collections.emptyList();
        }
        List<FirstMileEstimatedBillDTO.View> list = baseMapper.listByLogisticsBillIds(billIds, status);
        //物流单
        List<LogisticsBillCostDTO.CostDetailDTO> costDetailDTOS = logisticsBillCostService.listCostDetailByBillAndReconciliationIds(billIds,null,LogisticsBillCostTypeEnum.ESTIMATED.getCode());
        if (org.springframework.util.CollectionUtils.isEmpty(costDetailDTOS)) {
            return Collections.emptyList();
        }
        //对账单明细中的费用项进行重置
        list.forEach(e -> {
            List<LogisticsBillCostDTO.CostDetailDTO> collect = costDetailDTOS.stream().filter(f -> e.getLogisticsBillId().equals(f.getLogisticsBillId())).collect(Collectors.toList());
            e.setLogisticsCostCurrency(CurrencyEnum.CNY.getCurrencyCode());
            e.setLogisticsCost(collect.stream().filter(f -> f.getDictCostCategory().equals(AllocationFeeTypeEnum.SHIPPING_COST.getCode()) && f.getIsAllocate()).map(f -> MathUtil.multiplyWithFour(f.getCostValue(),f.getExchangeRate())).reduce(BigDecimal.ZERO,BigDecimal::add));
            e.setCustomsClearanceCostCurrency(CurrencyEnum.CNY.getCurrencyCode());
            e.setCustomsClearanceCost(collect.stream().filter(f -> f.getDictCostCategory().equals(AllocationFeeTypeEnum.DECLARE_COST.getCode()) && f.getIsAllocate()).map(f -> MathUtil.multiplyWithFour(f.getCostValue(),f.getExchangeRate())).reduce(BigDecimal.ZERO,BigDecimal::add));
            e.setOtherCostCurrency(CurrencyEnum.CNY.getCurrencyCode());
            e.setOtherCost(collect.stream().filter(f -> f.getDictCostCategory().equals(AllocationFeeTypeEnum.OTHER_COST.getCode()) && f.getIsAllocate()).map(f -> MathUtil.multiplyWithFour(f.getCostValue(),f.getExchangeRate())).reduce(BigDecimal.ZERO,BigDecimal::add));
            e.setOtherTaxCostCurrency(CurrencyEnum.CNY.getCurrencyCode());
            e.setOtherTaxCost(collect.stream().filter(f -> f.getDictCostCategory().equals(AllocationFeeTypeEnum.OTHER_TAX_FEE.getCode()) && f.getIsAllocate()).map(f -> MathUtil.multiplyWithFour(f.getCostValue(),f.getExchangeRate())).reduce(BigDecimal.ZERO,BigDecimal::add));
        });
        return list;
    }
}
