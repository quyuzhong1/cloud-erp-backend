package com.erp.server.plm.constant;

import java.io.Serializable;

/**
 * @Classname 任务属性

 * @Date 2022-09-19 9:48
 * @Created by yl
 */
public class TaskConstant implements Serializable {

    public static final Integer APPROVAL_TASK = 1;
    //评审任务
    public static final Integer REVIEW_TASK = 1;


    //低级任务
    public static final Integer LOW_TASK=1;

    //中级任务
    public static final Integer INTERMEDIATE_TASK=2;

    //高级任务
    public static final Integer ADVANCED_TASK=3;

    public static final Integer PROJECT_TASK = 2;

    public static final String APPROVAL_TASK_PHASE = "立项阶段";


    public static final Integer MY_FINISH_TASK = 0;
    public static final Integer MY_APPROVAL_TASK = 1;
    public static final Integer ALL_FINISH_TASK = 2;
    //变更任务
    public static final Integer CHANGE_TASK = 3;


    //高级任务
    public static final Integer YES_MILEPOST=1;

    public static final String FILE_TYPE = "文件";

    //分配给我
    public static final String ASSIGN_TO_ME = "assignToMe";

    //我创建的
    public static final  String MY_CREATE = "myCreate";


    //生成sku
    public static final String CREATE_SKU = "createSku";

    //填写sku
    public static final String FILL_PRODUCT_INFO = "fillProductInfo";


    //所有任务
    public static final String ALL = "all";

    //全部
    public static final String ALL_CN = "全部";
    /**
     * 任务条件
     * 1.待完成，待审核
     * 2 全部
     */
    public static final Integer WAIT_HANDLE = 1;


    /**
     * 任务条件
     * 3.待审核
     */
    public static final Integer WAIT_AUDIT = 3;

    //全部
    public static final Integer ALL_TASK = 2;


    //产品
    public static final String PRODUCT = "product";

    //计划结束时间
    public static final String PLAN_END_TIME = "planEndTime";

    /**
     * 本地上传
     */
    public static final Integer LOCAL_UPLOAD=0;

    /**
     * 链接
     */
    public static final Integer URL=1;


}
