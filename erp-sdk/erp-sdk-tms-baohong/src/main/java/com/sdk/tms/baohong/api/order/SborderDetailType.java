
package com.sdk.tms.baohong.api.order;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>sborderDetailType complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="sborderDetailType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="skuNo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="skuName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="skuCnName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="puName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="hsCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="skuDeclaredValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="quantity" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="specification" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sborderDetailType", propOrder = {
    "skuNo",
    "skuName",
    "skuCnName",
    "puName",
    "hsCode",
    "skuDeclaredValue",
    "quantity",
    "specification"
})
public class SborderDetailType {

    @XmlElement(required = true)
    protected String skuNo;
    @XmlElement(required = true)
    protected String skuName;
    @XmlElement(required = true)
    protected String skuCnName;
    @XmlElement(required = true)
    protected String puName;
    @XmlElement(required = true)
    protected String hsCode;
    @XmlElement(required = true)
    protected String skuDeclaredValue;
    @XmlElement(required = true)
    protected String quantity;
    @XmlElement(required = true)
    protected String specification;

    /**
     * 获取skuNo属性的值。
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
     * 设置skuNo属性的值。
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
     * 获取skuName属性的值。
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
     * 设置skuName属性的值。
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
     * 获取skuCnName属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSkuCnName() {
        return skuCnName;
    }

    /**
     * 设置skuCnName属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSkuCnName(String value) {
        this.skuCnName = value;
    }

    /**
     * 获取puName属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPuName() {
        return puName;
    }

    /**
     * 设置puName属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPuName(String value) {
        this.puName = value;
    }

    /**
     * 获取hsCode属性的值。
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
     * 设置hsCode属性的值。
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
     * 获取skuDeclaredValue属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSkuDeclaredValue() {
        return skuDeclaredValue;
    }

    /**
     * 设置skuDeclaredValue属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSkuDeclaredValue(String value) {
        this.skuDeclaredValue = value;
    }

    /**
     * 获取quantity属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQuantity() {
        return quantity;
    }

    /**
     * 设置quantity属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuantity(String value) {
        this.quantity = value;
    }

    /**
     * 获取specification属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecification() {
        return specification;
    }

    /**
     * 设置specification属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecification(String value) {
        this.specification = value;
    }

}
