package com.erp.server.mrp.calculation.service.impl;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.SnapshotTableEnum;
import com.erp.server.mrp.calculation.service.SalesService;
import com.erp.server.mrp.mapper.SalesMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.erp.model.mrp.enums.SnapshotTableEnum.*;

@Service
public class SalesServiceImpl implements SalesService {
    @Resource
    private SalesMapper salesMapper;

    @Override
    public List<ReplenishmentResultDTO.SalesInfoDTO> listSalesBySob2c(ReplenishmentResultDTO replenishmentResult) {
        String calcDate = replenishmentResult.getReplenishmentDetail().getCalcDate();
        return salesMapper.listSalesBySob2c(replenishmentResult.getReplenishment().getSkuId(), replenishmentResult.getReplenishment().getShopId(),
                SnapshotTableEnum.getTableName(SO_B2C, calcDate), SnapshotTableEnum.getTableName(SO_B2C_DETAIL, calcDate),
                LocalDate.parse(calcDate, DateTimeFormatter.BASIC_ISO_DATE).minusDays(360));
    }

    @Override
    public List<ReplenishmentResultDTO.SalesInfoDTO> listSalesBySoOutStock(ReplenishmentResultDTO replenishmentResult) {
        String calcDate = replenishmentResult.getReplenishmentDetail().getCalcDate();
        return salesMapper.listSalesBySoOutStock(replenishmentResult.getReplenishment().getSkuId(), replenishmentResult.getReplenishment().getShopId(),
                SnapshotTableEnum.getTableName(SO_OUT_STOCK, calcDate), SnapshotTableEnum.getTableName(SO_OUT_STOCK_DETAIL, calcDate), SnapshotTableEnum.getTableName(SO_B2C, calcDate),
                LocalDate.parse(calcDate, DateTimeFormatter.BASIC_ISO_DATE).minusDays(360));
    }
}
