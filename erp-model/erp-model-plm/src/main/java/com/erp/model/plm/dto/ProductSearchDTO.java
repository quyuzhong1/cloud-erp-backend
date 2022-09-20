package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    //搜索关键字
    private String searchKeyword;

    //搜索类型
    @StateEnumValue(strValues = {"name", "grade", "projectStatus", "approvalStatus", " productOwner", "projectOwner", "createTime"}, message = "搜索类型有误")
    private String searchType;

    //产品等级
    private List<String> gradeList;

    //产品负责人id
    private List<String> productOwnerList;

    //项目负责人id
    private List<String> projectOwnerList;

    //立项状态
    private List<Integer> approvalStatusList;

    //项目状态
    private List<Integer> projectStatus;


    private Date startTime;

    private Date endTime;
}
