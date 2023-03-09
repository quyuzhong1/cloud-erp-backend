package com.erp.model.plm.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 模板管理查询DTO
 * @date 2022/11/16 16:06
 */
@NoArgsConstructor
@Data
public class TemplateSearchDTO extends SortDTO implements Serializable {

    /**
     * 搜索关键字
     */
    private String searchKeyword;

   /**
    * 模板id
    */
    private String templateId;

    private String flagId;



}
