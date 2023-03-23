package com.common.business.constant;

/**
 * 搜索类型
 *
 * @author Lambda
 * @Classname SearchType
 * @Description TODO
 * @Date 2023-03-23 18:19
 * @Created by yl
 */
public interface SearchType {

    /**
     * 所有
     */
    String ALL = "all";

    /**
     * 待我审核
     */
    String WAIT_FOR_ME_APPROVE = "waitForMeApprove";

    /**
     * 待审核
     */
    String WAIT_APPROVE = "waitApprove";
}
