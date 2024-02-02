
package com.sdk.tms.baohong.api.product;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="data" type="{http://www.example.org/ServiceForProduct/}inventoryRow" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="errorCodeMsg" type="{http://www.example.org/ServiceForProduct/}errorCodeMsgType" maxOccurs="unbounded" minOccurs="0"/>
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
    "data",
    "errorCodeMsg"
})
@XmlRootElement(name = "getStockResponse")
public class GetStockResponse {

    @XmlElement(required = true)
    protected String ask;
    @XmlElement(required = true)
    protected String message;
    protected List<InventoryRow> data;
    protected List<ErrorCodeMsgType> errorCodeMsg;

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
     * Gets the value of the data property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the data property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InventoryRow }
     * 
     * 
     */
    public List<InventoryRow> getData() {
        if (data == null) {
            data = new ArrayList<InventoryRow>();
        }
        return this.data;
    }

    /**
     * Gets the value of the errorCodeMsg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the errorCodeMsg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getErrorCodeMsg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ErrorCodeMsgType }
     * 
     * 
     */
    public List<ErrorCodeMsgType> getErrorCodeMsg() {
        if (errorCodeMsg == null) {
            errorCodeMsg = new ArrayList<ErrorCodeMsgType>();
        }
        return this.errorCodeMsg;
    }

}
