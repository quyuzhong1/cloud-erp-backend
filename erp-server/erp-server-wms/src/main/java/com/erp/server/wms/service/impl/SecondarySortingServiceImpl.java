package com.erp.server.wms.service.impl;

import com.erp.model.wms.dto.renovation.SecondarySortingDTO;
import com.erp.server.wms.service.PickingWaveService;
import com.erp.server.wms.service.SecondarySortingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecondarySortingServiceImpl implements SecondarySortingService {


    @Override
    public SecondarySortingDTO.ScanCodeView scanCode(String code) {
        return null;
    }

    @Override
    public SecondarySortingDTO.ScanSkuView scanSku(String code, String skuCode) {
        return null;
    }

    @Override
    public List<SecondarySortingDTO.BasketDetail> basketDetail(String code, String basketNo) {
        return null;
    }

    @Override
    public void printDistribution(String code) {

    }
}
