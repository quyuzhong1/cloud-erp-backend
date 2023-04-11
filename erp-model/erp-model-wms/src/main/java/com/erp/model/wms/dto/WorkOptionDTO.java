package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 收货明细DTO
 * @Author Luo_WG
 * @Date 2023/4/6 17:18
 **/
@Data
@NoArgsConstructor
public class WorkOptionDTO extends BaseEntity<WorkOptionDTO> {

    /**
     * 代办列表
     */
    @Data
    @NoArgsConstructor
    public static class pendingViewDTO {
        /**
         * 模块名称
         */
        private String name;


    }

}
