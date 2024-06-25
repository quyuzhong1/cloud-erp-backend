package com.common.business.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * mongo task 处理超类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MongoSuperDTO{

    @Panno(findType = PannoEnum.GT,field = "_id")
    private String id;

    /**
     * 报告记录内容 + 报告行号 + 报告ID 生成MD5
     */
    @Panno(findType = PannoEnum.EQ,field = "uniqueId")
    private String uniqueId;

    /**
     * 保存到mongo的时间
     */
    @Panno(findType = PannoEnum.EQ,field = "downloadTime")
    private LocalDateTime downloadTime;

    /**
     * 是新数据（新增或更新）
     */
    @Panno(findType = PannoEnum.EQ,field = "isInsertOrUpdate")
    private Boolean isAddOrUpdate;

    /**
     * 业务唯一KEY(用于区分新数据还是历史数据)
     */
    @Panno(findType = PannoEnum.EQ,field = "businessUniqueKey")
    public String businessUniqueKey;


    public static MongoSuperDTO getUniqId(String uniqueId) {
        MongoSuperDTO uniqueDto = new MongoSuperDTO();
        uniqueDto.setUniqueId(uniqueId);
        return uniqueDto;
    }

    public static MongoSuperDTO initLastId(String lastId) {
        MongoSuperDTO mongoDTO = new MongoSuperDTO();
        mongoDTO.setId(lastId);
        return mongoDTO;
    }

}
