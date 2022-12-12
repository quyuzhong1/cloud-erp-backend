package com.erp.model.dmp.gyy.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class TagsBean {
    /**
     * tag_code : refund
     * tag_name : 退款
     * tag_note :
     */

    @SerializedName("tag_code")
    private String tagCode;
    @SerializedName("tag_name")
    private String tagName;
    @SerializedName("tag_note")
    private String tagNote;
}
