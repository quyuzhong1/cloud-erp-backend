package com.erp.model.plm.vo;

import com.erp.model.plm.dto.DocsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Classname 产品任务
 * @Description TODO
 * @Date 2023-02-02 19:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductTaskVO implements Serializable {

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 项目id
     */
    private String projectId;


    /**
     * 父级id
     */
    private String pid;

    /**
     * 产品id
     */
    private String productId;


    /**
     * 产品名
     */
    private String productName;


    /**
     * 任务名
     */
    private String name;


    /**
     * 任务类型 0 一般任务 1：审核任务
     */
    private Integer type;

    /**
     * 负责人id
     */

    private List<String> chargeIds;

    /**
     * 前置任务id
     */
    private List<String> preTaskIdList;


    /**
     * 前置任务名
     */
    private String preTaskNames;

    /**
     * 计划开始时间
     */
    private Date planStartTime;

    /**
     * 计划结束时间
     */
    private Date planEndTime;


    /**
     *实际开始时间
     */
    private Date realityStartTime;


    /**
     *实际结束时间
     */
    private Date realityEndTime;


    /**
     * 任务优先级 1 低级 2 中级 3 高级
     */
    private Integer priority;

    /**
     * 任务阶段id
     */
    private String phaseId;

    /**
     * 任务阶段名
     */
    private String phaseName;


    /**
     * 任务描述
     */
    private String description;


    /**
     * 状态
     */
    private Integer status;


    /**
     * 交付文档
     */
    @Valid
    private List<DocsDTO> deliveryDocsList;

    /**
     * 设置里程碑(0否，1是)
     */
    private Integer isMilepost;


    /**
     * 关联sku 表id集合
     */
    private List<String> refSkuIdList;

    /**
     * 关联sku 表sku 名字集合
     */
    private List<String> refSkuNoList;


    /**
     * 字段配置类型 createSku 创造sku，fillProductInfo 填写信息
     */
    private String fieldConfigType;


    /**
     * sku 完成信息
     */
    private List<Map<String, Object>> refSkuFinishList;


    /**
     * 交付文档名称（逗号分隔，用于操作日志）
     */
    private String deliveryDocsNames;




    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * 创建人
     */
    private Date createUserName;


    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 更新人
     */
    private Date updateUserName;


}
