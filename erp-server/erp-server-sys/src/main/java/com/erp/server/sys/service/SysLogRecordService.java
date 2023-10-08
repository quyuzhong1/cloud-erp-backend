package com.erp.server.sys.service;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.SysLogMqDTO;
import com.erp.model.sys.entity.SysLogRecordEntity;
import com.common.business.service.SuperService;
import com.erp.model.sys.dto.SysLogRecordDTO;

import java.util.List;

/**
 * <p>
 * 操作日志 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-08-25
 */
public interface SysLogRecordService extends SuperService<SysLogRecordEntity> {

    /**
     * 添加到mq
     */

    void mqBatchSend(List<SysLogRecordDTO.AddDTO> listDto);

    /**
     * 消费
     */
    void consumerAndAdd(SysLogMqDTO mqDTO);

    /**
     * 分页列表查询
     * @author Jim
     * @date: 2023-09-04
     * @return PagingVO<SysLogRecordDTO.ListDTO>>
     */
    PagingVO<SysLogRecordDTO.ListDTO> paging(PagingDTO<SysLogRecordDTO.PagingParamDTO> pagingParamDTO);

}
