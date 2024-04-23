
package com.sdk.tms.baohong.api.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RecordItemRequest {

    @Builder.Default
    protected int operationType = 1;
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


}
