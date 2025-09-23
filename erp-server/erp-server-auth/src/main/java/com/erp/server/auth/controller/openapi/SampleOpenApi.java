package com.erp.server.auth.controller.openapi;

import com.erp.rpc.oms.feign.ExhibitionOrderFeign;
import com.erp.rpc.oms.feign.OmsDropDownFeign;
import com.erp.rpc.wms.feign.SampleFeign;
import com.erp.server.auth.config.OpenApi;
import javax.annotation.Resource;

/**
 * 样品管理
 * @author jack
 * @since 2025-09-10
 */
@OpenApi
public class SampleOpenApi {

    @Resource
    private SampleFeign sampleFeign;

    @Resource
    private ExhibitionOrderFeign exhibitionOrderFeign;

    @Resource
    private OmsDropDownFeign omsDropDownFeign;



}
