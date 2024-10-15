package com.sdk.wms.goodcang.dto.request;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Max;
import java.time.LocalDateTime;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GoodCangGetReturnInstockReq {
    @Max(value = 200,message = "每页最大长度不能大于200")
    protected Integer pageSize;

    protected Integer currentPage;

    //订单修改开始时间
    private LocalDateTime startUpdateTime;

    //订单修改结束时间
    private LocalDateTime endUpdateTime;

    //订单状态
    @Builder.Default
    private Integer asroStatus = 5;

}
