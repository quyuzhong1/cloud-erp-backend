
package com.sdk.tms.baohong.org.example.webservice.agency;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>productDeatil complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="productDeatil">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="productSku" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="productTitleEn" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="declaredValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="opQuantity" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="purposeDeclaredValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="productLink" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="currencyCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "productDeatil", propOrder = {
    "productSku",
    "productTitleEn",
    "declaredValue",
    "opQuantity",
    "purposeDeclaredValue",
    "productLink",
    "currencyCode"
})
public class ProductDeatil {

    @XmlElement(required = true)
    protected String productSku;
    protected String productTitleEn;
    protected String declaredValue;
    protected int opQuantity;
    protected String purposeDeclaredValue;
    protected String productLink;
    protected String currencyCode;

    /**
     * 获取productSku属性的值。
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
     * 设置productSku属性的值。
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
     * 获取productTitleEn属性的值。
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
     * 设置productTitleEn属性的值。
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
     * 获取declaredValue属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeclaredValue() {
        return declaredValue;
    }

    /**
     * 设置declaredValue属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeclaredValue(String value) {
        this.declaredValue = value;
    }

    /**
     * 获取opQuantity属性的值。
     * 
     */
    public int getOpQuantity() {
        return opQuantity;
    }

    /**
     * 设置opQuantity属性的值。
     * 
     */
    public void setOpQuantity(int value) {
        this.opQuantity = value;
    }

    /**
     * 获取purposeDeclaredValue属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPurposeDeclaredValue() {
        return purposeDeclaredValue;
    }

    /**
     * 设置purposeDeclaredValue属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPurposeDeclaredValue(String value) {
        this.purposeDeclaredValue = value;
    }

    /**
     * 获取productLink属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductLink() {
        return productLink;
    }

    /**
     * 设置productLink属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductLink(String value) {
        this.productLink = value;
    }

    /**
     * 获取currencyCode属性的值。
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
     * 设置currencyCode属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrencyCode(String value) {
        this.currencyCode = value;
    }

}
