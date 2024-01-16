
package com.sdk.tms.baohong.api;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>OrderInfo complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="OrderInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="referenceNo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="orderType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="changeOrder" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="smCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Tracking_no" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="provinceName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneeCountry" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneeLastname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneeFirstname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneeCompany" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneePostcode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneeAddress1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneeAddress2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneePhone" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneeEmail" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="isFBA" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="isFBATax" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="grossWt" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="currency" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="charge" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="shippingPremiumFee" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Trade_platform" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="addTime" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="shipTime" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="signTime" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="orderDetail" type="{http://www.example.org/ServiceForOrder/}sborderDetailType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderInfo", propOrder = {
    "referenceNo",
    "orderType",
    "changeOrder",
    "smCode",
    "trackingNo",
    "provinceName",
    "consigneeCountry",
    "consigneeLastname",
    "consigneeFirstname",
    "consigneeCompany",
    "consigneePostcode",
    "consigneeAddress1",
    "consigneeAddress2",
    "consigneePhone",
    "consigneeEmail",
    "isFBA",
    "isFBATax",
    "grossWt",
    "currency",
    "charge",
    "shippingPremiumFee",
    "tradePlatform",
    "addTime",
    "shipTime",
    "signTime",
    "orderDetail"
})
public class OrderInfo {

    @XmlElement(required = true)
    protected String referenceNo;
    @XmlElement(required = true)
    protected String orderType;
    @XmlElement(required = true)
    protected String changeOrder;
    @XmlElement(required = true)
    protected String smCode;
    @XmlElement(name = "Tracking_no", required = true)
    protected String trackingNo;
    @XmlElement(required = true)
    protected String provinceName;
    @XmlElement(required = true)
    protected String consigneeCountry;
    @XmlElement(required = true)
    protected String consigneeLastname;
    @XmlElement(required = true)
    protected String consigneeFirstname;
    @XmlElement(required = true)
    protected String consigneeCompany;
    @XmlElement(required = true)
    protected String consigneePostcode;
    @XmlElement(required = true)
    protected String consigneeAddress1;
    @XmlElement(required = true)
    protected String consigneeAddress2;
    @XmlElement(required = true)
    protected String consigneePhone;
    @XmlElement(required = true)
    protected String consigneeEmail;
    protected int isFBA;
    protected int isFBATax;
    @XmlElement(required = true)
    protected String grossWt;
    @XmlElement(required = true)
    protected String currency;
    @XmlElement(required = true)
    protected String charge;
    @XmlElement(required = true)
    protected String shippingPremiumFee;
    @XmlElement(name = "Trade_platform", required = true)
    protected String tradePlatform;
    @XmlElement(required = true)
    protected String addTime;
    @XmlElement(required = true)
    protected String shipTime;
    @XmlElement(required = true)
    protected String signTime;
    @XmlElement(required = true)
    protected List<SborderDetailType> orderDetail;

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
     * 获取orderType属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrderType() {
        return orderType;
    }

