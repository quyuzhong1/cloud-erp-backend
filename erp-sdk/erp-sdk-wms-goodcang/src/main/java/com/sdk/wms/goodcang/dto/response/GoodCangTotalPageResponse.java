package com.sdk.wms.goodcang.dto.response;

import com.alibaba.fastjson.JSONObject;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@Builder
@ToString
@AllArgsConstructor
public class GoodCangTotalPageResponse implements Serializable {

    /**
     * 总数
     */
    private Integer total;


    /**
     * 内容列表
     */
    private List<JSONObject> list;

}
