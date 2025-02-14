package com.erp.server.sys.service;

import com.common.business.service.SuperService;
import com.erp.model.sys.dto.CfgNoticeDTO;
import com.erp.model.sys.entity.CfgNoticeDetailEntity;

import java.util.List;

/**
 * <p>
 * 通知配置明细表 服务类
 * </p>
 *
 * @author will
 * @since 2025-02-13
 */
public interface CfgNoticeDetailService extends SuperService<CfgNoticeDetailEntity> {

    /**
     * 新增通知对象
     * @Auther will
     * @Date 2025/2/13 15:38
     * @param noticeObjectDTOList
     * @param id
     */
    void addOrUpdateNoticeObjectList(List<CfgNoticeDTO.NoticeObjectDTO> noticeObjectDTOList, String id);
    /**
     * 新增通知时间
     * @Auther will
     * @Date 2025/2/13 15:39
     * @param noticeTimeDTOList
     * @param id
     */
    void addNOrUpdateoticeTimeList(List<CfgNoticeDTO.NoticeTimeDTO> noticeTimeDTOList, String id);
    /**
     * 根据主表id集合查询
     * @Auther will
     * @Date 2025/2/13 18:18
     * @param idList
     * @return List<CfgNoticeDetailEntity>
     */
    List<CfgNoticeDetailEntity> listByMainIdList(List<String> idList);
}
