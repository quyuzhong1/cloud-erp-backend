package com.common.business.dto.base;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.enums.OperationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 批量处理结果
 *
 * @Author Cloud
 * @Date 2023/8/11 11:05
 **/

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveResultDTO extends BatchResultDTO {
    private Boolean isExistProcess;
}
