package com.erp.server.wms.service.impl;

import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.erp.model.scm.dto.excel.PurchasePriceChangeExportExcelDTO;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.excel.DeliveryOrderExportExcelDTO;
import com.erp.rpc.srm.feign.SrmDeliveryOrderFeign;
import com.erp.rpc.wms.feign.SupplierFeign;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.SupplierDeliveryOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 供应商送货单接口实现类
 */
@Slf4j
@Service
public class SupplierDeliveryOrderServiceImpl implements SupplierDeliveryOrderService{

    @Resource
    private SrmDeliveryOrderFeign srmDeliveryFeign;

    @Resource
    private SupplierFeign supplierFeign;

    @Resource
    private CommonService commonService;

    @Override
    public Boolean export(DeliveryOrderDTO.ParamDTO dto, HttpServletResponse response) {
        dto.setSupplierIdList(supplierFeign.listByPurchaseUserId(commonService.getUserInfo().getUid()).stream().map(BaseEntity::getId).collect(Collectors.toList()));
        List<DeliveryOrderExportExcelDTO> resultList = srmDeliveryFeign.getExportList(dto);
        String fileName = "供应商送货单";
        try {
            ExcelUtil.export(fileName, "供应商送货单", resultList, DeliveryOrderExportExcelDTO.class, response);
        } catch (Exception e) {
            log.error("供应商送货单导出失败:",e);
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return true;
    }
}
