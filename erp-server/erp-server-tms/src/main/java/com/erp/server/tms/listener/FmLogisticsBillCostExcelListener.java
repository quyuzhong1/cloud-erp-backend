package com.erp.server.tms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.entity.BaseEntity;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.dto.excel.FmLogisticsBillCostExcelDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.TmsCostDetailEntity;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.TmsCostDetailService;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
                TmsCostDetailEntity updateCostDetailEntity = new TmsCostDetailEntity();
                updateCostDetailEntity.setId(costViewDTO.getId());
                updateCostDetailEntity.setCostValue(excelDTO.getCost());
                updateCostDetailList.add(updateCostDetailEntity);
            }
        }
        tmsFirstMileLogisticService.updateImportCost(updateCostList,updateCostDetailList);
    }
}
