package com.erp.server.dmp.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.dto.DwsDbErpDmpSkuSalesReportFDTO;
import com.erp.model.dmp.entity.doris.DwsDbErpDmpSkuSalesReportFEntity;
import com.erp.server.dmp.mapper.doris.DwsDbErpDmpSkuSalesReportFMapper;
import com.erp.server.dmp.service.DwsDbErpDmpSkuSalesReportFService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
                .last(StringUtils.isNotBlank(dto.getConditionSql()), dto.getConditionSql())
                .list();
    }
}
