package com.erp.model.plm.dto;

import com.common.business.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Lambda
 * @Classname ProductSearchDTO
 * @Description TODO
 * @Date 2022-09-17 14:41
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductSearchDTO extends PermissionsDTO {


    /**
     * 我的任务类型
     * all 全部  finished 完成的  unfinished 未完成的
     *
     */
    private String myTaskType;


    /**
     * 搜索关键字
     */
    private String searchKeyword;

    /**
     * 分类id
     */
    private String categoryId;

    /**
     * 在点击分类可以获取到产品id
     */
    private List<String> productIds;




    /**
     * 阶段集合
     */
    private List<String> phaseNameList;

    /**
     * 状态列表
     *  立项状态 0 待规划 1 调研中  2：ID设计中  3::已立项  4：已终止
     */
    private List<Integer> stateList;



    /**
     * 产品等级
     */
    private List<String> gradeIdList;

    /**
     * 产品 品牌id 集合
     */
    private List<String> brandIdList;

    /**
     * 产品 属性
     */
    private List<String> propertyIdList;


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
     * 创建时间
     */
    private List<LocalDate> createTimeList;

}
