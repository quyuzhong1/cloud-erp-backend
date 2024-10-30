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
    public List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllAmzSalesBySoOutStock(LocalDate calculationDate, Integer cleanDay) {
        String calcDate = calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        return salesMapper.listAllAmzSalesBySoOutStock(SnapshotTableEnum.getTableName(SO_OUT_STOCK, calcDate), SnapshotTableEnum.getTableName(SO_OUT_STOCK_DETAIL, calcDate), SnapshotTableEnum.getTableName(SO_B2C, calcDate),
                calculationDate.minusDays(cleanDay), calculationDate.minusDays(1));
    }

    @Override
    public List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllAmzSalesBySob2c(LocalDate calculationDate, Integer cleanDay) {
        String calcDate = calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        return salesMapper.listAllAmzSalesBySob2c(SnapshotTableEnum.getTableName(SO_B2C, calcDate), SnapshotTableEnum.getTableName(SO_B2C_DETAIL, calcDate),
                calculationDate.minusDays(cleanDay), calculationDate.minusDays(1));
    }
}
