package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname 交付物
 *  TODO
 * @Date 2022-09-23 10:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DeliveryDocsDTO implements Serializable {


    /**
     *id
     */
    private String id;



    /**
     * 完成的文档id
     */
    private String finishDocsId;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 任务名
     */
    private String taskName;



    /**
     * 文件类型
     */
    private String fileType;


    /**
     * 交付文档名
     */
    private String deliveryDocsName;


    /**
     * 交付文档名
     */
    private String fileName;


    /**
     * 文件大小
     */
    private Integer fileSize;


    /**
     * 文提交时间
     */
    private Date submitTime;


    /**
     * 提交人
     */
    private Date submitUserName;


}
