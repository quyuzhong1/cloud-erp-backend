package com.erp.model.dmp.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 速帝云保存接口返参DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class SdySaveResultDTO {

    /**
     * errno : 2001
     * errmsg : 2001:每页条数为必填字段
     * data :
     * trace_id : c0a8030466cd85def7a8d26c3e9ddcb0
     * stack :
     */

    @SerializedName("errno")
    private int errno;
    @SerializedName("errmsg")
    private String errmsg;
    @SerializedName("data")
    private String data;
    @SerializedName("trace_id")
    private String traceId;
    @SerializedName("stack")
    private String stack;
}
