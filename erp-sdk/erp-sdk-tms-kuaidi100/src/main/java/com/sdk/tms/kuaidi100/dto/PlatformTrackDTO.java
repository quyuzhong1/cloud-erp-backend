package com.sdk.tms.kuaidi100.dto;

import com.common.business.dto.UniqueDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 功能描述：快递100 轨迹消息中转 DTO
 *
 * @author jack
 * @date 2026-03-31
 */
@Slf4j
@Setter
@Getter
@NoArgsConstructor
public class PlatformTrackDTO extends UniqueDto {
    /**
     * 运单号
     */
    private String trackNo;

    /**
     * 轨迹明细列表
     */
    private List<PlatformTrackDetail> details;

    @Override
    public String toString() {
        return "PlatformTrackDTO{" +
                "trackNo='" + trackNo + '\'' +
                ", details=" + details +
                '}';
    }
}
