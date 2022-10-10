package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Classname ProductSearchDTO
 * @Description TODO
 * @Date 2022-09-17 14:41
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductSearchDTO implements Serializable {


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
    @NotBlank(message = "分类id不能为空")
    private String categoryId;

    //状态列表
    private List<Integer> stateList;

    //项目列表
    private List<String> productIds;


}
