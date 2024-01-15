
package com.sdk.tms.baohong.api;

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
 *         &lt;element name="HeaderRequest" type="{http://www.example.org/ServiceForOrder/}HeaderRequest"/>
 *         &lt;element name="orderInfo" type="{http://www.example.org/ServiceForOrder/}UpdateOrderInfo"/>
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
    "headerRequest",
    "orderInfo"
})
@XmlRootElement(name = "updateOrder")
public class UpdateOrder {

    @XmlElement(name = "HeaderRequest", required = true)
    protected HeaderRequest headerRequest;
    @XmlElement(required = true)
    protected UpdateOrderInfo orderInfo;

    /**
     * 获取headerRequest属性的值。
     * 
     * @return
     *     possible object is
     *     {@link HeaderRequest }
     *     
     */
    public HeaderRequest getHeaderRequest() {
        return headerRequest;
    }

    /**
     * 设置headerRequest属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link HeaderRequest }
     *     
     */
    public void setHeaderRequest(HeaderRequest value) {
        this.headerRequest = value;
    }

    /**
     * 获取orderInfo属性的值。
     * 
     * @return
     *     possible object is
     *     {@link UpdateOrderInfo }
     *     
     */
    public UpdateOrderInfo getOrderInfo() {
        return orderInfo;
    }

    /**
     * 设置orderInfo属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link UpdateOrderInfo }
     *     
     */
    public void setOrderInfo(UpdateOrderInfo value) {
        this.orderInfo = value;
    }

}
