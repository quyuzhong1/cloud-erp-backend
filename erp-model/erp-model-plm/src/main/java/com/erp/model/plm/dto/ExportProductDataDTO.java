package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname ExportProductDataDTO
 * @Description TODO
 * @Date 2022-09-28 17:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ExportProductDataDTO implements Serializable {


    /**
     * 产品id 集合
     */
    @NotNull(message = "产品id集合不能为空")
    private List<String> productIds;

    /**
     * 导出数据 类型
     * 0，产品列表
     * 1. 任务列表
     */
    @NotNull(message = "导出类型不能为空")
  //  @Size(min=1,max = 2,message = "导出数据必须勾选")
    private List<Integer> exportDataList;


}
