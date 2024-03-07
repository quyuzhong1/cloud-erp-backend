
package com.sdk.tms.baohong.api.asn;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>ReceivingInfo complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="ReceivingInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="warehouseCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="refCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="isDelivery" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="iePort" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="formType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="trafName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="wrapType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="packNo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="trafMode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tradeMode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transMode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="receiveMode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="roughWeight" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="receivingStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="receivingDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ReceivingItems" type="{http://www.example.org/ServiceForAsn/}ReceivingItemsType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReceivingInfo", propOrder = {
    "warehouseCode",
    "refCode",
    "isDelivery",
    "iePort",
    "formType",
    "trafName",
    "wrapType",
    "packNo",
    "trafMode",
    "tradeMode",
    "transMode",
    "receiveMode",
    "roughWeight",
    "receivingStatus",
    "receivingDescription",
    "receivingItems"
})
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
@ToString
public class ReceivingInfo {

    /**
     * 交货仓库
     */
    @XmlElement(required = true)
    @Builder.Default
    protected String warehouseCode = "sz01";

    /**
     * 客户参考号
     */
    @XmlElement(required = true)
    protected String refCode;

    /**
     * 是否提货：0否 1是
     */
    @XmlElement(required = true)
    protected String isDelivery;

    /**
     * 进出口口岸:
     * 5349, 深圳前海湾保税港区口岸作业区;
     * 5314, 深关邮办;
     */
    @Builder.Default
    protected String iePort = "5349";
    /**
     * 业务类型：
     * 目前固定为I211
     */
    @Builder.Default
    protected String formType = "I211";

    /**
     * 车牌号
     */
    @XmlElement(required = true)
    @Builder.Default
    protected String trafName = "AAA";

    /**
     * 包装种类：
     * 1木箱,
     * 2纸箱,
     * 3桶,
     * 4散装,
     * 5托盘,
     * 6包,
     * 7其他
     */
    @XmlElement(required = true)
    @Builder.Default
    protected String wrapType = "7";

    /**
     * 总件数
     */
    @XmlElement(required = true)
    protected String packNo;
    /**
     * 出入港区运输方式：
     * 目前固定为Y
     */
    @Builder.Default
    protected String trafMode = "Y";

    /**
     * 监管方式：
     * 1210保税电商
     */
    @Builder.Default
    protected String tradeMode = "1210";

    /**
     * 成交方式：
     * 目前固定为3
     */
    @Builder.Default
    protected String transMode = "7";

    /**
     * 固定值1集货
     */
    @XmlElement(required = true)
    @Builder.Default
    protected String receiveMode = "1";

    /**
     * 毛重(KG)
     */
    @XmlElement(required = true)
    protected String roughWeight;

    /**
     * ASN的状态：
     * 0删除,
     * 1草稿,
     * 2确认,
     * 3待审核,
     */
    protected String receivingStatus;
    /**
     * 备注
     */
    protected String receivingDescription;
    /**
     * 详情
     */
    @XmlElement(name = "ReceivingItems", required = true)
    protected List<ReceivingItemsType> receivingItems;

}
