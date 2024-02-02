
package com.sdk.tms.baohong.api.asn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>anonymous complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ask" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="error" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Data" type="{http://www.example.org/ServiceForAsn/}ASNData" minOccurs="0"/>
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
    "message",
    "error",
    "data"
})
@XmlRootElement(name = "getAsnByCodeResponse")
public class GetAsnByCodeResponse {

    @XmlElement(required = true)
    protected String ask;
    @XmlElement(required = true)
    protected String message;
    protected String error;
    @XmlElement(name = "Data")
    protected ASNData data;

    /**
     * ��ȡask���Ե�ֵ��
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
     * ����ask���Ե�ֵ��
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
     * ��ȡmessage���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * ����message���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * ��ȡerror���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getError() {
        return error;
    }

    /**
     * ����error���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setError(String value) {
        this.error = value;
    }

    /**
     * ��ȡdata���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link ASNData }
     *     
     */
    public ASNData getData() {
        return data;
    }

    /**
     * ����data���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link ASNData }
     *     
     */
    public void setData(ASNData value) {
        this.data = value;
    }

}
