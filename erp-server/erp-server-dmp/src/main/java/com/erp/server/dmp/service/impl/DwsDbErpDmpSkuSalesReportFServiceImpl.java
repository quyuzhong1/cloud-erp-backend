package com.erp.server.dmp.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.dto.DwsDbErpDmpSkuSalesReportFDTO;
import com.erp.model.dmp.entity.doris.DwsDbErpDmpSkuSalesReportFEntity;
import com.erp.server.dmp.mapper.doris.DwsDbErpDmpSkuSalesReportFMapper;
import com.erp.server.dmp.service.DwsDbErpDmpSkuSalesReportFService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * <p>
 * SKU销量报告 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-06-23
 */
@Slf4j
@Service
public class DwsDbErpDmpSkuSalesReportFServiceImpl extends SuperServiceImpl<DwsDbErpDmpSkuSalesReportFMapper, DwsDbErpDmpSkuSalesReportFEntity> implements DwsDbErpDmpSkuSalesReportFService {


    @DS("doris")
    @Override
    public List<DwsDbErpDmpSkuSalesReportFEntity> reportList(DwsDbErpDmpSkuSalesReportFDTO.RequestListDTO dto) {
        // 根据所有DwsDbErpDmpSkuSalesReportFDTO.RequestListDTO的参数, 判空查询
        return this.lambdaQuery()
                .in(CollectionUtils.isNotEmpty(dto.getSkuIdList()), DwsDbErpDmpSkuSalesReportFEntity::getSkuId, dto.getSkuIdList())
                .in(CollectionUtils.isNotEmpty(dto.getSkuNoList()), DwsDbErpDmpSkuSalesReportFEntity::getSkuNo, dto.getSkuNoList())
                .in(CollectionUtils.isNotEmpty(dto.getFirstCategoryIdList()), DwsDbErpDmpSkuSalesReportFEntity::getFirstCategoryId, dto.getFirstCategoryIdList())
                .in(CollectionUtils.isNotEmpty(dto.getSecondCategoryIdList()), DwsDbErpDmpSkuSalesReportFEntity::getSecondCategoryId, dto.getSecondCategoryIdList())
                .in(CollectionUtils.isNotEmpty(dto.getDictPlatformList()), DwsDbErpDmpSkuSalesReportFEntity::getDictPlatform, dto.getDictPlatformList())
                .in(CollectionUtils.isNotEmpty(dto.getCustomerIdList()), DwsDbErpDmpSkuSalesReportFEntity::getCustomerId, dto.getCustomerIdList())
                .in(CollectionUtils.isNotEmpty(dto.getCountryIdList()), DwsDbErpDmpSkuSalesReportFEntity::getCountryId, dto.getCountryIdList())
                .in(CollectionUtils.isNotEmpty(dto.getPartitionIdList()), DwsDbErpDmpSkuSalesReportFEntity::getPartitionId, dto.getPartitionIdList())
                .in(CollectionUtils.isNotEmpty(dto.getDailySalesType()), DwsDbErpDmpSkuSalesReportFEntity::getDailySalesType, dto.getDailySalesType())
                .in(CollectionUtils.isNotEmpty(dto.getDimension()), DwsDbErpDmpSkuSalesReportFEntity::getDimension, dto.getDimension())
                .in(CollectionUtils.isNotEmpty(dto.getApplicationCategoryIdList()), DwsDbErpDmpSkuSalesReportFEntity::getApplicationCategoryId, dto.getApplicationCategoryIdList())
                .list();
    }
}
