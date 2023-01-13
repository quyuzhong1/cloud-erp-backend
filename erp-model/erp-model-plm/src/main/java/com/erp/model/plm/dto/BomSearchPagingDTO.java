package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname BomSearchPagingDTO
 * @Description TODO
 * @Date 2023-01-10 17:14
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BomSearchPagingDTO  implements Serializable {
    /**
     * 任务类型
     * 0 全部
     * 1 待我审核
     */
    @StateEnumValue(intValues = {0,1}, message = "bom搜索类型有误")
    private Integer bomSearchType;


    /**
     * 搜索关键字
     */
    private String searchKeyword;

}
