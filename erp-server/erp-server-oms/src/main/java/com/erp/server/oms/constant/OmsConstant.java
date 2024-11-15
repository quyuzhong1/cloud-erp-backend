package com.erp.server.oms.constant;

/**
 * @author Lambda
 * @Classname ScmConstant1

 * @Date 2023-03-20 19:54
 * @Created by yl
 */
public class OmsConstant {

    /**
     * 所有
     */
    static final String ALL = "all";

    /**
     * 待审核
     */
    static final String WAIT_APPROVE = "waitApprove";

    /**
     * 已审核
     */
    static final String APPROVE = "approve";

    /**
     * 审核不通过
     */
    static final String REJECT = "reject";

    /**
     * 待发货
     */
    static final String WAIT_DELIVERY = "waitDelivery";


    /**
     * 已发货
     */
    static final String DELIVERY = "delivery";

    /**
     * 待提交
     */
    static final String WAIT_SUBMIT = "waitSubmit";

    /**
     * 已匹配
     */
    static final String ALREADY = "already";

    /**
     * 未匹配
     */
    static final String NOT = "not";

    static final Integer SKU_MAPPING_RULE_SIZE = 5;

    private OmsConstant() {
    }
}
