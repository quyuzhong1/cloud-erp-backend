
package com.sdk.tms.baohong.api.product;

import lombok.ToString;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>productRow complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="productRow">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="goods_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="product_sku" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="product_barcode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="product_title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="product_title_en" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="product_status" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="hs_goods_name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="hs_code" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="product_weight" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="product_declared_value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="is_accessories" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="has_invoice" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="currency_code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="model_serial" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="model_serial2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reject_reason" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "productRow", propOrder = {
    "goodsId",
    "productSku",
    "productBarcode",
    "productTitle",
    "productTitleEn",
    "productStatus",
    "hsGoodsName",
    "hsCode",
    "productWeight",
    "productDeclaredValue",
    "isAccessories",
    "hasInvoice",
    "currencyCode",
    "modelSerial",
    "modelSerial2",
    "rejectReason"
})
@ToString
public class ProductRow {

    @XmlElement(name = "goods_id", required = true)
    protected String goodsId;
    @XmlElement(name = "product_sku", required = true)
    protected String productSku;
    @XmlElement(name = "product_barcode", required = true)
    protected String productBarcode;
    @XmlElement(name = "product_title")
    protected String productTitle;
    @XmlElement(name = "product_title_en")
    protected String productTitleEn;
    @XmlElement(name = "product_status")
    protected String productStatus;
    @XmlElement(name = "hs_goods_name", required = true)
    protected String hsGoodsName;
    @XmlElement(name = "hs_code", required = true)
    protected String hsCode;
    @XmlElement(name = "product_weight")
    protected String productWeight;
    @XmlElement(name = "product_declared_value")
    protected String productDeclaredValue;
    @XmlElement(name = "is_accessories")
    protected String isAccessories;
    @XmlElement(name = "has_invoice")
    protected String hasInvoice;
    @XmlElement(name = "currency_code")
    protected String currencyCode;
    @XmlElement(name = "model_serial")
    protected String modelSerial;
    @XmlElement(name = "model_serial2")
    protected String modelSerial2;
    @XmlElement(name = "reject_reason")
    protected String rejectReason;

    /**
     * ��ȡgoodsId���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGoodsId() {
        return goodsId;
    }

    /**
     * ����goodsId���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGoodsId(String value) {
        this.goodsId = value;
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
     * ��ȡproductTitle���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductTitle() {
        return productTitle;
    }

    /**
     * ����productTitle���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductTitle(String value) {
        this.productTitle = value;
    }

    /**
     * ��ȡproductTitleEn���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductTitleEn() {
        return productTitleEn;
    }

    /**
     * ����productTitleEn���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductTitleEn(String value) {
        this.productTitleEn = value;
    }

    /**
     * ��ȡproductStatus���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductStatus() {
        return productStatus;
    }

    /**
     * ����productStatus���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductStatus(String value) {
        this.productStatus = value;
    }

    /**
     * ��ȡhsGoodsName���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHsGoodsName() {
        return hsGoodsName;
    }

    /**
     * ����hsGoodsName���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHsGoodsName(String value) {
        this.hsGoodsName = value;
    }

    /**
     * ��ȡhsCode���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHsCode() {
        return hsCode;
    }

    /**
     * ����hsCode���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHsCode(String value) {
        this.hsCode = value;
    }

    /**
     * ��ȡproductWeight���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductWeight() {
        return productWeight;
    }

    /**
     * ����productWeight���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductWeight(String value) {
        this.productWeight = value;
    }

    /**
     * ��ȡproductDeclaredValue���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductDeclaredValue() {
        return productDeclaredValue;
    }

    /**
     * ����productDeclaredValue���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductDeclaredValue(String value) {
        this.productDeclaredValue = value;
    }

    /**
     * ��ȡisAccessories���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsAccessories() {
        return isAccessories;
    }

    /**
     * ����isAccessories���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsAccessories(String value) {
        this.isAccessories = value;
    }

    /**
     * ��ȡhasInvoice���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHasInvoice() {
        return hasInvoice;
    }

    /**
     * ����hasInvoice���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHasInvoice(String value) {
        this.hasInvoice = value;
    }

    /**
     * ��ȡcurrencyCode���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * ����currencyCode���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrencyCode(String value) {
        this.currencyCode = value;
    }

    /**
     * ��ȡmodelSerial���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModelSerial() {
        return modelSerial;
    }

    /**
     * ����modelSerial���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModelSerial(String value) {
        this.modelSerial = value;
    }

    /**
     * ��ȡmodelSerial2���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModelSerial2() {
        return modelSerial2;
    }

    /**
     * ����modelSerial2���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModelSerial2(String value) {
        this.modelSerial2 = value;
    }

    /**
     * ��ȡrejectReason���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRejectReason() {
        return rejectReason;
    }

    /**
     * ����rejectReason���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRejectReason(String value) {
        this.rejectReason = value;
    }

}
