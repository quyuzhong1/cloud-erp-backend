package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
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
import com.erp.model.tms.entity.FirstMileEstimatedBillEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.listener.FirstMileEstimatedBillExcelListener;
import com.erp.server.tms.mapper.FirstMileEstimatedBillMapper;
import com.erp.server.tms.service.FirstMileEstimatedBillService;
import com.erp.server.tms.service.LogisticsBillService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.TmsCostDetailService;
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
            item.setStatusName(ConfirmStatusEnum.getNameByCode(item.getStatus()));
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
                item.setCostTotal(item.getLogisticsCost().add(item.getCustomsClearanceCost()).add(item.getOtherTaxCost()).add(item.getOtherCost()));
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
        if(status.equals(ConfirmStatusEnum.CONFIRMED.getCode())){
            this.lambdaUpdate().set(FirstMileEstimatedBillEntity::getStatus, status).set(FirstMileEstimatedBillEntity::getConfirmTime, LocalDateTime.now()).eq(FirstMileEstimatedBillEntity::getId, id).update();
        }
        if(status.equals(ConfirmStatusEnum.TO_BE_CONFIRM.getCode())){
            this.lambdaUpdate().set(FirstMileEstimatedBillEntity::getStatus, status).set(FirstMileEstimatedBillEntity::getConfirmTime, null).eq(FirstMileEstimatedBillEntity::getId, id).update();
        }

        return BatchResultDTO.success(id, id);
    }

    @Override
    public BaseResultDTO.AddDTO add(String id) {
        FirstMileEstimatedBillEntity entity = new FirstMileEstimatedBillEntity();
        entity.setLogisticsBillId(id);
        entity.setStatus(ConfirmStatusEnum.TO_BE_CONFIRM.getCode());
        save(entity);
        return new BaseResultDTO.AddDTO(entity.getId(), null);
    }

    @Override
    public List<FirstMileEstimatedBillDTO.Tab> tabList() {
        List<FirstMileEstimatedBillDTO.Tab> list = new ArrayList<>();
        list.add(getTabCount(ConfirmStatusEnum.TO_BE_CONFIRM.getCode(), "物流商待确认"));
        list.add(getTabCount(ConfirmStatusEnum.CONFIRMED.getCode(), "物流商已确认"));
        return list;
    }

    private FirstMileEstimatedBillDTO.Tab getTabCount(String status, String tabFlagName) {
        int count = 0;
        if(ConfirmStatusEnum.TO_BE_CONFIRM.getCode().equals(status)){
            count = this.baseMapper.countByParam(status, status);
        }
        if(ConfirmStatusEnum.CONFIRMED.getCode().equals(status)){
            count = this.baseMapper.countByParam(null, status);
        }

        return new FirstMileEstimatedBillDTO.Tab(status, tabFlagName, count);
    }

    @Override
    public void importExcel(MultipartFile excelFile, HttpServletResponse response) {
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
        //头程物流单
        List<FirstMileEstimatedBillEntity> estimatedList = this.baseMapper.selectList(null);
        List<String> logisticsBillIds = estimatedList.stream().map(item -> item.getLogisticsBillId()).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillList = logisticsBillService.listByIds(logisticsBillIds);

        List<FirstMileEstimatedBillExcelDTO> successList = listener.getSuccessList();
        List<FirstMileEstimatedBillExcelDTO> errorList = listener.getErrorList();
        for (FirstMileEstimatedBillExcelDTO dto : successList) {
            if(StringUtils.isBlank(dto.getBusinessCode()) && StringUtils.isBlank(dto.getTransportNo())){
                dto.setErrorMsg("【业务单号】和【物流运单号】不能同时为空");
                errorList.add(dto);
            }
            logisticsBillList.stream().filter(item -> item.getSourceCode().equals(dto.getBusinessCode()));
        }
    }

    @Override
    public void exportExcel(FirstMileEstimatedBillDTO.ExportParam dto, HttpServletResponse response) {
        List<FirstMileEstimatedBillDTO.View> viewList;
        if(! dto.getIds().isEmpty()){
            viewList = baseMapper.listByParamIds(dto.getIds());
        }else {
            viewList = baseMapper.listByParam(dto);
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
