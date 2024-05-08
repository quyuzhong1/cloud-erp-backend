
package com.sdk.tms.baohong.api.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>CreateOrderInfo complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="CreateOrderInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="orderMode" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="orderType" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="channel" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="trackingNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="warehouseCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="oabCountry" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="smCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="referenceNo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="oabName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="oabCompany" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabCity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabPostcode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabStreetAddress1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="oabStreetAddress2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabPhone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabEmail" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="currency" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transactionPrice" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="deliveryAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isFBA" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="isFBATax" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="isPod" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="remark" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="orderStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="iossNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="serialNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="replacePay" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="relativeReferenceNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="returnType" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="labelUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="buyInsurance" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="insuranceRate" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="insuranceName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dispatchNotice" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="guarantee" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tradeMode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="invoiceBase64" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="grossWeight" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="packNum" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="platform" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="shippingInfo" type="{http://www.example.org/ServiceForOrder/}shippingInfo" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="orderProduct" type="{http://www.example.org/ServiceForOrder/}productDeatil" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateOrderInfo", propOrder = {
    "orderMode",
    "orderType",
    "channel",
    "trackingNumber",
    "warehouseCode",
    "oabCountry",
    "smCode",
    "referenceNo",
    "oabName",
    "oabCompany",
    "oabState",
    "oabCity",
    "oabPostcode",
    "oabStreetAddress1",
    "oabStreetAddress2",
    "oabPhone",
    "oabEmail",
    "currency",
    "transactionPrice",
    "deliveryAddress",
    "isFBA",
    "isFBATax",
    "isPod",
    "remark",
    "orderStatus",
    "iossNo",
    "serialNo",
    "replacePay",
    "relativeReferenceNo",
    "returnType",
    "labelUrl",
    "buyInsurance",
    "insuranceRate",
    "insuranceName",
    "dispatchNotice",
    "guarantee",
    "tradeMode",
    "invoiceBase64",
    "grossWeight",
    "packNum",
    "platform",
    "shippingInfo",
    "orderProduct"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CreateOrderInfo {

    /**
     * 订单模型：
     * 0:备货模式
     * 1:集货模式
     * 默认 1
     */
    @Builder.Default
    protected int orderMode = 1;

    protected Integer orderType;
    /**
     * 使用自有渠道
     * 0:否
     * 1:是
     * orderMode为1时必填
     * 默认 1
     */
    @Builder.Default
    protected Integer channel = 0;

    /**
     * 服务商单号
     * channel为1时必填
     */
    protected String trackingNumber;

    /**
     * 交货仓库
     */
    @XmlElement(required = true)
    @Builder.Default
    protected String warehouseCode = "sz01";

    /**
     * 收件人国家
     */
    @XmlElement(required = true)
    protected String oabCountry;

    /**
     * 运输方式代码
     */
    @XmlElement(required = true)
    protected String smCode;

    /**
     * 交易订单号
     */
    @XmlElement(required = true)
    protected String referenceNo;

    /**
     * 收件人姓名
     */
    @XmlElement(required = true)
    protected String oabName;

    /**
     * 收件人公司名
     */
    protected String oabCompany;

    /**
     * 收件人州/区域
     */
    protected String oabState;

    /**
     * 收件人城市
     */
    protected String oabCity;

    /**
     * 收件人邮编
     */
    protected String oabPostcode;

    /**
     * 收件人地址1
     */
    @XmlElement(required = true)
    protected String oabStreetAddress1;

    /**
     * 收件人地址2
     */
    protected String oabStreetAddress2;

    /**
     * 收件人电话
     */
    protected String oabPhone;

    /**
     * 电子邮件
     */
    protected String oabEmail;

    protected String currency;
    protected String transactionPrice;

    /**
     * 派送地址,集货模式的订单才有，自有渠道 必填
     */
    protected String deliveryAddress;

    /**
     * 是否FBA，HKDHL必填
     * 0，否；
     * 1，是；
     */
    protected Integer isFBA;

    /**
     * 目的地关税
     */
    protected Integer isFBATax;

    /**
     * POD签名服务费0无1有
     */
    protected Integer isPod;

    /**
     * 备注
     */
    protected String remark;

    /**
     * 提交订单状态
     * 1,草稿;
     * 2,确认;
     * 4,已提交;
     * 备注：集货模式不可提交至4状态
     */
    protected String orderStatus;

    /**
     * IOSS 号
     */
    protected String iossNo;

    /**
     * 平台订单号
     */
    protected String serialNo;

    /**
     * 是否代缴：0否，1是
     */
    protected String replacePay;


    protected String relativeReferenceNo;
    protected Integer returnType;
    protected String labelUrl;
    /**
     * 是否购买运单保险。0：不购买； 1：购买
     */
    protected Integer buyInsurance;
    /**
     * 投保金额率，取值范围在1<= insuranceRate  <= 1.1，保留2位有效小数位
     */
    protected BigDecimal insuranceRate;
    /**
     * 运单保险名称，目前有四种保险：货物运输保险、订单延误保险、订单取消保险、产品责任保险
     */
    protected String insuranceName;
    /**
     * 派送通知服务费，订单运输方式为BH-DHL-HKDELIVERY时,此项必填；0:否1:是
     */
    protected String dispatchNotice;
    /**
     * 保障服务费，订单运输方式为BH-DHL-HKDELIVERY时,此项必填；0:
     */
    protected String guarantee;
    protected String tradeMode;
    /**
     * BH-HKDHL正本发票的base64 码BH-HKDHL到以下国家及地区需上传正本发票(invoiceBase64字段不超过2M);阿根廷,巴西,保加利亚,厄瓜多尔,卡塔尔,科威特,黎巴嫩,罗马尼亚,秘鲁,沙特阿拉伯,台湾省,坦桑尼亚,土耳其,危地马拉,伊拉克,印度,智利,阿联酋
     */
    protected String invoiceBase64;
    /**
     * 订单包裹重量
     */
    protected String grossWeight;
    /**
     * 订单包裹的包袋号
     */
    protected String packNum;
    protected String platform;

    /**
     * DHL空运才需要填
     */
    protected List<ShippingInfo> shippingInfo;


    @XmlElement(required = true)
    protected List<ProductDeatil> orderProduct;

}
