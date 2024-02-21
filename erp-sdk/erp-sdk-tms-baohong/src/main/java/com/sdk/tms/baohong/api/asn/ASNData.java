
package com.sdk.tms.baohong.api.asn;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>ASNData complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="ASNData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="receivingCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="referenceNo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="warehouseCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="receivingType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="receivingStatus" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="contacter" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="contactPhone" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="receivingDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="receivingAddtime" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="receivingUpdatetime" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="iePort" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="formType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="trafName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="wrapType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="packNo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="trafMode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tradeMode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="transMode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="edaDate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="expectedDate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="receiveMode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="roughWeight" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="netWeight" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="isDelivery" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="productDetail" type="{http://www.example.org/ServiceForAsn/}productDetail" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="OrderDetail" type="{http://www.example.org/ServiceForAsn/}OrderDetail" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="receivingDetail" type="{http://www.example.org/ServiceForAsn/}receivingDetail" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ASNData", propOrder = {
    "receivingCode",
    "referenceNo",
    "warehouseCode",
    "customerCode",
    "receivingType",
    "receivingStatus",
    "contacter",
    "contactPhone",
    "receivingDescription",
    "receivingAddtime",
    "receivingUpdatetime",
    "iePort",
    "formType",
    "trafName",
    "wrapType",
    "packNo",
    "trafMode",
    "tradeMode",
    "transMode",
    "edaDate",
    "expectedDate",
    "receiveMode",
    "roughWeight",
    "netWeight",
    "isDelivery",
    "productDetail",
    "orderDetail",
    "receivingDetail"
})
public class ASNData {

    @XmlElement(required = true)
    protected String receivingCode;
    @XmlElement(required = true)
    protected String referenceNo;
    protected String warehouseCode;
    @XmlElement(required = true)
    protected String customerCode;
    @XmlElement(required = true)
    protected String receivingType;
    @XmlElement(required = true)
    protected String receivingStatus;
    @XmlElement(required = true)
    protected String contacter;
    @XmlElement(required = true)
    protected String contactPhone;
    protected String receivingDescription;
    @XmlElement(required = true)
    protected String receivingAddtime;
    @XmlElement(required = true)
    protected String receivingUpdatetime;
    @XmlElement(required = true)
    protected String iePort;
    @XmlElement(required = true)
    protected String formType;
    @XmlElement(required = true)
    protected String trafName;
    @XmlElement(required = true)
    protected String wrapType;
    @XmlElement(required = true)
    protected String packNo;
    @XmlElement(required = true)
    protected String trafMode;
    @XmlElement(required = true)
    protected String tradeMode;
    @XmlElement(required = true)
    protected String transMode;
    @XmlElement(required = true)
    protected String edaDate;
    @XmlElement(required = true)
    protected String expectedDate;
    @XmlElement(required = true)
    protected String receiveMode;
    @XmlElement(required = true)
    protected String roughWeight;
    @XmlElement(required = true)
    protected String netWeight;
    @XmlElement(required = true)
    protected String isDelivery;
    protected List<ProductDetail> productDetail;
    @XmlElement(name = "OrderDetail")
    protected List<OrderDetail> orderDetail;
    protected List<ReceivingDetail> receivingDetail;

