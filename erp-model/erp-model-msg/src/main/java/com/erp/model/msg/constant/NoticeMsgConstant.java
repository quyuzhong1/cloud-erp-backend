package com.erp.model.msg.constant;

/**
 * @author Lambda
 * @Classname NoticeMsgConstant
 * @Description TODO
 * @Date 2023-04-28 16:13
 * @Created by yl
 */
public interface NoticeMsgConstant {


    /**
     * 质检结果消息头
     */
    String QC_RESULT_HEAD = "【%s】已完成-【%s】的【%s】";

    /**
     * 质检结果消息内容
     */
    String QC_RESULT_CONTENT = "**采购订单：**%s\n**产品名称：**%s\n**质检人员：%s\n**完成时间：**%s\n**处理措施：**%s";
}
