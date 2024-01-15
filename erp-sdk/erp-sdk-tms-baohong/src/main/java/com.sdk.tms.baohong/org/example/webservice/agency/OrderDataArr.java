
package com.sdk.tms.baohong.org.example.webservice.agency;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>orderDataArr complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="orderDataArr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="orderCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="orderType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="warehouseCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tracking_number" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="smCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="orderStatus" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="referenceNo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneeCountry" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneeState" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneeCity" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneeName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneeCompany" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneePostcode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneeAddress1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneeAddress2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneePhone" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consigneeEmail" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="currency" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="transactionPrice" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="deliveryAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isFBA" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="isFBATax" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="isPod" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Remark" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="shipTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OrderDetail" type="{http://www.example.org/ServiceForOrder/}orderDetail" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "orderDataArr", propOrder = {
    "orderCode",
    "orderType",
    "warehouseCode",
    "trackingNumber",
    "smCode",
    "orderStatus",
    "referenceNo",
    "consigneeCountry",
    "consigneeState",
    "consigneeCity",
    "consigneeName",
    "consigneeCompany",
    "consigneePostcode",
    "consigneeAddress1",
    "consigneeAddress2",
    "consigneePhone",
    "consigneeEmail",
    "currency",
    "transactionPrice",
    "deliveryAddress",
    "isFBA",
    "isFBATax",
    "isPod",
    "remark",
    "shipTime",
    "orderDetail"
})
public class OrderDataArr {

    @XmlElement(required = true)
    protected String orderCode;
    @XmlElement(required = true)
    protected String orderType;
    @XmlElement(required = true)
    protected String warehouseCode;
    @XmlElement(name = "tracking_number")
    protected String trackingNumber;
    @XmlElement(required = true)
    protected String smCode;
    @XmlElement(required = true)
    protected String orderStatus;
    @XmlElement(required = true)
    protected String referenceNo;
    @XmlElement(required = true)
    protected String consigneeCountry;
    @XmlElement(required = true)
    protected String consigneeState;
    @XmlElement(required = true)
    protected String consigneeCity;
    @XmlElement(required = true)
    protected String consigneeName;
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
    @XmlElement(required = true)
    protected String currency;
    @XmlElement(required = true)
    protected String transactionPrice;
    protected String deliveryAddress;
    protected Integer isFBA;
    protected Integer isFBATax;
    protected String isPod;
    @XmlElement(name = "Remark", required = true)
    protected String remark;
    protected String shipTime;
    @XmlElement(name = "OrderDetail")
    protected List<OrderDetail> orderDetail;

    /**
     * 获取orderCode属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrderCode() {
        return orderCode;
    }

    /**
     * 设置orderCode属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrderCode(String value) {
        this.orderCode = value;
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
     * 获取consigneeState属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsigneeState() {
        return consigneeState;
    }

    /**
     * 设置consigneeState属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsigneeState(String value) {
        this.consigneeState = value;
    }

    /**
     * 获取consigneeCity属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsigneeCity() {
        return consigneeCity;
    }

    /**
     * 设置consigneeCity属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsigneeCity(String value) {
        this.consigneeCity = value;
    }

    /**
     * 获取consigneeName属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsigneeName() {
        return consigneeName;
    }

    /**
     * 设置consigneeName属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsigneeName(String value) {
        this.consigneeName = value;
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
     *     {@link String }
     *     
     */
    public String getIsPod() {
        return isPod;
    }

    /**
     * 设置isPod属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsPod(String value) {
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
     * {@link OrderDetail }
     * 
     * 
     */
    public List<OrderDetail> getOrderDetail() {
        if (orderDetail == null) {
            orderDetail = new ArrayList<OrderDetail>();
        }
        return this.orderDetail;
    }

}
