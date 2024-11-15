package com.erp.tms.aliexpress.model.handover.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.erp.tms.aliexpress.model.handover.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author zdy
 * @ClassName CommitRequest
 * @date 2024年02月01日
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfRequest implements Serializable {
    /**
     * 用户信息
     */
    @JSONField(name = "user_info")
    private UserInfo userInfo;
    /**
     *
     * ISV名称，ISV：ISV-ISV英文或拼音名称、商家ERP：SELLER-商家英文或拼音名称
     */
    @NotBlank(message = "ISV名称不能为空")
    @JSONField(name = "client")
    private String client;
    /**
     * 多语言
     */
    @JSONField(name = "locale")
    private String locale;
    /**
     * 要取消的交接物id，即大包id
     */
    @JSONField(name = "handover_content_id")
    private Long handoverContentId;
    /**
     * 打印数据类型，1：面单、4：发货标签、512：交接清单
     */
    @JSONField(name = "type")
    private Integer type;
}
