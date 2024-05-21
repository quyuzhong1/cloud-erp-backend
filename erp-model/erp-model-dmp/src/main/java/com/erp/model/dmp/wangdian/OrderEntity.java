package com.erp.model.dmp.wangdian;

import com.common.business.dto.CleanBaseDTO;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderEntity extends CleanBaseDTO {

    private String _id;

    /**
     * 出库单ID
     */
    private String stockoutId;
    /**
     * 出库单号
     */
    private String orderNo;
    /**
     * 系统订单编号
     */
    private String srcOrderNo;
    /**
     * 仓库编号
     */
    private String warehouseNo;
    /**
     * 仓库名称
     */
    private String warehouseName;
    /**
     * 发货时间
     */
    private String consignTime;
    /**
     * 源单据类别
     */
    private String orderType;
    /**
     * 货品数量
     */
    private BigDecimal goodsCount;
    /**
     * 物流单号
     */
    private String logisticsNo;
    /**
     * 收件人姓名
     */
    private String receiverName;
    /**
     * 国家
     */
    private String receiverCountry;
    /**
     * 省份ID
     */
    private String receiverProvince;
    /**
     * 城市ID
     */
    private String receiverCity;
    /**
     * 地区ID
     */
    private String receiverDistrict;
    /**
     * 地址
     */
    private String receiverAddress;
    /**
     * 收件人手机
     */
    private String receiverMobile;
    /**
     * 收件人固话
     */
    private String receiverTelNo;
    /**
     * 收件人邮编
     */
    private String receiverZip;
    /**
     * 省市区
     */
    private String receiverArea;
    /**
     * 出库单备注
     */
    private String remark;
    /**
     * 重量
     */
    private BigDecimal weight;
    /**
     * 截停原因
     * 0正常
     * 1申请退款
     * 2已退款
     * 4地址被修改
     * 8发票被修改
     * 16物流被修改
     * 32仓库变化
     * 64备注修改
     * 128更换货品
     * 256取消退款
     */
    private Integer blockReason;
    /**
     * 物流方式
     */
    private Integer logisticsType;
    /**
     * 物流编号
     */
    private String logisticsCode;
    /**
     * 物流公司名称
     */
    private String logisticsName;
    /**
     * 出库单ID
     */
    private String shopId;
    /**
     * 仓库id
     */
    private String warehouseId;
    /**
     * 物流id
     */
    private String logisticsId;

    /**
     * 异常原因
     * 0正常
     * 1无库存记录
     * 2地址发生变化
     * 4发票变化
     * 8仓库变化
     * 16备注变化
     * 32平台更换货品
     * 64退款
     */
    private Integer badReason;
    /**
     * 大头笔
     */
    private String receiverDtb;
    /**
     * 退款状态
     * 0无退款
     * 1申请退款
     * 2部分退款
     * 3全部退款
     */
    private Integer refundStatus;

    /**
     * 销售类型
     * 1网店销售
     * 2线下订单
     * 3售后换货
     * 4批发业务
     * 7现款销售
     * 8分销订单
     * 101 订单自定义属性1
     * 102 订单自定义属性2
     * 103 订单自定义属性3
     * 104 订单自定义属性4
     * 105 订单自定义属性5
     * 106 订单自定义属性6
     * 107 订单自定义属性7
     * 108 订单自定义属性8
     * 109 订单自定义属性9
     * 110 订单自定义属性10
     */
    private Integer tradeType;
    /**
     * 业务员编号
     */
    private Integer salesmanNo;
    /**
     * 业务员姓名
     */
    private Integer fullname;
    /**
     * 拣货员
     */
    private Integer pickerName;
    /**
     * 验货员
     */
    private Integer examinerName;
    /**
     * 验货员
     */
    private Integer consignerName;
    /**
     * 发货员
     */
    private Integer printerName;
    /**
     * 打包员
     */
    private Integer packagerName;
    /**
     * 订单状态
     * 4 线下退款
     * 5已取消
     * 6 待转预订单(待审核)
     * 7 待转已完成
     * 10未付款
     * 12待尾款
     * 15等未付
     * 16延时审核
     * 19预订单前处理
     * 20 审核前处理
     * 21自流转待发货
     * 23 异常预订单
     * 24 换货预订单
     * 25 待处理预订单
     * 27待分配预订单
     * 30待客审
     * 35待财审
     * 55已审核
     * 95已发货
     * 96 成本确认（待录入计划成本，订单结算时有货品无计划成本）
     * 101 已过账
     * 110已完成
     */
    private Integer tradeStatus;
    /**
     * 订单编号
     */
    @SerializedName("trade_no")
    private String tradeNo;
    /**
     * 原始单号
     */
    private String srcTradeNo;
    /**
     * 客户网名
     */
    private String nickName;
    /**
     * 客户编码
     */
    private String customerNo;
    /**
     * 客户姓名
     */
    private String customerName;
    /**
     * 下单时间
     */
    private String tradeTime;
    /**
     * 支付时间
     */
    private String payTime;
    /**
     * 标记名称
     */
    private String flagName;
    /**
     * 邮费
     */
    private BigDecimal postAmount;
    /**
     * 证件类别
     */
    private Integer idCardType;
    /**
     * 证件号码
     */
    private Integer idCard;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 店铺编号
     */
    private String shopNo;
    /**
     * 店铺备注
     */
    private String shopRemark;
    /**
     * 出库单状态:
     * 5已取消
     * 10待放回(拣货待放回), 小于该值的都是已取消的单子
     * 51 缺货
     * 53 WMS已接单
     * 54 获取电子面单
     * 60 待分配
     * 61 排队中
     * 63 待补货
     * 65 待处理
     * 70 待发货
     * 73 爆款锁定
     * 75 待拣货
     * 77 拣货中,PDA拣货后
     * 79 已拣货
     * 90 延时发货
     * 110已完成
     */
    private String status;
    /**
     * 发票类型:
     * 0：不需要
     * 1：电子普通发票
     * 2：增值税普通发票
     * 3：电子增值税专用发票
     * 4：纸质普通发票
     * 5：纸质增值税专用发票
     */
    private Integer invoiceType;
    /**
     * 发票id:
     * 目前只设0-1，
     * 1表示已开发票
     */
    private Integer invoiceId;
    /**
     * 货到付款金额
     */
    private BigDecimal codAmount;
    /**
     * 发货条件:
     * 1款到发货
     * 2货到付款(包含部分货到付款)
     * 3分期付款
     * 4挂账
     */
    private Integer deliveryTerm;
    /**
     * 平台ID
     */
    private String platformId;
    /**
     * 订单ID
     */
    private String tradeId;
    /**
     * 审核员编号
     */
    private String employeeNo;
    /**
     * 优惠金额
     */
    private BigDecimal discount;
    /**
     * 税额
     */
    private BigDecimal tax;
    /**
     * 税率
     */
    private BigDecimal taxRate;
    /**
     * 币种
     */
    private String currency;
    /**
     * 建单时间
     */
    private String created;
    /**
     * 出库单建单时间
     */
    private String stockCheckTime;
    /**
     * 打印备注
     */
    private String printRemark;
    /**
     * 买家留言
     */
    private String buyerMessage;
    /**
     * 客服备注
     */
    private String csRemark;
    /**
     * 发票抬头
     */
    private String invoiceTitle;
    /**
     * 发票内容
     */
    private String invoiceContent;
    /**
     * 称重预估邮资
     */
    private BigDecimal postFee;
    /**
     * 包装成本
     */
    private BigDecimal packageFee;
    /**
     * 已付金额(使用应收金额)
     */
    private BigDecimal receivable;
    /**
     * 总成本价（对应销售出库明细内实际货品总成本）
     */
    private BigDecimal goodsTotalCost;
    /**
     * 预估货品成本(使用单品审核时成本*数量计算得出该成本)
     */
    private BigDecimal goodsTotalAmount;
    /**
     * 最后修改时间
     */
    private String modified;
    /**
     * 分销商昵称
     */
    private String fenxiaoNick;
    /**
     * 订单标签
     */
    private String tradeLabel;
    /**
     * 订单来源
     * 1、接口抓取
     * 2、手工建单
     * 3、Excel导入
     * 4、复制订单
     * 5、接口推送
     * 6、补发订单
     * 7、PDA选货开单
     * 8、分销补发订单
     */
    private Integer tradeFrom;
    /**
     * 分拣波次
     */
    private String picklistNo;
    /**
     * 分拣序号
     */
    private Integer picklistSeq;
    /**
     * 物流单打印状态
     * 0：未打印
     * 1：打印中
     * 2：已打印
     * 3：无需打印
     */
    private Integer logisticsPrintStatus;
    /**
     * 已付
     */
    private BigDecimal paid;
    /**
     * 店铺平台id
     */
    private Integer shopPlatformId;
    /**
     * 子平台id
     */
    private Integer subPlatformId;
    /**
     * 接口处理错误信息
     */
    private String errorInfo;

    /**
     * 其他出库自定义子类别0,1,2,3,4
     */
    private Integer customType;
    /**
     * 发货单模板id
     */
    private Integer sendbillTemplateId;
    /**
     * 客户id
     */
    private Integer customerId;
    /**
     * 仓库类别
     * 0：不限
     * 1：普通仓库
     * 2：自动流转外部仓
     * 3：奇门仓储
     * 4：京东沧海
     * 6：抖店云仓
     * 125：代发仓
     * 126：分销委外仓
     */
    private Integer warehouseType;
    /**
     * 制单人id（操作员）
     */
    private Integer operatorId;
    /**
     * 外部单号
     */
    private String outerNo;
    /**
     * 出库状态
     * 0：无
     * 1：已验货
     * 2：已称重
     * 16：已拣货
     * 32：电子面单回传
     * 64：已分拣
     * 128：配送清单打印状态
     * 4096：订单回告（订单生成出库单中间量，暂时只有订单合并回告使用）
     */
    private Integer consignStatus;
    /**
     * 货品种类
     */
    private BigDecimal goodsTypeCount;
    /**
     * 预估邮资成本
     */
    private BigDecimal calcPostCost;
    /**
     * 打印批次
     */
    private String batchNo;
    /**
     * 创建时间
     */
    private String createdDate;
    /**
     * 分销原始单号
     */
    private String fenxiaoTid;
    /**
     * 分销商编号
     */
    private String fenxiaoNickNo;
    /**
     * 主播
     */
    private String anchorName;
    /**
     * 助播
     */
    private String assistAchorName;
    /**
     * 运营
     */
    private String controlAchorName;
    /**
     * 拣货分组名称
     */
    private String operationAnchorName;
    /**
     * 分销商编号
     */
    private String pickGroupName;
    /**
     * 物流单列表
     */
    private List<LogisticsList> logisticsList;
    /**
     * 销售出库单详情
     */
    private List<DetailItem> detailsList;
    /**
     * 清洗到发货订单 0 未清洗 1 清洗中 2 清洗完成
     */
    private Integer cleanToDelivery;
    /**
     * 上次推送Delivery时间
     */
    private LocalDateTime lastPushDeliveryTime;

    @Getter
    @Setter
    public static class LogisticsList {
        /**
         * 物流单id
         */
        private String recId;
        /**
         * 出库单id
         */
        private String stockoutId;
        /**
         * 物流单号
         */
        private String logisticsNo;
        /**
         * 估算重量
         */
        private String calcWeight;
        /**
         * 称重重量
         */
        private String weight;
        /**
         * 包装
         */
        private String packageName;
        /**
         * 物流名称
         */
        private String logisticsName;
        /**
         * 物流ID
         */
        private String logisticsId;
        /**
         * 估算邮资
         */
        private String postage;
        /**
         * 备注
         */
        private String remark;
        /**
         * 长
         */
        private String length;
        /**
         * 宽
         */
        private String width;
        /**
         * 高
         */
        private String height;
        /**
         * 体积
         */
        private String volume;
    }

    @Getter
    @Setter
    public static class DetailItem {
        /**
         * 物流单id
         */
        private String recId;
        /**
         * 出库单ID
         */
        private String stockoutId;
        /**
         * 订单明细id
         */
        private String srcOrderDetailId;
        /**
         * 单品id
         */
        private String specId;
        /**
         * 商家编码
         */
        private String specNo;
        /**
         * 货品数量
         */
        private BigDecimal goodsCount;
        /**
         * 总成本
         */
        private BigDecimal totalAmount;
        /**
         * 成交价
         */
        private BigDecimal sellPrice;
        /**
         * 备注
         */
        private String remark;
        /**
         * 货品名
         */
        private String goodsName;
        /**
         * 货品编号
         */
        private String goodsNo;
        /**
         * 规格名称
         */
        private String specName;
        /**
         * 规格码
         */
        private String specCode;
        /**
         * 货品成本
         */
        private BigDecimal costPrice;
        /**
         * 总重量
         */
        private BigDecimal weight;
        /**
         * 货品id（系统货品主键）
         */
        private String goodsId;
        /**
         * 规格自定义属性1
         */
        private String prop1;
        /**
         * 规格自定义属性2
         */
        private String prop2;
        /**
         * 规格自定义属性3
         */
        private String prop3;
        /**
         * 规格自定义属性4
         */
        private String prop4;
        /**
         * 规格自定义属性5
         */
        private String prop5;
        /**
         * 规格自定义属性6
         */
        private String prop6;
        /**
         * 平台id
         */
        private Integer platformId;
        /**
         * 退款状态
         * 0无退款
         * 1申请退款
         * 2部分退款
         * 3全部退款
         */
        private Integer refundStatus;
        /**
         * 单价/货品原单价
         */
        private BigDecimal marketPrice;
        /**
         * 货品总优惠
         */
        private BigDecimal discount;
        /**
         * 货品成交价
         */
        private BigDecimal sharePrice;
        /**
         * 总货款/货品成交总价
         */
        private BigDecimal shareAmount;
        /**
         * 税率
         */
        private BigDecimal taxRate;
        /**
         * 主条码
         */
        private String barcode;
        /**
         * 单位名称
         */
        private String unitName;
        /**
         * 订单货品(子订单)id
         */
        private String saleOrderId;
        /**
         * 是否是赠品:
         * 0非赠品
         * 1自动赠送
         * 2手工赠送
         * 4周期购赠送
         * 8平台赠送
         */
        private Integer giftType;
        /**
         * 原始子订单号
         */
        private String srcOid;
        /**
         * 原始订单号
         */
        private String srcTid;
        /**
         * 订单内部来源:
         * 1手机
         * 2聚划算
         */
        private Integer fromMask;
        /**
         * 货品类型:
         * 0：其它
         * 1：销售货品
         * 2：原材料
         * 3：包装物
         * 4：周转材料
         * 5：虚拟商品
         * 6：固定资产
         * 8：分装箱
         */
        private Integer goodsType;
        /**
         * 货品自定义属性1
         */
        private String goodProp1;
        /**
         * 货品自定义属性2
         */
        private String goodProp2;
        /**
         * 货品自定义属性3
         */
        private String goodProp3;
        /**
         * 货品自定义属性4
         */
        private String goodProp4;
        /**
         * 货品自定义属性5
         */
        private String goodProp5;
        /**
         * 货品自定义属性6
         */
        private String goodProp6;
        /**
         * 组合装编码
         */
        private String suiteNo;
        /**
         * 分摊邮费
         */
        private BigDecimal sharePostAmount;
        /**
         * 已付
         */
        private BigDecimal paid;
        /**
         * 是否包装
         * true:是
         * false:否
         */
        private Boolean isPackage;
        /**
         * 品牌编号
         */
        private String brandNo;
        /**
         * 品牌名称
         */
        private String brandName;
        /**
         * 源单据类别
         * 1：销售订单
         */
        private Integer srcOrderType;
        /**
         * 基本单位
         */
        private Integer baseUnitId;
        /**
         * 辅助单位
         */
        private Integer unitId;
        /**
         * 单位换算
         */
        private BigDecimal unitRatio;
        /**
         * 辅助数量
         */
        private BigDecimal num2;
        /**
         * 货品数量
         */
        private BigDecimal num;
        /**
         * 出库货位id
         */
        private String positionId;
        /**
         * 指定出库批次
         */
        private String batchId;
        /**
         * 是否验货
         * true:是
         * false:否
         */
        private Boolean isExamined;
        /**
         * 有效期
         */
        private String expireDate;
        /**
         * 扫描方式
         * 0：未验货
         * 1：扫描验货
         * 2：手工验货
         * 3：快速验货
         * 4：无需验货
         */
        private Integer scanType;
        /**
         * 最后修改时间，时间格式：yyyy-MM-dd HH:mm:ss
         */
        private String modifiedDate;
        /**
         * 创建时间
         */
        private String createdDate;
        /**
         * 分类名称
         */
        private String className;
        /**
         * 出库货位明细
         */
        private List<PositionDetailsList> positionDetailsList;
    }

    @Getter
    @Setter
    public static class PositionDetailsList {
        /**
         * 货位明细id
         */
        private String recId;
        /**
         * 销售出库单详情id
         */
        private String stockoutDetailId;
        /**
         * 货位id
         */
        private String positionId;
        /**
         * 货位号
         */
        private String positionNo;
        /**
         * 有效期
         */
        private String expireDate;
        /**
         * 批次号
         */
        private String batchNo;
        /**
         * 当前货位出库总货品数量
         */
        private BigDecimal positionGoodsCount;
    }

    @Override
    public String toString() {
        return "OrderEntity{" +
                "stockoutId='" + stockoutId + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", srcOrderNo='" + srcOrderNo + '\'' +
                ", warehouseNo='" + warehouseNo + '\'' +
                ", warehouseName='" + warehouseName + '\'' +
                ", consignTime='" + consignTime + '\'' +
                ", orderType='" + orderType + '\'' +
                ", goodsCount=" + goodsCount +
                ", logisticsNo='" + logisticsNo + '\'' +
                ", receiverName='" + receiverName + '\'' +
                ", receiverCountry='" + receiverCountry + '\'' +
                ", receiverProvince=" + receiverProvince +
                ", receiverCity=" + receiverCity +
                ", receiverDistrict=" + receiverDistrict +
                ", receiverAddress='" + receiverAddress + '\'' +
                ", receiverMobile='" + receiverMobile + '\'' +
                ", receiverTelNo='" + receiverTelNo + '\'' +
                ", receiverZip='" + receiverZip + '\'' +
                ", receiverArea='" + receiverArea + '\'' +
                ", remark='" + remark + '\'' +
                ", weight=" + weight +
                ", blockReason=" + blockReason +
                ", logisticsType=" + logisticsType +
                ", logisticsCode='" + logisticsCode + '\'' +
                ", logisticsName='" + logisticsName + '\'' +
                ", shopId=" + shopId +
                ", warehouseId=" + warehouseId +
                ", logisticsId=" + logisticsId +
                ", badReason=" + badReason +
                ", receiverDtb='" + receiverDtb + '\'' +
                ", refundStatus=" + refundStatus +
                ", tradeType=" + tradeType +
                ", salesmanNo=" + salesmanNo +
                ", fullname=" + fullname +
                ", pickerName=" + pickerName +
                ", examinerName=" + examinerName +
                ", consignerName=" + consignerName +
                ", printerName=" + printerName +
                ", packagerName=" + packagerName +
                ", tradeStatus=" + tradeStatus +
                ", tradeNo='" + tradeNo + '\'' +
                ", srcTradeNo='" + srcTradeNo + '\'' +
                ", nickName='" + nickName + '\'' +
                ", customerNo='" + customerNo + '\'' +
                ", customerName='" + customerName + '\'' +
                ", tradeTime='" + tradeTime + '\'' +
                ", payTime='" + payTime + '\'' +
                ", flagName='" + flagName + '\'' +
                ", postAmount=" + postAmount +
                ", idCardType=" + idCardType +
                ", idCard=" + idCard +
                ", shopName='" + shopName + '\'' +
                ", shopNo='" + shopNo + '\'' +
                ", shopRemark='" + shopRemark + '\'' +
                ", status='" + status + '\'' +
                ", invoiceType=" + invoiceType +
                ", invoiceId=" + invoiceId +
                ", codAmount=" + codAmount +
                ", deliveryTerm=" + deliveryTerm +
                ", platformId=" + platformId +
                ", tradeId=" + tradeId +
                ", employeeNo='" + employeeNo + '\'' +
                ", discount=" + discount +
                ", tax=" + tax +
                ", taxRate=" + taxRate +
                ", currency='" + currency + '\'' +
                ", created='" + created + '\'' +
                ", stockCheckTime='" + stockCheckTime + '\'' +
                ", printRemark='" + printRemark + '\'' +
                ", buyerMessage='" + buyerMessage + '\'' +
                ", csRemark='" + csRemark + '\'' +
                ", invoiceTitle='" + invoiceTitle + '\'' +
                ", invoiceContent='" + invoiceContent + '\'' +
                ", postFee=" + postFee +
                ", packageFee=" + packageFee +
                ", receivable=" + receivable +
                ", goodsTotalCost=" + goodsTotalCost +
                ", goodsTotalAmount=" + goodsTotalAmount +
                ", fenxiaoNick='" + fenxiaoNick + '\'' +
                ", tradeLabel='" + tradeLabel + '\'' +
                ", tradeFrom=" + tradeFrom +
                ", picklistNo='" + picklistNo + '\'' +
                ", picklistSeq=" + picklistSeq +
                ", logisticsPrintStatus=" + logisticsPrintStatus +
                ", paid=" + paid +
                ", shopPlatformId=" + shopPlatformId +
                ", subPlatformId=" + subPlatformId +
                ", errorInfo='" + errorInfo + '\'' +
                ", customType=" + customType +
                ", sendbillTemplateId=" + sendbillTemplateId +
                ", customerId=" + customerId +
                ", warehouseType=" + warehouseType +
                ", operatorId=" + operatorId +
                ", outerNo='" + outerNo + '\'' +
                ", consignStatus=" + consignStatus +
                ", goodsTypeCount=" + goodsTypeCount +
                ", calcPostCost=" + calcPostCost +
                ", batchNo='" + batchNo + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", fenxiaoTid='" + fenxiaoTid + '\'' +
                ", fenxiaoNickNo='" + fenxiaoNickNo + '\'' +
                ", anchorName='" + anchorName + '\'' +
                ", assistAchorName='" + assistAchorName + '\'' +
                ", controlAchorName='" + controlAchorName + '\'' +
                ", operationAnchorName='" + operationAnchorName + '\'' +
                ", pickGroupName='" + pickGroupName + '\'' +
                ", logisticsList=" + logisticsList +
                ", detailsList=" + detailsList +
                '}';
    }
}
