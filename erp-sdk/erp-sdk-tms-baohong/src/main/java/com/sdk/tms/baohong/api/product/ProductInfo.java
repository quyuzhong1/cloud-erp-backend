
package com.sdk.tms.baohong.api.product;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>ProductInfo complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="ProductInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="skuNo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="skuName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="productTtitleEn" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="barcodeType" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="barcodeDefine" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="productDeclaredValue" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="weight" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="length" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="width" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="height" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="hasInvoice" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="is_accessories" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="hsGoodsName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="hsCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="UOM" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="recordInfo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isRecord" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="with_battery" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="battery_type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="battery_detail" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProductInfo", propOrder = {
    "skuNo",
    "skuName",
    "productTtitleEn",
    "barcodeType",
    "barcodeDefine",
    "productDeclaredValue",
    "weight",
    "length",
    "width",
    "height",
    "hasInvoice",
    "isAccessories",
    "hsGoodsName",
    "hsCode",
    "uom",
    "recordInfo",
    "isRecord",
    "withBattery",
    "batteryType",
    "batteryDetail"
})
public class ProductInfo {

    @XmlElement(required = true)
    protected String skuNo;
    @XmlElement(required = true)
    protected String skuName;
    @XmlElement(required = true)
    protected String productTtitleEn;
    protected int barcodeType;
    protected String barcodeDefine;
    protected float productDeclaredValue;
    protected String weight;
    protected String length;
    protected String width;
    protected String height;
    protected String hasInvoice;
    @XmlElement(name = "is_accessories")
    protected String isAccessories;
    protected String hsGoodsName;
    protected String hsCode;
    @XmlElement(name = "UOM")
    protected String uom;
    protected String recordInfo;
    protected String isRecord;
    @XmlElement(name = "with_battery")
    protected String withBattery;
    @XmlElement(name = "battery_type")
    protected String batteryType;
    @XmlElement(name = "battery_detail")
    protected String batteryDetail;

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
     * ��ȡskuName���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSkuName() {
        return skuName;
    }

    /**
     * ����skuName���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSkuName(String value) {
        this.skuName = value;
    }

    /**
     * ��ȡproductTtitleEn���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductTtitleEn() {
        return productTtitleEn;
    }

    /**
     * ����productTtitleEn���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductTtitleEn(String value) {
        this.productTtitleEn = value;
    }

    /**
     * ��ȡbarcodeType���Ե�ֵ��
     * 
     */
    public int getBarcodeType() {
        return barcodeType;
    }

    /**
     * ����barcodeType���Ե�ֵ��
     * 
     */
    public void setBarcodeType(int value) {
        this.barcodeType = value;
    }

    /**
     * ��ȡbarcodeDefine���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBarcodeDefine() {
        return barcodeDefine;
    }

    /**
     * ����barcodeDefine���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBarcodeDefine(String value) {
        this.barcodeDefine = value;
    }

    /**
     * ��ȡproductDeclaredValue���Ե�ֵ��
     * 
     */
    public float getProductDeclaredValue() {
        return productDeclaredValue;
    }

    /**
     * ����productDeclaredValue���Ե�ֵ��
     * 
     */
    public void setProductDeclaredValue(float value) {
        this.productDeclaredValue = value;
    }

    /**
     * ��ȡweight���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWeight() {
        return weight;
    }

    /**
     * ����weight���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWeight(String value) {
        this.weight = value;
    }

    /**
     * ��ȡlength���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLength() {
        return length;
    }

    /**
     * ����length���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLength(String value) {
        this.length = value;
    }

    /**
     * ��ȡwidth���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWidth() {
        return width;
    }

    /**
     * ����width���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWidth(String value) {
        this.width = value;
    }

    /**
     * ��ȡheight���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeight() {
        return height;
    }

    /**
     * ����height���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeight(String value) {
        this.height = value;
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
     * ��ȡuom���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUOM() {
        return uom;
    }

    /**
     * ����uom���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUOM(String value) {
        this.uom = value;
    }

    /**
     * ��ȡrecordInfo���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecordInfo() {
        return recordInfo;
    }

    /**
     * ����recordInfo���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecordInfo(String value) {
        this.recordInfo = value;
    }

    /**
     * ��ȡisRecord���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsRecord() {
        return isRecord;
    }

    /**
     * ����isRecord���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsRecord(String value) {
        this.isRecord = value;
    }

    /**
     * ��ȡwithBattery���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWithBattery() {
        return withBattery;
    }

    /**
     * ����withBattery���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWithBattery(String value) {
        this.withBattery = value;
    }

    /**
     * ��ȡbatteryType���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBatteryType() {
        return batteryType;
    }

    /**
     * ����batteryType���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBatteryType(String value) {
        this.batteryType = value;
    }

    /**
     * ��ȡbatteryDetail���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBatteryDetail() {
        return batteryDetail;
    }

    /**
     * ����batteryDetail���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBatteryDetail(String value) {
        this.batteryDetail = value;
    }

}
