
package com.sdk.tms.baohong.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>smRow complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="smRow">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sm_name_cn" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sm_code" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="warehouse_code" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sm_class_code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sm_channel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "smRow", propOrder = {
    "smNameCn",
    "smCode",
    "warehouseCode",
    "smClassCode",
    "smChannel"
})
public class SmRow {

    @XmlElement(name = "sm_name_cn", required = true)
    protected String smNameCn;
    @XmlElement(name = "sm_code", required = true)
    protected String smCode;
    @XmlElement(name = "warehouse_code", required = true)
    protected String warehouseCode;
    @XmlElement(name = "sm_class_code")
    protected String smClassCode;
    @XmlElement(name = "sm_channel")
    protected String smChannel;

    /**
     * 获取smNameCn属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSmNameCn() {
        return smNameCn;
    }

    /**
     * 设置smNameCn属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSmNameCn(String value) {
        this.smNameCn = value;
    }

    /**
     * 获取smCode属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSmCode() {
        return smCode;
    }

    /**
     * 设置smCode属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSmCode(String value) {
        this.smCode = value;
    }

    /**
     * 获取warehouseCode属性的值。
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
     * 设置warehouseCode属性的值。
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
     * 获取smClassCode属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSmClassCode() {
        return smClassCode;
    }

    /**
     * 设置smClassCode属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSmClassCode(String value) {
        this.smClassCode = value;
    }

    /**
     * 获取smChannel属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSmChannel() {
        return smChannel;
    }

    /**
     * 设置smChannel属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSmChannel(String value) {
        this.smChannel = value;
    }

}
