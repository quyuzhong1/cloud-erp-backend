package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 *  撤销的是
 * @Classname
 * @Description TODO
 * @Date 2023-02-20 19:14
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class WithDrawProcessBusinessDTO  implements Serializable {

    /**
     * 用户id
     */
    private String userId;


    /**
     * 具体业务表id
     */
    private List<String> businessTableIdList;
}
