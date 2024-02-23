
package com.sdk.tms.baohong.api.asn;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>ASNInfo complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="ASNInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="warehouseCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="refCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="isDelivery" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="iePort" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="formType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="trafName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="wrapType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="packNo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="trafMode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tradeMode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transMode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="receiveMode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="roughWeight" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="receivingStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="receivingDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ASNItems" type="{http://www.example.org/ServiceForAsn/}ASNItemsType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ASNInfo", propOrder = {
    "warehouseCode",
    "refCode",
    "isDelivery",
    "iePort",
    "formType",
    "trafName",
    "wrapType",
    "packNo",
    "trafMode",
    "tradeMode",
    "transMode",
    "receiveMode",
    "roughWeight",
    "receivingStatus",
    "receivingDescription",
    "asnItems"
})
public class ASNInfo {

    @XmlElement(required = true)
    protected String warehouseCode;
    @XmlElement(required = true)
    protected String refCode;
    @XmlElement(required = true)
    protected String isDelivery;
    protected String iePort;
    protected String formType;
    @XmlElement(required = true)
    protected String trafName;
    @XmlElement(required = true)
    protected String wrapType;
    @XmlElement(required = true)
    protected String packNo;
    protected String trafMode;
    protected String tradeMode;
    protected String transMode;
    @XmlElement(required = true)
    protected String receiveMode;
    @XmlElement(required = true)
    protected String roughWeight;
    protected String receivingStatus;
    protected String receivingDescription;
    @XmlElement(name = "ASNItems", required = true)
    protected List<ASNItemsType> asnItems;

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
     * ��ȡrefCode���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefCode() {
        return refCode;
    }

    /**
     * ����refCode���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefCode(String value) {
        this.refCode = value;
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
     * Gets the value of the asnItems property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the asnItems property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getASNItems().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ASNItemsType }
     * 
     * 
     */
    public List<ASNItemsType> getASNItems() {
        if (asnItems == null) {
            asnItems = new ArrayList<ASNItemsType>();
        }
        return this.asnItems;
    }

}
