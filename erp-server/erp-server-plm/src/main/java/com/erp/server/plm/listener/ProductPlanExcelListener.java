package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.erp.model.plm.dto.excel.ProductPlanExcelDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.service.ProductPlanService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 10:16
 */
public class ProductPlanExcelListener extends AnalysisEventListener<ProductPlanExcelDTO> {

    private SysUserFeign sysUserFeign;

    private ProductPlanService productPlanService;
    /**
     * 错误数据返回集合
     */
    private List<ProductPlanExcelDTO> list;

    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<ProductPlanExcelDTO> dataList = new ArrayList<>();

    public ProductPlanExcelListener(ProductPlanService productPlanService,SysUserFeign sysUserFeign) {
        this.productPlanService = productPlanService;
        this.sysUserFeign = sysUserFeign;
        this.list = new ArrayList<>();
    }

    @Override
    public void invoke(ProductPlanExcelDTO productPlanExcelDTO, AnalysisContext analysisContext) {

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<ProductPlanExcelDTO> getDateList(){
        return list;
    }

    public List<ProductPlanExcelDTO> getExcelDateList(){
        return dataList;
    }
}
