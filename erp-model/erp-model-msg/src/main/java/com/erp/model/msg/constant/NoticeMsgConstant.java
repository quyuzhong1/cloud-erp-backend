package com.erp.model.msg.constant;

/**
 * @author Lambda
 * @Classname NoticeMsgConstant

 * @Date 2023-04-28 16:13
 * @Created by yl
 */
public class NoticeMsgConstant {
    private NoticeMsgConstant() {
        throw new IllegalStateException("Utility NoticeMsgConstant class");
    }

    /**
     * 质检结果消息头
     */
    public final static String QC_RESULT_HEAD = "【%s】已完成-【%s】的【%s】";

    /**
     * 质检结果消息内容
     */
    public final static String QC_RESULT_CONTENT = "**采购订单：**%s\n**产品名称：**%s\n**质检人员：**%s\n**完成时间：**%s\n**处理措施：**%s";

    /**
     * 首次质检完成回填尺寸信息
     */
    public final static String QC_BACK_FILL_PACK_HEAD = "通知：质检尺寸信息更新通知";

    /**
     * 首次质检完成回填尺寸信息
     */
    public final static String QC_BACK_FILL_PACK_CONTENT = "**所属SKU：{}\n**更新数据：产品尺寸{}；箱规{}；净重{}；单箱数量{}\n发生时间：{}";

    /**
     * 质检通知消息头
     */
    public final static String FS_QC_SETTING_HEAD="总计质检单{}，已质检{}【{}】，未质检{}；累计未质检{}，累计超时质检{}";

    /**
     * 质检通知消息体
     */
    public final static String FS_QC_SETTING_CONTENT="**通知类型：{}\n**推送时间：{}";


    /**
     * 仓位补货通知消息头
     */
    public final static String FS_WLR_SETTING_HEAD="SDC-ERP：仓位补货通知";

    /**
     * 仓位补货通知消息体
     */
    public final static String FS_WLR_SETTING_CONTENT="**【仓储管理-仓位补货】待处理数据,数量：{%s}\n【仓储管理-仓位补货】处理中数据,数量：{%s}\n**推送时间：{%s}";

    /**
     * 装箱完成通知消息头
     */
    public final static String FS_FINISH_PACKING_HEAD="已完成{}装箱操作，可在装箱任务导出装箱清单，请知悉";

    /**
     * 装箱完成通知消息体
     */
    public final static String FS_FINISH_PACKING_CONTENT=" 通知类型：{}\n 关联单号：{}";

    /**
     * 试产量产审核完成通知消息头
     */
    public final static String PILOT_APPROVE_END_HEAD="试产量产单已完成审核，请知悉";

    /**
     * 试产量产审核完成通知消息体
     */
    public final static String PILOT_APPROVE_END_CONTENT=" 通知类型：试产量产完成通知\n 产品经理：{%s}\n SKU：{%s}";

    /**
     * 要货申请通知消息头
     */
    public final static String FS_REQUISITION_SETTING_HEAD="要货申请通知";
    /**
     * 要货申请完成通知消息体
     */
    public final static String FS_REQUISITION_SETTING_CONTENT="所属项目：%s\n业务名称：%s\n详细信息：%s\n创建人：%s\n发送时间：%s";
//    要货申请单单据【%s】当前已处理完成，请即时下推发货单出库

    /**
     * 头程发货通知消息头
     */
    public final static String FS_FIRSTMILEDELIVERY_SETTING_HEAD="头程发货单通知";
    /**
     * 头程发货通知消息体
     */
    public final static String FS_FIRSTMILEDELIVERY_SETTING_CONTENT="所属项目：%s\n业务名称：%s\n详细信息：%s\n创建人：%s\n发送时间：%s";

    /**
     * 要货申请变更通知消息头
     */
    public final static String FS_REQUISITION_CHANGE_SETTING_HEAD="要货申请变更通知";
    /**
     * 要货申请完成通知消息体
     */
    public final static String FS_REQUISITION_CHANGE_SETTING_CONTENT="所属项目：%s\n业务名称：%s\n详细信息：要货申请变更单：【%s】当前【%s】，请即时处理\n创建人：%s\n审核人：%s\n发送时间：%s";


    /**
     * 采购调价消息头
     */
    public final static String PRUCHASE_PRICE_CHANGE_WAIT_HEAD = "采购调价审核提醒";
    public final static String PRUCHASE_PRICE_CHANGE_REJECT_HEAD = "采购调价审核不通过提醒";
    public final static String PRUCHASE_PRICE_CHANGE_APPROVE_HEAD = "采购调价完成通知";


    /**
     * 采购调价消息内容
     */
    public final static String PRUCHASE_PRICE_CHANGE_WAIT_CONTENT = "采购调价表【%s】已在数大臣审核提交，请尽快审核\n供应商：%s \n创建人：%s \n创建时间：%s \n审核人：%s \n审核时间：";
    public final static String PRUCHASE_PRICE_CHANGE_REJECT_CONTENT = "采购调价表【%s】已在数大臣审核不通过，请知悉\n供应商：%s \n创建人：%s \n创建时间：%s \n审核人：%s \n审核时间：%s \n审核结果：审核不通过 \n审核说明：%s ";
    public final static String PRUCHASE_PRICE_CHANGE_APPROVE_CONTENT = "采购调价表【%s】已在数大臣审核通过，请知悉\n供应商：%s \n创建人：%s \n创建时间：%s \n审核人：%s \n审核时间：%s \n审核结果：审核通过 \n审核说明：%s ";

    /**
     * 质检通知消息头
     */
    public final static String SHOP_EXPIRE_NOTICE="店铺{}将于{}授权到期，请及时处理";

    /** B2C平台SKU未匹配通知标题 */
    public final static String FS_SKU_MAPPING_PLATFORM_TITLE = "B2C平台的sku映射尚存在未匹配通知";
    /** B2C平台SKU未匹配通知正文头部（%s=汇总行列表，每行格式：{平台名}({count}个)） */
    public final static String FS_SKU_MAPPING_PLATFORM_CONTENT = "平台(未匹配sku数量),汇总如下：\n%s";

    /** 库存SKU未匹配通知标题 */
    public final static String FS_SKU_MAPPING_WAREHOUSE_TITLE = "海外仓的sku映射尚存在未匹配通知";
    /** 库存SKU未匹配通知正文头部（%s=汇总行列表，每行格式：{仓库简称}({count}个)） */
    public final static String FS_SKU_MAPPING_WAREHOUSE_CONTENT = "海外仓(未匹配sku数量),汇总如下：\n%s";

    /** B2B客户SKU未匹配通知标题 */
    public final static String FS_SKU_MAPPING_CUSTOMER_TITLE = "B2B客户的sku映射尚存在未匹配通知";
    /** B2B客户SKU未匹配通知正文头部（%s=汇总行列表，每行格式：{客户名}({count}个)） */
    public final static String FS_SKU_MAPPING_CUSTOMER_CONTENT = "B2B客户名称（未匹配sku数量）,汇总如下：\n%s";

    /** B2B平台SKU未匹配通知标题 */
    public final static String FS_SKU_MAPPING_B2B_PLATFORM_TITLE = "B2B平台的sku映射尚存在未匹配通知";
    /** B2B平台SKU未匹配通知正文头部（%s=汇总行列表，每行格式：{平台名}({count}个)） */
    public final static String FS_SKU_MAPPING_B2B_PLATFORM_CONTENT = "平台(未匹配sku数量),汇总如下：\n%s";
}
