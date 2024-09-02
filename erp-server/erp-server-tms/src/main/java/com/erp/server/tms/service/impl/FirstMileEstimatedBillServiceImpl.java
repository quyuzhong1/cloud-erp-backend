package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ConfirmStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.dto.excel.FirstMileEstimatedBillExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.listener.FirstMileEstimatedBillExcelListener;
import com.erp.server.tms.mapper.FirstMileEstimatedBillMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public PagingVO<FirstMileEstimatedBillDTO.View> paging(PagingDTO<FirstMileEstimatedBillDTO.PagingParam> dto) {
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

        //预计费用
        List<String> logisticsBillIds = records.stream().map(item -> item.getLogisticsBillId()).distinct().collect(Collectors.toList());
        List<FirstMileEstimatedBillDTO.EstimatedCost> estimatedCostList = this.baseMapper.listEstimatedCost(logisticsBillIds);
        Map<String, List<FirstMileEstimatedBillDTO.EstimatedCost>> estimatedCostMap = estimatedCostList.stream().collect(Collectors.groupingBy(item -> item.getLogisticsBillId()));
        for (FirstMileEstimatedBillDTO.View item : records) {
            item.setStatusName(ConfirmStatusEnum.getName(item.getStatus()));
            item.setActualBillStatusName(ReconciliationStatusEnum.getName(item.getActualBillStatus()));
            item.setToCountryName(item.getToCountry());
            item.setToCountry(countryMap.getOrDefault(item.getToCountryName(), ""));
            item.setFeeRuleName(ShippingFeeRuleEnum.getName(item.getFeeRule()));
            item.setCurrencySymbol(StringUtils.isBlank(item.getCurrency()) ? "" : CurrencyEnum.getSymbolByCode(item.getCurrency()));

            //预计费用
            if(estimatedCostMap.containsKey(item.getLogisticsBillId())){
                List<FirstMileEstimatedBillDTO.EstimatedCost> estimatedCosts = estimatedCostMap.get(item.getLogisticsBillId());
                //物流运费
                FirstMileEstimatedBillDTO.EstimatedCost logisticsDTO = estimatedCosts.stream().filter(v -> v.getDictCostCategory().equals(DictCostCategoryEnum.SHIPPING_COST.getCode())).findFirst().orElse(null);
                item.setLogisticsCost(logisticsDTO != null ? logisticsDTO.getCostValue() : BigDecimal.ZERO);
                //报关费
                FirstMileEstimatedBillDTO.EstimatedCost declareDTO = estimatedCosts.stream().filter(v -> v.getDictCostCategory().equals(DictCostCategoryEnum.DECLARE_COST.getCode())).findFirst().orElse(null);
                item.setCustomsClearanceCost(declareDTO != null ? declareDTO.getCostValue() : BigDecimal.ZERO);
                //其它税费
                FirstMileEstimatedBillDTO.EstimatedCost otherTaxDTO = estimatedCosts.stream().filter(v -> v.getDictCostCategory().equals(DictCostCategoryEnum.OTHER_TAX_FEE.getCode())).findFirst().orElse(null);
                item.setOtherTaxCost(otherTaxDTO != null ? otherTaxDTO.getCostValue() : BigDecimal.ZERO);
                //其它费用
                FirstMileEstimatedBillDTO.EstimatedCost otherDTO = estimatedCosts.stream().filter(v -> v.getDictCostCategory().equals(DictCostCategoryEnum.OTHER_COST.getCode())).findFirst().orElse(null);
                item.setOtherCost(otherDTO != null ? otherDTO.getCostValue() : BigDecimal.ZERO);
                //总计
                BigDecimal total = item.getLogisticsCost().add(item.getCustomsClearanceCost()).add(item.getOtherTaxCost()).add(item.getOtherCost());
                item.setCostTotal(total);
            }

            //预计重量
            FirstMileDeliveryDTO.GenerateLogisticReqDTO reqDto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
            reqDto.setIds(Arrays.asList(item.getOutStockId()));
            List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTO = wmsFirstMileDeliveryFeign.getGenerateLogisticDTO(reqDto);
            List<TmsFirstMileLogisticDTO.DeliveryDTO> deliveryDTOList = BeanUtil.copyToList(generateLogisticDTO,TmsFirstMileLogisticDTO.DeliveryDTO.class);
            if(CollectionUtils.isNotEmpty(deliveryDTOList)){
                TmsFirstMileLogisticDTO.DeliveryDTO deliveryDTO = deliveryDTOList.get(0);
                if(CollectionUtils.isNotEmpty(deliveryDTO.getPackingDTOList())){
                    LogisticsChannelEntity channelEntity = logisticsChannelService.getById(item.getLogisticsChannelId());
                    if(Objects.nonNull(channelEntity) && channelEntity.getVolumeSetting() != null && channelEntity.getVolumeSetting() > 0){
                        deliveryDTO.getPackingDTOList().forEach(v -> {
                            v.setVolumeWeight(v.getMultiplySize().divide(BigDecimal.valueOf(channelEntity.getVolumeSetting()), 4, RoundingMode.HALF_UP));
                        });
                    }
                    List<TmsFirstMileLogisticDTO.PackingDTO> packingDTOList = deliveryDTO.getPackingDTOList();
                    BigDecimal actualWeight = packingDTOList.stream().map(v -> BigDecimal.valueOf(new Long(v.getPackageWeight()))).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal volumeWeight = packingDTOList.stream().map(v -> v.getVolumeWeight() == null ? BigDecimal.ZERO : v.getVolumeWeight()).reduce(BigDecimal.ZERO, BigDecimal::add);
                    item.setActualWeight(actualWeight);
                    item.setVolumeWeight(volumeWeight);
                    item.setChargedWeight(actualWeight.max(volumeWeight));
                    item.setWeightUnit("kg");
                }
            }
            item.setTransportStatusName(FmLogisticTrackStatusEnum.getNameByCode(item.getTransportStatus()).getName());
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
    public List<FirstMileEstimatedBillDTO.Tab> tabList() {
        List<FirstMileEstimatedBillDTO.Tab> list = new ArrayList<>();
        list.add(getTabCount(ConfirmStatusEnum.WAIT_CONFIRM.getCode(), "物流商待确认"));
        list.add(getTabCount(ConfirmStatusEnum.CONFIRM.getCode(), "物流商已确认"));
        return list;
    }

    private FirstMileEstimatedBillDTO.Tab getTabCount(String status, String tabFlagName) {
        int count = 0;
        if(ConfirmStatusEnum.WAIT_CONFIRM.getCode().equals(status)){
            count = this.baseMapper.countByParam(ConfirmStatusEnum.WAIT_CONFIRM.getCode(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
        }
        if(ConfirmStatusEnum.CONFIRM.getCode().equals(status)){
            count = this.baseMapper.countByParam(null, ReconciliationStatusEnum.CONFIRMED.getCode());
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
        //物流单信息
        List<FirstMileEstimatedBillDTO.LogisticsInfoDTO> logisticsInfoList = this.baseMapper.listLogisticsInfo();

        List<FirstMileEstimatedBillExcelDTO> successList = listener.getSuccessList();
        List<FirstMileEstimatedBillExcelDTO> errorList = listener.getErrorList();
        for (FirstMileEstimatedBillExcelDTO dto : successList) {
            /*if(StringUtils.isBlank(dto.getBusinessCode()) && StringUtils.isBlank(dto.getTransportNo())){
                dto.setErrorMsg("【业务单号】和【物流运单号】不能同时为空，");
                errorList.add(dto);
                continue;
            }*/
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
            List<FirstMileCostAllocationEntity> costAllocationList = firstMileCostAllocationList.stream().filter(item -> item.getLogisticsBillId().equals(infoDTO.getLogisticsBillId())).collect(Collectors.toList());
            costAllocationList.sort((Comparator.comparing(FirstMileCostAllocationEntity::getReportPeriodMonth).reversed()));
            if(! costAllocationList.isEmpty() && costAllocationList.get(0).getStatus().equals("confirm")){
                dto.setErrorMsg("费用分摊核算【已确认】不允许导入");
                errorList.add(dto);
                continue;
            }
            /*if(!logisticsBillMap.containsKey(dto.getTransportNo())){
                dto.setErrorMsg("物流运单号错误");
                errorList.add(dto);
                continue;
            }*/
            TmsCfgCostEntity tmsCfgCostEntity = tmsCfgCostOptional.get();
            String costId = infoDTO.getCostId();

            TmsCostDetailEntity detailEntity = new TmsCostDetailEntity();
            detailEntity.setCfgCostId(tmsCfgCostEntity.getId());
            detailEntity.setMainId(costId);
            detailEntity.setSourceType(SourceTypeEnum.FIRST_MILE_ESTIMATED.getCode());
            detailEntity.setType("estimated");
            detailEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            detailEntity.setExchangeRate(BigDecimal.ONE);
            detailEntity.setCostValue(dto.getCostValue());
            tmsCostDetailService.save(detailEntity);
        }
        if(! errorList.isEmpty()){
            try {
                String fileName = "头程暂估账单-导入错误" + DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                String excelPath = "excel/firstMileEstimatedBillExport.xlsx";
                new ExcelPrintUtils().patchExport(errorList, response, fileName, excelPath);
            } catch (IOException e) {
                log.error("仓位安全库存导出错误：{}", e);
                return Boolean.FALSE;
            }
        }
        return false;
    }

    @Override
    public void exportExcel(FirstMileEstimatedBillDTO.ExportParam dto, HttpServletResponse response) {
        List<FirstMileEstimatedBillDTO.View> viewList;
        if(dto.getIds() == null || ArrayUtils.isEmpty(dto.getIds().toArray())){
            viewList = baseMapper.listByParam(dto);
        }else {
            viewList = baseMapper.listByParamIds(dto.getIds());
        }
        fillData(viewList);
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        StringBuilder builder = new StringBuilder();
        builder.append("头程暂估账单导出").append(date);
        try {
            new ExcelPrintUtils().patchExport(viewList, response, builder.toString(), "excel/firstMileEstimatedBillExport.xlsx");
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public void removeByLogisticsBillId(String logisticsBillId) {
        if (StrUtil.isNotBlank(logisticsBillId)){
            this.lambdaUpdate().eq(FirstMileEstimatedBillEntity::getLogisticsBillId,logisticsBillId).remove();
        }
    }

    @Override
    public List<FirstMileEstimatedBillDTO.View> listByLogisticsBillIds(List<String> ids, String status) {
        if(ids.isEmpty() && StrUtil.isBlank(status)){
            return Collections.emptyList();
        }
        List<FirstMileEstimatedBillDTO.View> list = baseMapper.listByLogisticsBillIds(ids, status);
        fillData(list);
        return list;
    }
}
