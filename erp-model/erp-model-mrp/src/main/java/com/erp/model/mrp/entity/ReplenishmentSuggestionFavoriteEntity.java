package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 补货建议关注表
 * </p>
 *
 * @author will
 * @since 2024-08-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("replenishment_suggestion_favorite")
public class ReplenishmentSuggestionFavoriteEntity extends BaseEntity<ReplenishmentSuggestionFavoriteEntity> {

    /**
    * 用户id
    */
    @TableField("user_id")
    private String userId;
    /**
    * 补货建议id
    */
    @TableField("replenishment_suggestion_id")
    private String replenishmentSuggestionId;


    public static final String USER_ID = "user_id";

    public static final String REPLENISHMENT_SUGGESTION_ID = "replenishment_suggestion_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}