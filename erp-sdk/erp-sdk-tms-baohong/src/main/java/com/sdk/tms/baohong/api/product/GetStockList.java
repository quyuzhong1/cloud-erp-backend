
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
 *         &lt;element name="warehouse_code" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="page" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="pageSize" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
    "warehouseCode",
    "page",
    "pageSize"
})
@XmlRootElement(name = "getStockList")
public class GetStockList {

    @XmlElement(name = "HeaderRequest", required = true)
    protected HeaderRequest headerRequest;
    @XmlElement(name = "warehouse_code", required = true)
    protected String warehouseCode;
    protected int page;
    protected int pageSize;

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
     * ��ȡwarehouseCode���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWarehouseCode() {
        return warehouseCode;
    }

    /**
     * ����warehouseCode���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWarehouseCode(String value) {
        this.warehouseCode = value;
    }

    /**
     * ��ȡpage���Ե�ֵ��
     * 
     */
    public int getPage() {
        return page;
    }

    /**
     * ����page���Ե�ֵ��
     * 
     */
    public void setPage(int value) {
        this.page = value;
    }

    /**
     * ��ȡpageSize���Ե�ֵ��
     * 
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * ����pageSize���Ե�ֵ��
     * 
     */
    public void setPageSize(int value) {
        this.pageSize = value;
    }

}
