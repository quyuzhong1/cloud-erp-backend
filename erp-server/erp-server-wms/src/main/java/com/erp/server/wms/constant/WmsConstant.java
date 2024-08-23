package com.erp.server.wms.constant;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lambda
 * @Classname ScmConstant1

 * @Date 2023-03-20 19:54
 * @Created by yl
 */
public interface WmsConstant {

    /**
     * 驳回类型
     */
    String REJECT = "reject";

    /**
     * 通过类型
     */
    String PASS = "pass";


    /**
     * 质检产品图片 类型
     */
    String QC_PRODUCT="product";

    /**
     * 质检外箱图片 类型
     */
    String QC_BOX="box";


    /**
     * 质检单 质检报告附件
     */
    String QC_REPORT="report";


    /**
     * 质检信息不良
     */
    String BAD="bad";

    /**
     * 质检附件
     */
    String QC_ATTACHMENT="qcAttachment";


    /**
     * 质检结果处理措施
     */
    String QC_RESULT_HANDLE_MODE="returnSupplier";

    /**
     * 所有
     */
    String ALL = "all";

    /**
     * 按筛选组织划分的仓库
     */
    String WAREHOUSE_BY_FILTER_ORG = "warehouseByFilterOrg";
    /**
     * 供应商仓库类型
     */
    String SUPPLIER = "supplier";
    /**
     * 虚拟仓分货单
     */
    String VIRTUAL_WAREHOUSE_ALLOCATION = "virtualWarehouseAllocation";

    List<String> WDT_NULL_LOCATION = Arrays.asList("直发暂存","发货暂存待放回", "下架暂存", "销退质检", "补货暂存", "其它未上架", "销退暂存", "盘亏暂存", "发货暂存", "采购未上架");

}
