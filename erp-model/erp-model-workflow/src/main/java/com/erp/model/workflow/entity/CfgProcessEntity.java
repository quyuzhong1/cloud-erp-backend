package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 流程配置
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_process")
public class CfgProcessEntity extends BaseEntity<CfgProcessEntity> {

    /**
    * 配置编码：purchaseOrder=采购订单，purchaseApplication=采购申请单，purchaseChange=采购变更单，productChange=变更管理，productBomInfo=BOM管理，salesDemand=备货申请单，poInstock=入库单，soOutstock=销售出库单，customerB2bChangeSeller=B2B客户表变更销售员，requisitionApplicationChange=要货申请变更单，ProductLogistics=物流产品信息，transferApplication=调拨申请单，supplier=供应商列表，poReturn=采购退货单，pilotApplication=试产量产单，soDeliveryNoticeChange=发货通知变更单，stocktakingTask=盘点任务，transferInfo=直接调拨单，poReceive=收货单，productDetail=产品管理，projectTask=任务列表，subcontractOrder=委外订单，purchasePrice=采购价目表，purchasePriceChange=采购调价表，soB2c=B2C销售订单，soInfo=B2B销售订单，firstMileDelivery=发货单，customerInfo=B2B客户列表，soChange=B2B销售变更单，deliveryPlan=第三方仓发货计划，requisitionApplication=要货申请，soPrice=销售价目表，soPriceChange=销售调价表，transferIn=分步式调入单，transferOut=分步式调出单  枚举：CfgProcessCodeEnum
    */
    @TableField("code")
    private String code;
    /**
    * 配置名称
    */
    @TableField("name")
    private String name;
    /**
    * 配置单据
    */
    @TableField("bussiness_key")
    private String bussinessKey;


    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String BUSSINESS_KEY = "bussiness_key";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
