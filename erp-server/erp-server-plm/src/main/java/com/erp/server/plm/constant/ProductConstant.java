package com.erp.server.plm.constant;

/**
 * @Classname ProductConstant

 * @Date 2022-10-13 10:34
 * @Created by yl
 */
public interface ProductConstant {

    Integer NEW_PRODUCT = 1;
    Integer ITERATION_PRODUCT = 2;

    /**
     * 产品开发管理列表
     */
    String PRODUCT_DEVELOPMENT = "productDevelop";


    /**
     * 产品经理
     */
    String PRODUCT_CHARGE = "产品经理";

    /**
     * 项目经理
     */
    String PROJECT_CHARGE = "项目经理";

    /**
     * 产品管理列表
     */
    String product = "product";

    /**
     * 产品归档管理列表
     */
    String PRODUCT_ARCHIVE = "productArchive";

    /**
     * 所有
     */
    String ALL = "all";

    /**
     * 完成
     */
    String FINISHED = "finished";

    /**
     * 完成
     */
    String UNFINISHED = "unfinished";

    /**
     * 产品导出
     */
    Integer PRODUCT_EXPORT = 0;

    /**
     * 产品任务导出
     */
    Integer PRODUCT_TASK_EXPORT = 1;

    /**
     * 带电标识
     */
    String IS_ELECTRIC="isElectric";


}
