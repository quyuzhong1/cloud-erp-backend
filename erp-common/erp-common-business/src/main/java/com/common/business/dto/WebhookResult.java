package com.common.business.dto;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;

/**
 * @author zdy
 * @ClassName WebhookResult
 * @description: TODO
 * @date 2026年04月22日
 * @version: 1.0
 */
@Data
@XmlRootElement(name = "response")
public class WebhookResult {
    private String flag;
    private Integer code;
    private String message;
    private LocalDateTime createTime;

    public static WebhookResult isSuccess() {
        WebhookResult result = new WebhookResult();
        result.setFlag("success");
        result.setCode(200);
        result.setMessage("成功");
        result.setCreateTime(LocalDateTime.now());
        return result;
    }
    public static WebhookResult isSuccess(String flag, Integer code, String message) {
        WebhookResult result = new WebhookResult();
        result.setFlag(flag);
        result.setCode(code);
        result.setMessage(message);
        result.setCreateTime(LocalDateTime.now());
        return result;
    }

    public String toXml() {
        try {
            javax.xml.bind.JAXBContext jaxbContext = javax.xml.bind.JAXBContext.newInstance(WebhookResult.class);
            java.io.StringWriter writer = new java.io.StringWriter();
            javax.xml.bind.Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
            marshaller.marshal(this, writer);
            return writer.toString();
        } catch (javax.xml.bind.JAXBException e) {
            throw new RuntimeException("Error converting to XML", e);
        }
    }
}
