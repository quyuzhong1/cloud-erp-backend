
package com.sdk.tms.baohong.api.product;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>inventoryRow complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="inventoryRow">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="skuNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="warehouseCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="onwayQty" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pendingQty" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sellableQty" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="unsellableQty" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reservedQty" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="shippedQty" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "inventoryRow", propOrder = {
    "skuNo",
    "warehouseCode",
    "onwayQty",
    "pendingQty",
    "sellableQty",
    "unsellableQty",
    "reservedQty",
    "shippedQty"
})
public class InventoryRow {

    protected String skuNo;
    protected String warehouseCode;
    protected String onwayQty;
    protected String pendingQty;
    protected String sellableQty;
    protected String unsellableQty;
    @XmlElement(required = true)
    protected String reservedQty;
    protected String shippedQty;

    /**
     * ��ȡskuNo���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSkuNo() {
        return skuNo;
    }

    /**
     * ����skuNo���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSkuNo(String value) {
        this.skuNo = value;
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
     * ��ȡonwayQty���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnwayQty() {
        return onwayQty;
    }

    /**
     * ����onwayQty���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnwayQty(String value) {
        this.onwayQty = value;
    }

    /**
     * ��ȡpendingQty���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPendingQty() {
        return pendingQty;
    }

    /**
     * ����pendingQty���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPendingQty(String value) {
        this.pendingQty = value;
    }

    /**
     * ��ȡsellableQty���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSellableQty() {
        return sellableQty;
    }

    /**
     * ����sellableQty���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSellableQty(String value) {
        this.sellableQty = value;
    }

    /**
     * ��ȡunsellableQty���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnsellableQty() {
        return unsellableQty;
    }

    /**
     * ����unsellableQty���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnsellableQty(String value) {
        this.unsellableQty = value;
    }

    /**
     * ��ȡreservedQty���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReservedQty() {
        return reservedQty;
    }

    /**
     * ����reservedQty���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReservedQty(String value) {
        this.reservedQty = value;
    }

    /**
     * ��ȡshippedQty���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShippedQty() {
        return shippedQty;
    }

    /**
     * ����shippedQty���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShippedQty(String value) {
        this.shippedQty = value;
    }

}