    /**
     * 设置orderType属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrderType(String value) {
        this.orderType = value;
    }

    /**
     * 获取changeOrder属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChangeOrder() {
        return changeOrder;
    }

    /**
     * 设置changeOrder属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChangeOrder(String value) {
        this.changeOrder = value;
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
     * 获取trackingNo属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrackingNo() {
        return trackingNo;
    }

    /**
     * 设置trackingNo属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrackingNo(String value) {
        this.trackingNo = value;
    }

    /**
     * 获取provinceName属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProvinceName() {
        return provinceName;
    }

    /**
     * 设置provinceName属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProvinceName(String value) {
        this.provinceName = value;
    }

    /**
     * 获取consigneeCountry属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsigneeCountry() {
        return consigneeCountry;
    }

    /**
     * 设置consigneeCountry属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsigneeCountry(String value) {
        this.consigneeCountry = value;
    }

    /**
     * 获取consigneeLastname属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsigneeLastname() {
        return consigneeLastname;
    }

    /**
     * 设置consigneeLastname属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsigneeLastname(String value) {
        this.consigneeLastname = value;
    }

    /**
     * 获取consigneeFirstname属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsigneeFirstname() {
        return consigneeFirstname;
    }

    /**
     * 设置consigneeFirstname属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsigneeFirstname(String value) {
        this.consigneeFirstname = value;
    }

    /**
     * 获取consigneeCompany属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsigneeCompany() {
        return consigneeCompany;
    }

    /**
     * 设置consigneeCompany属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsigneeCompany(String value) {
        this.consigneeCompany = value;
    }

    /**
     * 获取consigneePostcode属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsigneePostcode() {
        return consigneePostcode;
    }

    /**
     * 设置consigneePostcode属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsigneePostcode(String value) {
        this.consigneePostcode = value;
    }

    /**
     * 获取consigneeAddress1属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsigneeAddress1() {
        return consigneeAddress1;
    }

    /**
     * 设置consigneeAddress1属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsigneeAddress1(String value) {
        this.consigneeAddress1 = value;
    }

    /**
     * 获取consigneeAddress2属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsigneeAddress2() {
        return consigneeAddress2;
    }

    /**
     * 设置consigneeAddress2属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsigneeAddress2(String value) {
        this.consigneeAddress2 = value;
    }

    /**
     * 获取consigneePhone属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsigneePhone() {
        return consigneePhone;
    }

    /**
     * 设置consigneePhone属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsigneePhone(String value) {
        this.consigneePhone = value;
    }

    /**
     * 获取consigneeEmail属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsigneeEmail() {
        return consigneeEmail;
    }

    /**
     * 设置consigneeEmail属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsigneeEmail(String value) {
        this.consigneeEmail = value;
    }

    /**
     * 获取isFBA属性的值。
     * 
     */
    public int getIsFBA() {
        return isFBA;
    }

    /**
     * 设置isFBA属性的值。
     * 
     */
    public void setIsFBA(int value) {
        this.isFBA = value;
    }

    /**
     * 获取isFBATax属性的值。
     * 
     */
    public int getIsFBATax() {
        return isFBATax;
    }

    /**
     * 设置isFBATax属性的值。
     * 
     */
    public void setIsFBATax(int value) {
        this.isFBATax = value;
    }

    /**
     * 获取grossWt属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGrossWt() {
        return grossWt;
    }

    /**
     * 设置grossWt属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGrossWt(String value) {
        this.grossWt = value;
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
     * 获取charge属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCharge() {
        return charge;
    }

    /**
     * 设置charge属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCharge(String value) {
        this.charge = value;
    }

    /**
     * 获取shippingPremiumFee属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShippingPremiumFee() {
        return shippingPremiumFee;
    }

    /**
     * 设置shippingPremiumFee属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShippingPremiumFee(String value) {
        this.shippingPremiumFee = value;
    }

    /**
     * 获取tradePlatform属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTradePlatform() {
        return tradePlatform;
    }

    /**
     * 设置tradePlatform属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTradePlatform(String value) {
        this.tradePlatform = value;
    }

    /**
     * 获取addTime属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddTime() {
        return addTime;
    }

    /**
     * 设置addTime属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddTime(String value) {
        this.addTime = value;
    }

    /**
     * 获取shipTime属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShipTime() {
        return shipTime;
    }

    /**
     * 设置shipTime属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShipTime(String value) {
        this.shipTime = value;
    }

    /**
     * 获取signTime属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSignTime() {
        return signTime;
    }

    /**
     * 设置signTime属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSignTime(String value) {
        this.signTime = value;
    }

    /**
     * Gets the value of the orderDetail property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the orderDetail property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOrderDetail().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SborderDetailType }
     * 
     * 
     */
    public List<SborderDetailType> getOrderDetail() {
        if (orderDetail == null) {
            orderDetail = new ArrayList<SborderDetailType>();
        }
        return this.orderDetail;
    }

}