    /**
     * ��ȡreceivingCode���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceivingCode() {
        return receivingCode;
    }

    /**
     * ����receivingCode���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceivingCode(String value) {
        this.receivingCode = value;
    }

    /**
     * ��ȡreferenceNo���Ե�ֵ��
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
     * ����referenceNo���Ե�ֵ��
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
     * ��ȡwarehouseCode���Ե�ֵ��
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
     * ����warehouseCode���Ե�ֵ��
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
     * ��ȡcustomerCode���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerCode() {
        return customerCode;
    }

    /**
     * ����customerCode���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerCode(String value) {
        this.customerCode = value;
    }

    /**
     * ��ȡreceivingType���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceivingType() {
        return receivingType;
    }

    /**
     * ����receivingType���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceivingType(String value) {
        this.receivingType = value;
    }

    /**
     * ��ȡreceivingStatus���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceivingStatus() {
        return receivingStatus;
    }

    /**
     * ����receivingStatus���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceivingStatus(String value) {
        this.receivingStatus = value;
    }

    /**
     * ��ȡcontacter���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContacter() {
        return contacter;
    }

    /**
     * ����contacter���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContacter(String value) {
        this.contacter = value;
    }

    /**
     * ��ȡcontactPhone���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContactPhone() {
        return contactPhone;
    }

    /**
     * ����contactPhone���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContactPhone(String value) {
        this.contactPhone = value;
    }

    /**
     * ��ȡreceivingDescription���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceivingDescription() {
        return receivingDescription;
    }

    /**
     * ����receivingDescription���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceivingDescription(String value) {
        this.receivingDescription = value;
    }

    /**
     * ��ȡreceivingAddtime���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceivingAddtime() {
        return receivingAddtime;
    }

    /**
     * ����receivingAddtime���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceivingAddtime(String value) {
        this.receivingAddtime = value;
    }

    /**
     * ��ȡreceivingUpdatetime���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceivingUpdatetime() {
        return receivingUpdatetime;
    }

    /**
     * ����receivingUpdatetime���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceivingUpdatetime(String value) {
        this.receivingUpdatetime = value;
    }

    /**
     * ��ȡiePort���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIePort() {
        return iePort;
    }

    /**
     * ����iePort���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIePort(String value) {
        this.iePort = value;
    }

    /**
     * ��ȡformType���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormType() {
        return formType;
    }

    /**
     * ����formType���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormType(String value) {
        this.formType = value;
    }

    /**
     * ��ȡtrafName���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrafName() {
        return trafName;
    }

    /**
     * ����trafName���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrafName(String value) {
        this.trafName = value;
    }

    /**
     * ��ȡwrapType���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWrapType() {
        return wrapType;
    }

    /**
     * ����wrapType���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWrapType(String value) {
        this.wrapType = value;
    }

    /**
     * ��ȡpackNo���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPackNo() {
        return packNo;
    }

    /**
     * ����packNo���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPackNo(String value) {
        this.packNo = value;
    }

    /**
     * ��ȡtrafMode���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrafMode() {
        return trafMode;
    }

    /**
     * ����trafMode���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrafMode(String value) {
        this.trafMode = value;
    }

    /**
     * ��ȡtradeMode���Ե�ֵ��
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
     * ����tradeMode���Ե�ֵ��
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
     * ��ȡtransMode���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransMode() {
        return transMode;
    }

    /**
     * ����transMode���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransMode(String value) {
        this.transMode = value;
    }

    /**
     * ��ȡedaDate���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEdaDate() {
        return edaDate;
    }

    /**
     * ����edaDate���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEdaDate(String value) {
        this.edaDate = value;
    }

    /**
     * ��ȡexpectedDate���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExpectedDate() {
        return expectedDate;
    }

    /**
     * ����expectedDate���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExpectedDate(String value) {
        this.expectedDate = value;
    }

    /**
     * ��ȡreceiveMode���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceiveMode() {
        return receiveMode;
    }

    /**
     * ����receiveMode���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceiveMode(String value) {
        this.receiveMode = value;
    }

    /**
     * ��ȡroughWeight���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRoughWeight() {
        return roughWeight;
    }

    /**
     * ����roughWeight���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRoughWeight(String value) {
        this.roughWeight = value;
    }

    /**
     * ��ȡnetWeight���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNetWeight() {
        return netWeight;
    }

    /**
     * ����netWeight���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNetWeight(String value) {
        this.netWeight = value;
    }

    /**
     * ��ȡisDelivery���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsDelivery() {
        return isDelivery;
    }

    /**
     * ����isDelivery���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsDelivery(String value) {
        this.isDelivery = value;
    }

    /**
     * Gets the value of the productDetail property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the productDetail property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProductDetail().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProductDetail }
     * 
     * 
     */
    public List<ProductDetail> getProductDetail() {
        if (productDetail == null) {
            productDetail = new ArrayList<ProductDetail>();
        }
        return this.productDetail;
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

    /**
     * Gets the value of the receivingDetail property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the receivingDetail property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReceivingDetail().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReceivingDetail }
     * 
     * 
     */
    public List<ReceivingDetail> getReceivingDetail() {
        if (receivingDetail == null) {
            receivingDetail = new ArrayList<ReceivingDetail>();
        }
        return this.receivingDetail;
    }

}
