package com.erp.server.mrp.es.entity;

import com.common.business.threadlocal.UserContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class BaseEsEntity implements Serializable {

    /**
     * 主键
     */
    @Id
    private String id;

    /**
     * 创建人id
     */
    @Field(type = FieldType.Keyword)
    private String createUserId = UserContext.getNonLoginUser().getUid();

    /**
     * 创建人名称
     */
    @Field(type = FieldType.Keyword)
    private String createUserName = UserContext.getNonLoginUser().getUserName();

    /**
     * 创建时间
     */
    @Field(type = FieldType.Keyword)
    @CreatedDate
    private LocalDateTime createTime;

    /**
     * 修改人id
     */
    @Field(type = FieldType.Keyword)
    private String updateUserId = UserContext.getNonLoginUser().getUid();

    /**
     * 修改人名称
     */
    @Field(type = FieldType.Keyword)
    private String updateUserName = UserContext.getNonLoginUser().getUserName();

    /**
     * 更新时间
     */
    @Field(type = FieldType.Keyword)
    @LastModifiedDate
    private LocalDateTime updateTime;

    /**
     * 乐观锁版本号
     */
    private Integer version = 1;

    /**
     * 逻辑删除字段
     */
    private Boolean isDeleted = false;

}
