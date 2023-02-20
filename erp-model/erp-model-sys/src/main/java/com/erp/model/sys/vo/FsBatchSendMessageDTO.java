package com.erp.model.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Classname FsBatchSendMessageDTO
 * @Description TODO
 * @Date 2022-11-15 11:21
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class FsBatchSendMessageDTO implements Serializable {


    /**
     * 飞书的union_id
     */
    private List<String> unionIds;


    private Map<String, Object> contentMap;
}
