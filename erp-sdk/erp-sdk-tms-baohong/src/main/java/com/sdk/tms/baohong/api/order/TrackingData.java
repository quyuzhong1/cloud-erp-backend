
package com.sdk.tms.baohong.api.order;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>trackingData complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="trackingData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tracking_number" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="so_weight" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="country_code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="so_declared_value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pdouctDetail" type="{http://www.example.org/ServiceForOrder/}productDetail" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="trackingInfo" type="{http://www.example.org/ServiceForOrder/}trackingInfo" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "trackingData", propOrder = {
    "trackingNumber",
    "soWeight",
    "countryCode",
    "soDeclaredValue",
    "pdouctDetail",
    "trackingInfo"
})
public class TrackingData {

    @XmlElement(name = "tracking_number")
    protected String trackingNumber;
    @XmlElement(name = "so_weight")
    protected String soWeight;
    @XmlElement(name = "country_code")
    protected String countryCode;
    @XmlElement(name = "so_declared_value")
    protected String soDeclaredValue;
    protected List<ProductDetail> pdouctDetail;
    protected List<TrackingInfo> trackingInfo;

    /**
     * 获取trackingNumber属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrackingNumber() {
        return trackingNumber;
    }

    /**
     * 设置trackingNumber属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrackingNumber(String value) {
        this.trackingNumber = value;
    }

    /**
     * 获取soWeight属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSoWeight() {
        return soWeight;
    }

    /**
     * 设置soWeight属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSoWeight(String value) {
        this.soWeight = value;
    }

    /**
     * 获取countryCode属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * 设置countryCode属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountryCode(String value) {
        this.countryCode = value;
    }

    /**
     * 获取soDeclaredValue属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSoDeclaredValue() {
        return soDeclaredValue;
    }

    /**
     * 设置soDeclaredValue属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSoDeclaredValue(String value) {
        this.soDeclaredValue = value;
    }

    /**
     * Gets the value of the pdouctDetail property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pdouctDetail property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPdouctDetail().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProductDetail }
     * 
     * 
     */
    public List<ProductDetail> getPdouctDetail() {
        if (pdouctDetail == null) {
            pdouctDetail = new ArrayList<ProductDetail>();
        }
        return this.pdouctDetail;
    }

    /**
     * Gets the value of the trackingInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trackingInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTrackingInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TrackingInfo }
     * 
     * 
     */
    public List<TrackingInfo> getTrackingInfo() {
        if (trackingInfo == null) {
            trackingInfo = new ArrayList<TrackingInfo>();
        }
        return this.trackingInfo;
    }

}
