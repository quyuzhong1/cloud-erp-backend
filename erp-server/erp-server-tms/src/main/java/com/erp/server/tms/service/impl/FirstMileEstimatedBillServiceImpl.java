package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.entity.FirstMileEstimatedBillEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.TmsCostDetailEntity;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.mapper.FirstMileEstimatedBillMapper;
import com.erp.server.tms.service.FirstMileEstimatedBillService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.TmsCostDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 头程暂估账单业务类
 * @date 2024-08-16
 * @author tanmujin
 */
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

    @Override
    public PagingVO<FirstMileEstimatedBillDTO.View> paging(PagingDTO<FirstMileEstimatedBillDTO.PagingParam> dto) {
        Page<?> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<FirstMileEstimatedBillDTO.View> pageData = baseMapper.paging(query, dto.getParams());
        fillData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void fillData(List<FirstMileEstimatedBillDTO.View> records) {
        List<String> countryCodeList = records.stream().map(item -> item.getToCountry()).distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(countryCodeList);
        Map<String, String> countryMap = countryList.stream().collect(Collectors.toMap(item -> item.getId(), item2 -> item2.getNameCn()));

        for (FirstMileEstimatedBillDTO.View item : records) {
            item.setStatusName(ConfirmStatusEnum.getNameByCode(item.getStatus()));
            item.setActualBillStatusName(ReconciliationStatusEnum.getName(item.getActualBillStatus()));
            item.setToCountryName(countryMap.get(item.getToCountry()));
            item.setFeeRuleName(ShippingFeeRuleEnum.getName(item.getFeeRule()));

            //预计费用
            List<TmsCostDetailDTO.CostCompareDTO> costCompareList = tmsCostDetailService.getCostCompareListById(item.getLogisticsBillCostId());
            TmsCostDetailDTO.CostCompareDTO logisticsDTO = costCompareList.stream().filter(v -> v.getCostCode().equals(DictCostCategoryEnum.SHIPPING_COST.getCode())).findFirst().orElse(new TmsCostDetailDTO.CostCompareDTO());
            item.setLogisticsCost(logisticsDTO.getEstimatedFee());
            TmsCostDetailDTO.CostCompareDTO declareDTO = costCompareList.stream().filter(v -> v.getCostCode().equals(DictCostCategoryEnum.DECLARE_COST.getCode())).findFirst().orElse(new TmsCostDetailDTO.CostCompareDTO());
            item.setCustomsClearanceCost(declareDTO.getEstimatedFee());
//            TmsCostDetailDTO.CostCompareDTO otherTaxDTO = costCompareList.stream().filter(v -> v.getCostCode().equals(DictCostCategoryEnum.DECLARE_COST.getCode())).findFirst().orElse(new TmsCostDetailDTO.CostCompareDTO());
//            item.setOtherTaxCost(otherTaxDTO.getEstimatedFee());
            TmsCostDetailDTO.CostCompareDTO otherDTO = costCompareList.stream().filter(v -> v.getCostCode().equals(DictCostCategoryEnum.OTHER_COST.getCode())).findFirst().orElse(new TmsCostDetailDTO.CostCompareDTO());
            item.setOtherCost(otherDTO.getEstimatedFee());
            item.setCostTotal(item.getLogisticsCost().add(item.getCustomsClearanceCost()).add(item.getOtherTaxCost()).add(item.getOtherCost()));

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
                    /*List<TmsFirstMileLogisticDTO.PackingDTO> packingDTOList = deliveryDTO.getPackingDTOList();
                    item.setActualWeight();
                    item.setVolumeWeight();
                    item.setChargedWeight();
                    item.setWeightUnit();*/
                }
            }
            item.setTransportStatusName(FmLogisticTrackStatusEnum.getNameByCode(item.getTransportStatus()).getName());
        }
    }

    @Override
    public BatchResultDTO updateStatus(String id, String status) {
        this.updateStatus(id, status);
        return BatchResultDTO.success();
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
        return Collections.emptyList();
    }

    @Override
    public void importExcel(MultipartFile excelFile, HttpServletResponse response) {

    }

    @Override
    public void exportExcel(FirstMileEstimatedBillDTO.ExportParam dto) {
        /*if(! dto.getIds().isEmpty()){
            List<FirstMileEstimatedBillEntity> list = this.listByParamIds(dto.getIds());
            List<FirstMileEstimatedBillDTO.View> viewList = BeanMapper.copyList(list, FirstMileEstimatedBillDTO.View.class);
            fillData(viewList);
        }else {
            baseMapper.listAll(dto);
        }*/
    }

    @Override
    public void removeByLogisticsBillId(String logisticsBillId) {
        if (StrUtil.isNotBlank(logisticsBillId)){
            this.lambdaUpdate().eq(FirstMileEstimatedBillEntity::getLogisticsBillId,logisticsBillId).remove();
        }
    }
}
