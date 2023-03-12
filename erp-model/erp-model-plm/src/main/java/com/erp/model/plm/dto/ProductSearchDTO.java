package com.erp.model.plm.dto;

import com.common.business.dto.base.PermissionsDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
public class ProductSearchDTO extends PermissionsDTO {


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


    private String phaseName;

    /**
     * 阶段名称
     */
    private List<String> phaseNameList;

    /**
     * 状态列表
     *  立项状态 0 待规划 1 调研中  2：ID设计中  3::已立项  4：已终止
     */
    private List<Integer> stateList;

    //产品id 集合
    private List<String> productIds;


    /**
     * 产品等级
     */
    private List<String> gradeList;

    /**
     * 产品 品牌
     */
    private List<String> brandList;

    /**
     * 产品 属性
     */
    private List<String> propertyList;


    /**
     * 产品 经理
     */
    private List<String> productChargeIdList;

    /**
     * 项目 经理
     */
    private List<String> projectChargeIdList;


    /**
     * 项目进展
     * 列表
     */
    private List<String> progressStatusList;


    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime endTime;


}
