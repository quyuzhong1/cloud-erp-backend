
package com.sdk.tms.baohong.api;

import java.math.BigDecimal;
import java.util.ArrayList;
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
public class CreateOrderInfo {

    protected int orderMode;
    protected Integer orderType;
    protected Integer channel;
    protected String trackingNumber;
    @XmlElement(required = true)
    protected String warehouseCode;
    @XmlElement(required = true)
    protected String oabCountry;
    @XmlElement(required = true)
    protected String smCode;
    @XmlElement(required = true)
    protected String referenceNo;
    @XmlElement(required = true)
    protected String oabName;
    protected String oabCompany;
    protected String oabState;
    protected String oabCity;
    protected String oabPostcode;
    @XmlElement(required = true)
    protected String oabStreetAddress1;
    protected String oabStreetAddress2;
    protected String oabPhone;
    protected String oabEmail;
    protected String currency;
    protected String transactionPrice;
    protected String deliveryAddress;
    protected Integer isFBA;
    protected Integer isFBATax;
    protected Integer isPod;
    protected String remark;
    protected String orderStatus;
    protected String iossNo;
    protected String serialNo;
    protected String replacePay;
    protected String relativeReferenceNo;
    protected Integer returnType;
    protected String labelUrl;
    protected Integer buyInsurance;
    protected BigDecimal insuranceRate;
    protected String insuranceName;
    protected String dispatchNotice;
    protected String guarantee;
    protected String tradeMode;
    protected String invoiceBase64;
    protected String grossWeight;
    protected String packNum;
    protected String platform;
    protected List<ShippingInfo> shippingInfo;
    @XmlElement(required = true)
    protected List<ProductDeatil> orderProduct;

    /**
     * 获取orderMode属性的值。
     * 
     */
    public int getOrderMode() {
        return orderMode;
    }

    /**
     * 设置orderMode属性的值。
     * 
     */
    public void setOrderMode(int value) {
        this.orderMode = value;
    }

