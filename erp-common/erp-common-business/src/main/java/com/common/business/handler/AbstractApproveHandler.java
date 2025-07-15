package com.common.business.handler;

import com.common.business.dto.ApproveDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public abstract class AbstractApproveHandler {

    /**
     * 取消流程
     * @author will
     * @date 2025/6/18 09:41
     * @return String
     */
    public abstract Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto);
    /**
     * 反审核
     * @author will
     * @date 2025/6/18 09:40
     * @return String
     */
    public abstract Boolean disApprove(ApproveDTO.DisApproveDTO dto);
    
    /**
     * 审核通过
     * @author will
     * @date 2025/6/18 09:40
     * @param dto
     * @return void
     */
    public abstract Boolean approveEnd(ApproveDTO.EndProcessDTO dto);
}