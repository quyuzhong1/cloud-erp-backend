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
    public List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllAmzSalesBySoOutStock(String calcDate, String orderType) {
        return salesMapper.listAllAmzSalesBySoOutStock(SnapshotTableEnum.getTableName(SO_OUT_STOCK, calcDate), SnapshotTableEnum.getTableName(SO_OUT_STOCK_DETAIL, calcDate), SnapshotTableEnum.getTableName(SO_B2C, calcDate),
                LocalDate.parse(calcDate, DateTimeFormatter.BASIC_ISO_DATE).minusDays(361), orderType);
    }

    @Override
    public List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllAmzSalesBySob2c(String calcDate, String orderType) {
        return salesMapper.listAllAmzSalesBySob2c(SnapshotTableEnum.getTableName(SO_B2C, calcDate), SnapshotTableEnum.getTableName(SO_B2C_DETAIL, calcDate),
                LocalDate.parse(calcDate, DateTimeFormatter.BASIC_ISO_DATE).minusDays(361), orderType);
    }


    @Override
    public List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllOverseasSalesBySob2c(String calcDate, String orderType, List<String> channelIdList, List<String> platforms) {
        return salesMapper.listAllOverseasSalesBySob2c(SnapshotTableEnum.getTableName(SO_B2C, calcDate), SnapshotTableEnum.getTableName(SO_B2C_DETAIL, calcDate),
                LocalDate.parse(calcDate, DateTimeFormatter.BASIC_ISO_DATE).minusDays(361), orderType, channelIdList, platforms);
    }

    @Override
    public List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllOverseasSalesBySoOutStock(String calcDate, String orderType, List<String> channelIdList, List<String> platforms) {
        return salesMapper.listAllOverseasSalesBySoOutStock(SnapshotTableEnum.getTableName(SO_OUT_STOCK, calcDate), SnapshotTableEnum.getTableName(SO_OUT_STOCK_DETAIL, calcDate), SnapshotTableEnum.getTableName(SO_B2C, calcDate),
                LocalDate.parse(calcDate, DateTimeFormatter.BASIC_ISO_DATE).minusDays(361), orderType, channelIdList, platforms);
    }
}