    /**
     * 获取orderType属性的值。
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getOrderType() {
        return orderType;
    }

    /**
     * 设置orderType属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setOrderType(Integer value) {
        this.orderType = value;
    }

    /**
     * 获取channel属性的值。
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getChannel() {
        return channel;
    }

    /**
     * 设置channel属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setChannel(Integer value) {
        this.channel = value;
    }

    /**
     * 获取trackingNumber属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrackingNumber() {
        return trackingNumber;
    }

    /**
     * 设置trackingNumber属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrackingNumber(String value) {
        this.trackingNumber = value;
    }

    /**
     * 获取warehouseCode属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWarehouseCode() {
        return warehouseCode;
    }

    /**
     * 设置warehouseCode属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWarehouseCode(String value) {
        this.warehouseCode = value;
    }

    /**
     * 获取oabCountry属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOabCountry() {
        return oabCountry;
    }

    /**
     * 设置oabCountry属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOabCountry(String value) {
        this.oabCountry = value;
    }

    /**
     * 获取smCode属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSmCode() {
        return smCode;
    }

    /**
     * 设置smCode属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSmCode(String value) {
        this.smCode = value;
    }

    /**
     * 获取referenceNo属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReferenceNo() {
        return referenceNo;
    }

    /**
     * 设置referenceNo属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReferenceNo(String value) {
        this.referenceNo = value;
    }

    /**
     * 获取oabName属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOabName() {
        return oabName;
    }

    /**
     * 设置oabName属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOabName(String value) {
        this.oabName = value;
    }

    /**
     * 获取oabCompany属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOabCompany() {
        return oabCompany;
    }

    /**
     * 设置oabCompany属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOabCompany(String value) {
        this.oabCompany = value;
    }

    /**
     * 获取oabState属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOabState() {
        return oabState;
    }

    /**
     * 设置oabState属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOabState(String value) {
        this.oabState = value;
    }

    /**
     * 获取oabCity属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOabCity() {
        return oabCity;
    }

    /**
     * 设置oabCity属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOabCity(String value) {
        this.oabCity = value;
    }

    /**
     * 获取oabPostcode属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOabPostcode() {
        return oabPostcode;
    }

    /**
     * 设置oabPostcode属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOabPostcode(String value) {
        this.oabPostcode = value;
    }

    /**
     * 获取oabStreetAddress1属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOabStreetAddress1() {
        return oabStreetAddress1;
    }

    /**
     * 设置oabStreetAddress1属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOabStreetAddress1(String value) {
        this.oabStreetAddress1 = value;
    }

    /**
     * 获取oabStreetAddress2属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOabStreetAddress2() {
        return oabStreetAddress2;
    }

    /**
     * 设置oabStreetAddress2属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOabStreetAddress2(String value) {
        this.oabStreetAddress2 = value;
    }

    /**
     * 获取oabPhone属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOabPhone() {
        return oabPhone;
    }

    /**
     * 设置oabPhone属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOabPhone(String value) {
        this.oabPhone = value;
    }

    /**
     * 获取oabEmail属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOabEmail() {
        return oabEmail;
    }

    /**
     * 设置oabEmail属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOabEmail(String value) {
        this.oabEmail = value;
    }

    /**
     * 获取currency属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * 设置currency属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrency(String value) {
        this.currency = value;
    }

    /**
     * 获取transactionPrice属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactionPrice() {
        return transactionPrice;
    }

    /**
     * 设置transactionPrice属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactionPrice(String value) {
        this.transactionPrice = value;
    }

    /**
     * 获取deliveryAddress属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    /**
     * 设置deliveryAddress属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeliveryAddress(String value) {
        this.deliveryAddress = value;
    }

    /**
     * 获取isFBA属性的值。
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getIsFBA() {
        return isFBA;
    }

    /**
     * 设置isFBA属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setIsFBA(Integer value) {
        this.isFBA = value;
    }

    /**
     * 获取isFBATax属性的值。
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getIsFBATax() {
        return isFBATax;
    }

    /**
     * 设置isFBATax属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setIsFBATax(Integer value) {
        this.isFBATax = value;
    }

    /**
     * 获取isPod属性的值。
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getIsPod() {
        return isPod;
    }

    /**
     * 设置isPod属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setIsPod(Integer value) {
        this.isPod = value;
    }

    /**
     * 获取remark属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置remark属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemark(String value) {
        this.remark = value;
    }

    /**
     * 获取orderStatus属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrderStatus() {
        return orderStatus;
    }

    /**
     * 设置orderStatus属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrderStatus(String value) {
        this.orderStatus = value;
    }

    /**
     * 获取iossNo属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIossNo() {
        return iossNo;
    }

    /**
     * 设置iossNo属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIossNo(String value) {
        this.iossNo = value;
    }

    /**
     * 获取serialNo属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSerialNo() {
        return serialNo;
    }

    /**
     * 设置serialNo属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSerialNo(String value) {
        this.serialNo = value;
    }

    /**
     * 获取replacePay属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplacePay() {
        return replacePay;
    }

    /**
     * 设置replacePay属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplacePay(String value) {
        this.replacePay = value;
    }

    /**
     * 获取relativeReferenceNo属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelativeReferenceNo() {
        return relativeReferenceNo;
    }

    /**
     * 设置relativeReferenceNo属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelativeReferenceNo(String value) {
        this.relativeReferenceNo = value;
    }

    /**
     * 获取returnType属性的值。
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getReturnType() {
        return returnType;
    }

    /**
     * 设置returnType属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setReturnType(Integer value) {
        this.returnType = value;
    }

    /**
     * 获取labelUrl属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabelUrl() {
        return labelUrl;
    }

    /**
     * 设置labelUrl属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabelUrl(String value) {
        this.labelUrl = value;
    }

    /**
     * 获取buyInsurance属性的值。
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getBuyInsurance() {
        return buyInsurance;
    }

    /**
     * 设置buyInsurance属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setBuyInsurance(Integer value) {
        this.buyInsurance = value;
    }

    /**
     * 获取insuranceRate属性的值。
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getInsuranceRate() {
        return insuranceRate;
    }

    /**
     * 设置insuranceRate属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setInsuranceRate(BigDecimal value) {
        this.insuranceRate = value;
    }

    /**
     * 获取insuranceName属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInsuranceName() {
        return insuranceName;
    }

    /**
     * 设置insuranceName属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInsuranceName(String value) {
        this.insuranceName = value;
    }

    /**
     * 获取dispatchNotice属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDispatchNotice() {
        return dispatchNotice;
    }

    /**
     * 设置dispatchNotice属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDispatchNotice(String value) {
        this.dispatchNotice = value;
    }

    /**
     * 获取guarantee属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuarantee() {
        return guarantee;
    }

    /**
     * 设置guarantee属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuarantee(String value) {
        this.guarantee = value;
    }

    /**
     * 获取tradeMode属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTradeMode() {
        return tradeMode;
    }

    /**
     * 设置tradeMode属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTradeMode(String value) {
        this.tradeMode = value;
    }

    /**
     * 获取invoiceBase64属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInvoiceBase64() {
        return invoiceBase64;
    }

    /**
     * 设置invoiceBase64属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInvoiceBase64(String value) {
        this.invoiceBase64 = value;
    }

    /**
     * 获取grossWeight属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGrossWeight() {
        return grossWeight;
    }

    /**
     * 设置grossWeight属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGrossWeight(String value) {
        this.grossWeight = value;
    }

    /**
     * 获取packNum属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPackNum() {
        return packNum;
    }

    /**
     * 设置packNum属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPackNum(String value) {
        this.packNum = value;
    }

    /**
     * 获取platform属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * 设置platform属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlatform(String value) {
        this.platform = value;
    }

    /**
     * Gets the value of the shippingInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the shippingInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getShippingInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ShippingInfo }
     * 
     * 
     */
    public List<ShippingInfo> getShippingInfo() {
        if (shippingInfo == null) {
            shippingInfo = new ArrayList<ShippingInfo>();
        }
        return this.shippingInfo;
    }

    /**
     * Gets the value of the orderProduct property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the orderProduct property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOrderProduct().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProductDeatil }
     * 
     * 
     */
    public List<ProductDeatil> getOrderProduct() {
        if (orderProduct == null) {
            orderProduct = new ArrayList<ProductDeatil>();
        }
        return this.orderProduct;
    }

}
