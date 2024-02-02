
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
 *         &lt;element name="HeaderRequest" type="{http://www.example.org/ServiceForAsn/}HeaderRequest"/>
 *         &lt;element name="ASNInfo" type="{http://www.example.org/ServiceForAsn/}ASNInfo"/>
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
    "asnInfo"
})
@XmlRootElement(name = "createAsn")
public class CreateAsn {

    @XmlElement(name = "HeaderRequest", required = true)
    protected HeaderRequest headerRequest;
    @XmlElement(name = "ASNInfo", required = true)
    protected ASNInfo asnInfo;

    /**
     * ��ȡheaderRequest���Ե�ֵ��
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
     * ����headerRequest���Ե�ֵ��
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
     * ��ȡasnInfo���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link ASNInfo }
     *     
     */
    public ASNInfo getASNInfo() {
        return asnInfo;
    }

    /**
     * ����asnInfo���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link ASNInfo }
     *     
     */
    public void setASNInfo(ASNInfo value) {
        this.asnInfo = value;
    }

}
