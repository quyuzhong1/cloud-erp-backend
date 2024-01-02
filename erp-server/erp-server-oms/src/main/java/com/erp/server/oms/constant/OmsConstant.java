package com.erp.server.oms.constant;

/**
 * @author Lambda
 * @Classname ScmConstant1

 * @Date 2023-03-20 19:54
 * @Created by yl
 */
public interface OmsConstant {


    /**
     * 所有
     */
    String ALL = "all";

    /**
     * 待审核
     */
    String WAIT_APPROVE = "waitApprove";

    /**
     * 已审核
     */
    String APPROVE = "approve";

    /**
     * 审核不通过
     */
    String REJECT = "reject";

    /**
     * 待发货
     */
    String WAIT_DELIVERY = "waitDelivery";


    /**
     * 已发货
     */
    String DELIVERY = "delivery";

    /**
     * 待提交
     */
    String WAIT_SUBMIT = "waitSubmit";

    /**
     * 已匹配
     */
    String ALREADY = "already";

    /**
     * 未匹配
     */
    String NOT = "not";

    Integer SKU_MAPPING_RULE_SIZE = 5;
}
