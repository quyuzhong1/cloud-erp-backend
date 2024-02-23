
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
 *         &lt;element name="UpdateASNInfo" type="{http://www.example.org/ServiceForAsn/}updateAsnInfo"/>
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
    "updateASNInfo"
})
@XmlRootElement(name = "updateAsn")
public class UpdateAsn {

    @XmlElement(name = "HeaderRequest", required = true)
    protected HeaderRequest headerRequest;
    @XmlElement(name = "UpdateASNInfo", required = true)
    protected UpdateAsnInfo updateASNInfo;

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
     * ��ȡupdateASNInfo���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link UpdateAsnInfo }
     *     
     */
    public UpdateAsnInfo getUpdateASNInfo() {
        return updateASNInfo;
    }

    /**
     * ����updateASNInfo���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link UpdateAsnInfo }
     *     
     */
    public void setUpdateASNInfo(UpdateAsnInfo value) {
        this.updateASNInfo = value;
    }

}
