
package com.sdk.tms.baohong.api.asn;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>ProductArray complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="ProductArray">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="productSku" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="quantity" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="declaredValue" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="currencyCode" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProductArray", propOrder = {
    "productSku",
    "quantity",
    "declaredValue",
    "currencyCode"
})
public class ProductArray {

    protected List<String> productSku;
    protected List<String> quantity;
    protected List<String> declaredValue;
    protected List<String> currencyCode;

    /**
     * Gets the value of the productSku property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the productSku property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProductSku().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getProductSku() {
        if (productSku == null) {
            productSku = new ArrayList<String>();
        }
        return this.productSku;
    }

    /**
     * Gets the value of the quantity property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the quantity property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQuantity().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getQuantity() {
        if (quantity == null) {
            quantity = new ArrayList<String>();
        }
        return this.quantity;
    }

    /**
     * Gets the value of the declaredValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the declaredValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeclaredValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getDeclaredValue() {
        if (declaredValue == null) {
            declaredValue = new ArrayList<String>();
        }
        return this.declaredValue;
    }

    /**
     * Gets the value of the currencyCode property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the currencyCode property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCurrencyCode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCurrencyCode() {
        if (currencyCode == null) {
            currencyCode = new ArrayList<String>();
        }
        return this.currencyCode;
    }

}
