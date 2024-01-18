
package com.sdk.tms.baohong.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>productDetail complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="productDetail">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="product_sku" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="op_quantity" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "productDetail", propOrder = {
    "productSku",
    "opQuantity"
})
public class ProductDetail {

    @XmlElement(name = "product_sku", required = true)
    protected String productSku;
    @XmlElement(name = "op_quantity")
    protected int opQuantity;

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

}
