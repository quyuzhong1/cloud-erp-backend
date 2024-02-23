
package com.sdk.tms.baohong.api.product;

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
 *         &lt;element name="HeaderRequest" type="{http://www.example.org/ServiceForProduct/}HeaderRequest"/>
 *         &lt;element name="ProductInfo" type="{http://www.example.org/ServiceForProduct/}ProductInfo"/>
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
    "productInfo"
})
@XmlRootElement(name = "createProduct")
public class CreateProduct {

    @XmlElement(name = "HeaderRequest", required = true)
    protected HeaderRequest headerRequest;
    @XmlElement(name = "ProductInfo", required = true)
    protected ProductInfo productInfo;

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
     * ��ȡproductInfo���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link ProductInfo }
     *     
     */
    public ProductInfo getProductInfo() {
        return productInfo;
    }

    /**
     * ����productInfo���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link ProductInfo }
     *     
     */
    public void setProductInfo(ProductInfo value) {
        this.productInfo = value;
    }

}
