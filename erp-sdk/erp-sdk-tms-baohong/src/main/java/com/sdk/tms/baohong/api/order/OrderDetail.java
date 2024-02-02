
package com.sdk.tms.baohong.api.order;

import lombok.ToString;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>orderDetail complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="orderDetail">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="productSku" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="opQuantity" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="snCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "orderDetail", propOrder = {
    "productSku",
    "opQuantity",
    "snCode"
})
@ToString
public class OrderDetail {

    @XmlElement(required = true)
    protected String productSku;
    protected int opQuantity;
    @XmlElement(required = true)
    protected String snCode;

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
     * 获取snCode属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSnCode() {
        return snCode;
    }

    /**
     * 设置snCode属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSnCode(String value) {
        this.snCode = value;
    }

}
