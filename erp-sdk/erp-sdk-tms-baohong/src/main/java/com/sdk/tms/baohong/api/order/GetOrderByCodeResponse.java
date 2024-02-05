
package com.sdk.tms.baohong.api.order;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>anonymous complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ask" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="data" type="{http://www.example.org/ServiceForOrder/}orderDataArr" minOccurs="0"/>
 *         &lt;element name="error" type="{http://www.example.org/ServiceForOrder/}errorType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "ask",
    "data",
    "error"
})
@XmlRootElement(name = "getOrderByCodeResponse")
public class GetOrderByCodeResponse {

    @XmlElement(required = true)
    protected String ask;
    protected OrderDataArr data;
    protected List<ErrorType> error;

    /**
     * 获取ask属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAsk() {
        return ask;
    }

    /**
     * 设置ask属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAsk(String value) {
        this.ask = value;
    }

    /**
     * 获取data属性的值。
     * 
     * @return
     *     possible object is
     *     {@link OrderDataArr }
     *     
     */
    public OrderDataArr getData() {
        return data;
    }

    /**
     * 设置data属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link OrderDataArr }
     *     
     */
    public void setData(OrderDataArr value) {
        this.data = value;
    }

    /**
     * Gets the value of the error property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the error property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getError().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ErrorType }
     * 
     * 
     */
    public List<ErrorType> getError() {
        if (error == null) {
            error = new ArrayList<ErrorType>();
        }
        return this.error;
    }

}
