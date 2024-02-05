
package com.sdk.tms.baohong.api.asn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>receivingDetail complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="receivingDetail">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="receiving_code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="product_barcode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="op_declared_value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rd_receiving_qty" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rd_putaway_qty" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rd_received_qty" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="product_sku" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "receivingDetail", propOrder = {
    "receivingCode",
    "productBarcode",
    "opDeclaredValue",
    "rdReceivingQty",
    "rdPutawayQty",
    "rdReceivedQty",
    "productSku"
})
public class ReceivingDetail {

    @XmlElement(name = "receiving_code")
    protected String receivingCode;
    @XmlElement(name = "product_barcode")
    protected String productBarcode;
    @XmlElement(name = "op_declared_value")
    protected String opDeclaredValue;
    @XmlElement(name = "rd_receiving_qty")
    protected String rdReceivingQty;
    @XmlElement(name = "rd_putaway_qty")
    protected String rdPutawayQty;
    @XmlElement(name = "rd_received_qty")
    protected String rdReceivedQty;
    @XmlElement(name = "product_sku")
    protected String productSku;

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
     * ��ȡproductBarcode���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductBarcode() {
        return productBarcode;
    }

    /**
     * ����productBarcode���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductBarcode(String value) {
        this.productBarcode = value;
    }

    /**
     * ��ȡopDeclaredValue���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpDeclaredValue() {
        return opDeclaredValue;
    }

    /**
     * ����opDeclaredValue���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpDeclaredValue(String value) {
        this.opDeclaredValue = value;
    }

    /**
     * ��ȡrdReceivingQty���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRdReceivingQty() {
        return rdReceivingQty;
    }

    /**
     * ����rdReceivingQty���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRdReceivingQty(String value) {
        this.rdReceivingQty = value;
    }

    /**
     * ��ȡrdPutawayQty���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRdPutawayQty() {
        return rdPutawayQty;
    }

    /**
     * ����rdPutawayQty���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRdPutawayQty(String value) {
        this.rdPutawayQty = value;
    }

    /**
     * ��ȡrdReceivedQty���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRdReceivedQty() {
        return rdReceivedQty;
    }

    /**
     * ����rdReceivedQty���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRdReceivedQty(String value) {
        this.rdReceivedQty = value;
    }

    /**
     * ��ȡproductSku���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductSku() {
        return productSku;
    }

    /**
     * ����productSku���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductSku(String value) {
        this.productSku = value;
    }

}
