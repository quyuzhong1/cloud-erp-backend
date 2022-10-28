package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Classname TemplateCopySourceDTO
 * @Description TODO
 * @Date 2022-10-28 11:43
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TemplateCopySourceDTO  {

    /**
     * 模板数据库里面的id
     */
    private String templateDataId;

    /**
     * 新产生的id
     */
    private String newCreateId;
}
