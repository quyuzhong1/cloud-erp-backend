package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname ProductArchiveDTO
 * @Description TODO
 * @Date 2022-10-09 11:35
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductArchiveDTO implements Serializable {


    /**
     * 产品id
     */
    private String productId;

    /**
     * 产品名
     */
    private String name;


    /**
     * 产品等级
     */
    private String grade;

    /**
     * 产品项目状态
     */
    private Integer projectStatus;

    /**
     * 项目负责人
     */
    private String projectChargeName;

    /**
     * 产品负责人
     */
    private String productChargeName;



    /**
     * 归档时间
     */
    private Date archiveTime;



    /**
     * 总文档数
     */
    private Integer totalDocsCount;



    /**
     * 完成文档档数
     */
    private Integer finishDocsCount;



    /**
     * 迭代数量
     */
    private Integer iterateCount;

    /**
     * 产品创建时间
     */
    private Date productCreateTime;
}
