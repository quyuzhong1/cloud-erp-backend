
package com.sdk.tms.baohong.org.example.webservice.agency;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>UpdateOrderInfo complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="UpdateOrderInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="orderCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="changeOrder" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="trackingNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="warehouseCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabCountry" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="smCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="referenceNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabLastname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabFirstname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabCompany" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabCity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabPostcode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabStreetAddress1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabStreetAddress2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabPhone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oabEmail" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="currency" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transactionPrice" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="deliveryAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isFBA" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="isFBATax" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="remark" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="orderStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isPod" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="buyInsurance" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="insuranceRate" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="insuranceName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="orderProduct" type="{http://www.example.org/ServiceForOrder/}productDeatil" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateOrderInfo", propOrder = {
    "orderCode",
    "changeOrder",
    "trackingNumber",
    "warehouseCode",
    "oabCountry",
    "smCode",
    "referenceNo",
    "oabLastname",
    "oabFirstname",
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
    "remark",
    "orderStatus",
    "isPod",
    "buyInsurance",
    "insuranceRate",
    "insuranceName",
    "orderProduct"
})
public class UpdateOrderInfo {

    @XmlElement(required = true)
    protected String orderCode;
    protected String changeOrder;
    protected String trackingNumber;
    protected String warehouseCode;
    protected String oabCountry;
    protected String smCode;
    protected String referenceNo;
    protected String oabLastname;
    protected String oabFirstname;
    protected String oabCompany;
    protected String oabState;
    protected String oabCity;
    protected String oabPostcode;
    protected String oabStreetAddress1;
    protected String oabStreetAddress2;
    protected String oabPhone;
    protected String oabEmail;
    protected String currency;
    protected String transactionPrice;
    protected String deliveryAddress;
    protected Integer isFBA;
    protected Integer isFBATax;
    protected String remark;
    protected String orderStatus;
    protected Integer isPod;
    protected Integer buyInsurance;
    protected BigDecimal insuranceRate;
    protected String insuranceName;
    protected List<ProductDeatil> orderProduct;

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
     * 获取oabLastname属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOabLastname() {
        return oabLastname;
    }

    /**
     * 设置oabLastname属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOabLastname(String value) {
        this.oabLastname = value;
    }

    /**
     * 获取oabFirstname属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOabFirstname() {
        return oabFirstname;
    }

    /**
     * 设置oabFirstname属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOabFirstname(String value) {
        this.oabFirstname = value;
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
