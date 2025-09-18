package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.BaseSearchDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/5 21:26
 */
@Data
@NoArgsConstructor
public class SysLogSelectDTO extends BaseSearchDTO {

    /**
     * 业务id(对应模块id)
     */
    private String businessId;

    /**
     * 父级id(用于综合数据查询)
     */
    private String pid;
    private String searchKey;

    /**
     * 类型:140=SKU标准成本
     */
    private String moduleType;
}
