package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
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
     * all 全部  product 产品列表  task 任务列表
     */
    @StateEnumValue(strValues = {"all","product", "task"}, message = "导出数据 选择有误")
    private String exportData;


}
