package com.erp.model.dmp.gyy;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class GyyShopInfoEntity {

    /**
     * id : 311365621501
     * nick : beiyonglijun
     * code : 02
     * name : 淘宝-Beiyonglijun李军小店
     * create_date : 2021-02-23 17:46:40
     * modify_date : 2022-07-29 18:32:28
     * note :
     * type_name : 淘宝
     */

    @SerializedName("id")
    private String id;
    @SerializedName("nick")
    private String nick;
    @SerializedName("code")
    private String code;
    @SerializedName("name")
    private String name;
    @SerializedName("create_date")
    private String createDate;
    @SerializedName("modify_date")
    private String modifyDate;
    @SerializedName("note")
    private String note;
    @SerializedName("type_name")
    private String typeName;
    /**
     * 清洗数据
     */
    private Boolean isClean;
}
