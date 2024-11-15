package com.erp.server.plm.constant;

import java.io.Serializable;

/**
 * @Classname ProductConstant

 * @Date 2022-10-13 10:34
 * @Created by yl
 */
public class ProductConstant implements Serializable {

    public static final Integer NEW_PRODUCT = 1;
    public static final Integer ITERATION_PRODUCT = 2;

    /**
     * 产品开发管理列表
     */
    public static final String PRODUCT_DEVELOPMENT = "productDevelop";


    /**
     * 产品经理
     */
    public static final String PRODUCT_CHARGE = "产品经理";

    /**
     * 项目经理
     */
    public static final String PROJECT_CHARGE = "项目经理";

    /**
     * 产品管理列表
     */
    public static final String product = "product";

    /**
     * 产品归档管理列表
     */
    public static final String PRODUCT_ARCHIVE = "productArchive";

    /**
     * 所有
     */
    public static final String ALL = "all";

    /**
     * 完成
     */
    public static final String FINISHED = "finished";

    /**
     * 完成
     */
    public static final String UNFINISHED = "unfinished";

    /**
     * 产品导出
     */
    public static final Integer PRODUCT_EXPORT = 0;

    /**
     * 产品任务导出
     */
    public static final Integer PRODUCT_TASK_EXPORT = 1;

    /**
     * 带电标识
     */
    public static final String IS_ELECTRIC="isElectric";
    /**
     * 液体标识
     */
    public static final String IS_LIQUID="isLiquid";


    /**
     * 默认产品单位:Pcs
     */
    public static final String PRODUCT_UNIT_DEFAULT = "Pcs";

    /**
     * 默认产品属性:自研发
     */
    public static final String PRODUCT_PROPERTY_DEFAULT = "自研发";

    /**
     * 产品属性:费用
     */
    public static final String PRODUCT_PROPERTY_COST = "费用";

    /**
     * 产品属性:服务
     */
    public static final String PRODUCT_PROPERTY_SERVICE = "服务";


}
