package com.erp.server.sys.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.CfgNoticeDTO;
import com.erp.model.sys.entity.CfgNoticeEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 通知配置表 服务类
 * </p>
 *
 * @author will
 * @since 2025-02-13
 */
public interface CfgNoticeService extends SuperService<CfgNoticeEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-02-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgNoticeDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-02-13
    * @param dto
    * @return
    */
    Boolean update(CfgNoticeDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @Auther will
     * @Date 2025/2/13 14:51
     * @param dto
     * @return  PagingVO<CfgNoticeDTO.ListDTO>
     */
    PagingVO<CfgNoticeDTO.ListDTO> paging(PagingDTO<CfgNoticeDTO.SearchParamDTO> dto);

    /**
     * 查看详情
     * @Auther will
     * @Date 2025/2/13 15:00
     * @param id
     * @return CfgNoticeDTO.ViewDTO
     */
    CfgNoticeDTO.ViewDTO view(String id);
    /**
     * 更新启禁用状态
     * @Auther will
     * @Date 2025/2/13 15:13
     * @param dto
     */
    void updateDisabled(CfgNoticeDTO.UpdateDisabledDTO dto);
    /**
     * 查看发送通知时间
     * @author will
     * @date 2025/2/17 10:41
     * @param paramList
     * @return java.util.List<com.erp.model.sys.dto.CfgNoticeDTO.ViewSendTimeDTO>
     */
    List<LocalDateTime> viewSendTime(ValidList<CfgNoticeDTO.NoticeTimeDTO> paramList);
}
