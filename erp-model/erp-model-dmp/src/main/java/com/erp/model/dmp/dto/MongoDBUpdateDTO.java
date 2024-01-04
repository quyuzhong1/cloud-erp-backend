package com.erp.model.dmp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zdy
 * @ClassName MongoDBUpdateDTO
 * @description: TODO
 * @date 2023年12月26日
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MongoDBUpdateDTO {
    private String tableName;
    private String uniqueId;
    private Integer isClean;
}
