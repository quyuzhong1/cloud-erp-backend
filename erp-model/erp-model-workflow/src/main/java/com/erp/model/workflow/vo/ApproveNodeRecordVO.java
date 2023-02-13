package com.erp.model.workflow.vo;

import com.erp.model.workflow.dto.AuditorHandleDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/** 审核节点 记录
 * @Classname
 * @Description TODO
 * @Date 2023-02-13 16:57
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ApproveNodeRecordVO implements Serializable {

    /**
     * 审核人操作记录
     */
    private List<AuditorHandleDTO> auditorHandleList;
}
