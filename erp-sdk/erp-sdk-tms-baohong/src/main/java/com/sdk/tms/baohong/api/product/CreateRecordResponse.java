
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
 *         &lt;element name="ask" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="RecordItem" type="{http://www.example.org/ServiceForProduct/}RecordItemResponse" maxOccurs="unbounded" minOccurs="0"/>
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
    "recordItem"
})
@XmlRootElement(name = "createRecordResponse")
public class CreateRecordResponse {

    protected int ask;
    @XmlElement(required = true)
    protected String message;
    @XmlElement(name = "RecordItem")
    protected List<RecordItemResponse> recordItem;

    /**
     * ��ȡask���Ե�ֵ��
     * 
     */
    public int getAsk() {
        return ask;
    }

    /**
     * ����ask���Ե�ֵ��
     * 
     */
    public void setAsk(int value) {
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
     * Gets the value of the recordItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the recordItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRecordItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RecordItemResponse }
     * 
     * 
     */
    public List<RecordItemResponse> getRecordItem() {
        if (recordItem == null) {
            recordItem = new ArrayList<RecordItemResponse>();
        }
        return this.recordItem;
    }

}
