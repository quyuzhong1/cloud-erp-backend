package com.erp.model.bi.vo;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/19 16:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
public class SelectShowVO {

    /**
     * 编码
     */
    private Integer code;
    /**
     * 名称
     */
    private String name;
    /**
     * 描述
     */
    private String desc;
}
