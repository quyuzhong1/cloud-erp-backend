
package com.sdk.tms.baohong.api.product;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>RecordItemRequest complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="RecordItemRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="operationType" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="sku" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="englishName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="commodityId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="supplierCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="unit" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="model" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="barcodeType" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="barcode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="currencyCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="declaredValue" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="weight" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="length" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="width" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="height" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="hasInvoice" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="hasBattery" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="batteryType" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="batteryNote" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isAccessory" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="hsName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="hsCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="firstQauntity" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="secondQauntity" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="hsElement" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pictureUrl" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecordItemRequest", propOrder = {
    "operationType",
    "sku",
    "name",
    "englishName",
    "commodityId",
    "supplierCode",
    "unit",
    "model",
    "barcodeType",
    "barcode",
    "currencyCode",
    "declaredValue",
    "weight",
    "length",
    "width",
    "height",
    "hasInvoice",
    "hasBattery",
    "batteryType",
    "batteryNote",
    "isAccessory",
    "hsName",
    "hsCode",
    "firstQauntity",
    "secondQauntity",
    "hsElement",
    "pictureUrl"
})
public class RecordItemRequest {

    protected int operationType;
    @XmlElement(required = true)
    protected String sku;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String englishName;
    protected String commodityId;
    protected String supplierCode;
    @XmlElement(required = true)
    protected String unit;
    protected String model;
    protected int barcodeType;
    protected String barcode;
    @XmlElement(required = true)
    protected String currencyCode;
    protected float declaredValue;
    protected float weight;
    protected Float length;
    protected Float width;
    protected Float height;
    protected int hasInvoice;
    protected int hasBattery;
    protected Integer batteryType;
    protected String batteryNote;
    protected int isAccessory;
    @XmlElement(required = true)
    protected String hsName;
    @XmlElement(required = true)
    protected String hsCode;
    protected float firstQauntity;
    protected Float secondQauntity;
    @XmlElement(required = true)
    protected String hsElement;
    protected List<String> pictureUrl;

    /**
     * ��ȡoperationType���Ե�ֵ��
     * 
     */
    public int getOperationType() {
        return operationType;
    }

    /**
     * ����operationType���Ե�ֵ��
     * 
     */
    public void setOperationType(int value) {
        this.operationType = value;
    }

    /**
     * ��ȡsku���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSku() {
        return sku;
    }

    /**
     * ����sku���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSku(String value) {
        this.sku = value;
    }

    /**
     * ��ȡname���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * ����name���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * ��ȡenglishName���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnglishName() {
        return englishName;
    }

    /**
     * ����englishName���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnglishName(String value) {
        this.englishName = value;
    }

    /**
     * ��ȡcommodityId���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCommodityId() {
        return commodityId;
    }

    /**
     * ����commodityId���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCommodityId(String value) {
        this.commodityId = value;
    }

    /**
     * ��ȡsupplierCode���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupplierCode() {
        return supplierCode;
    }

    /**
     * ����supplierCode���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupplierCode(String value) {
        this.supplierCode = value;
    }

    /**
     * ��ȡunit���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnit() {
        return unit;
    }

    /**
     * ����unit���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnit(String value) {
        this.unit = value;
    }

    /**
     * ��ȡmodel���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModel() {
        return model;
    }

    /**
     * ����model���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModel(String value) {
        this.model = value;
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
     * ��ȡbarcode���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * ����barcode���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBarcode(String value) {
        this.barcode = value;
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
     * ��ȡdeclaredValue���Ե�ֵ��
     * 
     */
    public float getDeclaredValue() {
        return declaredValue;
    }

    /**
     * ����declaredValue���Ե�ֵ��
     * 
     */
    public void setDeclaredValue(float value) {
        this.declaredValue = value;
    }

    /**
     * ��ȡweight���Ե�ֵ��
     * 
     */
    public float getWeight() {
        return weight;
    }

    /**
     * ����weight���Ե�ֵ��
     * 
     */
    public void setWeight(float value) {
        this.weight = value;
    }

    /**
     * ��ȡlength���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getLength() {
        return length;
    }

    /**
     * ����length���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setLength(Float value) {
        this.length = value;
    }

    /**
     * ��ȡwidth���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getWidth() {
        return width;
    }

    /**
     * ����width���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setWidth(Float value) {
        this.width = value;
    }

    /**
     * ��ȡheight���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getHeight() {
        return height;
    }

    /**
     * ����height���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setHeight(Float value) {
        this.height = value;
    }

    /**
     * ��ȡhasInvoice���Ե�ֵ��
     * 
     */
    public int getHasInvoice() {
        return hasInvoice;
    }

    /**
     * ����hasInvoice���Ե�ֵ��
     * 
     */
    public void setHasInvoice(int value) {
        this.hasInvoice = value;
    }

    /**
     * ��ȡhasBattery���Ե�ֵ��
     * 
     */
    public int getHasBattery() {
        return hasBattery;
    }

    /**
     * ����hasBattery���Ե�ֵ��
     * 
     */
    public void setHasBattery(int value) {
        this.hasBattery = value;
    }

    /**
     * ��ȡbatteryType���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getBatteryType() {
        return batteryType;
    }

    /**
     * ����batteryType���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setBatteryType(Integer value) {
        this.batteryType = value;
    }

    /**
     * ��ȡbatteryNote���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBatteryNote() {
        return batteryNote;
    }

    /**
     * ����batteryNote���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBatteryNote(String value) {
        this.batteryNote = value;
    }

    /**
     * ��ȡisAccessory���Ե�ֵ��
     * 
     */
    public int getIsAccessory() {
        return isAccessory;
    }

    /**
     * ����isAccessory���Ե�ֵ��
     * 
     */
    public void setIsAccessory(int value) {
        this.isAccessory = value;
    }

    /**
     * ��ȡhsName���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHsName() {
        return hsName;
    }

    /**
     * ����hsName���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHsName(String value) {
        this.hsName = value;
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
     * ��ȡfirstQauntity���Ե�ֵ��
     * 
     */
    public float getFirstQauntity() {
        return firstQauntity;
    }

    /**
     * ����firstQauntity���Ե�ֵ��
     * 
     */
    public void setFirstQauntity(float value) {
        this.firstQauntity = value;
    }

    /**
     * ��ȡsecondQauntity���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getSecondQauntity() {
        return secondQauntity;
    }

    /**
     * ����secondQauntity���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setSecondQauntity(Float value) {
        this.secondQauntity = value;
    }

    /**
     * ��ȡhsElement���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHsElement() {
        return hsElement;
    }

    /**
     * ����hsElement���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHsElement(String value) {
        this.hsElement = value;
    }

    /**
     * Gets the value of the pictureUrl property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pictureUrl property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPictureUrl().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getPictureUrl() {
        if (pictureUrl == null) {
            pictureUrl = new ArrayList<String>();
        }
        return this.pictureUrl;
    }

}
