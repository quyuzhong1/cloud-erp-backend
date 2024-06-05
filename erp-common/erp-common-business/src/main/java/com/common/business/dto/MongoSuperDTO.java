package com.common.business.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * mongo task 处理超类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MongoSuperDTO {

    /**
     * 报告记录内容 + 报告行号 + 报告ID 生成MD5
     */
    @Panno(findType = PannoEnum.EQ,field = "uniqueId")
    private String uniqueId;

    @Panno(findType = PannoEnum.EQ,field = "downloadTime")
    private String downloadTime;

    public static MongoSuperDTO getUniqId(String uniqueId) {
        MongoSuperDTO uniqueDto = new MongoSuperDTO();
        uniqueDto.setUniqueId(uniqueId);
        return uniqueDto;
    }
}
