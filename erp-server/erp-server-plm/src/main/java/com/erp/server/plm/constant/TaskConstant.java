package com.erp.server.plm.constant;

import org.apache.poi.ss.formula.functions.T;

/**
 * @Classname 任务属性
 * @Description TODO
 * @Date 2022-09-19 9:48
 * @Created by yl
 */
public interface TaskConstant {

    public static final Integer APPROVAL_TASK = 1;

    public static final Integer PROJECT_TASK = 2;

    public static final String APPROVAL_TASK_NAME = "立项阶段";


    public static final Integer MY_FINISH_TASK = 0;
    public static final Integer MY_APPROVAL_TASK = 1;
    public static final Integer ALL_FINISH_TASK = 2;

    public static final String FILE_TYPE = "文件";

    //分配给我
    String ASSIGN_TO_ME = "assignToMe";

    //我创建的
    String MY_CREATE = "myCreate";

    //所有任务
    String ALL = "all";

    //全部
    String ALL_CN = "全部";
    /**
     * 任务条件
     * 1.待完成，待审核
     * 2 全部
     */
    Integer WAIT_HANDLE = 1;

    //全部
    Integer ALL_TASK = 2;


    //产品
    String PRODUCT="product";

    //计划结束时间
    String PLAN_END_TIME="planEndTime";


}
