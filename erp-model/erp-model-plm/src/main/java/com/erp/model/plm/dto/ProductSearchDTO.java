package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import com.erp.common.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Classname ProductSearchDTO
 * @Description TODO
 * @Date 2022-09-17 14:41
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductSearchDTO extends PermissionsDTO  {


    /**
     * 我的收藏 true 是
     */
    private Boolean isMyCollect;

    /**
     * 搜索关键字
     */
    private String searchKeyword;

    /**
     * 分类id
     */
    private String categoryId;

    /**
     * 状态列表
     *  立项状态 0 待规划 1 调研中  2：ID设计中  3::已立项  4：已终止
     */
    private List<Integer> stateList;

    //产品id 集合
    private List<String> productIds;

}
