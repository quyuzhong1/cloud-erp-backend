package com.erp.server.sys.service;

import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.SysVersionDTO;
import com.erp.model.sys.entity.MessageEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.MessageDTO;

import java.util.List;

/**
 * <p>
 * 消息通知表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-10
 */
public interface MessageService extends SuperService<MessageEntity> {

    /**
     * 查询未读消息数
     * @Author Luo_WG
     * @Date 2023/8/10 15:32
     * @return java.util.List<com.erp.model.sys.dto.MessageDTO.NotReadMessageNum>
     **/
    List<MessageDTO.NotReadMessageNum> listNotReadMessageNum();

    /**
     * 查询未读消息详情
     * @Author Luo_WG
     * @Date 2023/8/10 15:59
     * @param type
     * @return java.util.List<com.erp.model.sys.dto.MessageDTO.NotReadMessageNumDetail>
     **/
    List<MessageDTO.NotReadMessageNumDetail> listNotReadMessageDetail(String type , Integer pageNo , Integer pageSize);

    /**
     * 全部已读
     * @Author Luo_WG
     * @Date 2023/8/11 16:18
     * @return java.lang.Boolean
     **/
    Boolean readAll();

    /**
     * 根据类型查询消息
     * @Author Luo_WG
     * @Date 2023/8/11 16:18
     * @return java.lang.Boolean
     **/
    List<MessageEntity> listByType(String type);

    /**
     * 是否有新的消息
     * @Author Luo_WG
     * @Date 2023/8/11 16:44
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.sys.dto.MessageDTO.IsMessageDTO>
     **/
    MessageDTO.IsMessageDTO isMessage();

    /**
     * 获取指定用户在指定应用端的最新未读系统通知
     */
    MessageDTO.NoticeDTO getLatestUnreadNotice(String userId, String application);

    /**
     * 获取指定用户在指定应用端的最新未读升级通知
     */
    MessageDTO.NoticeDTO getLatestUnreadUpgradeNotice(String userId, String application);

    /**
     * 关闭消息通知
     * @Author Luo_WG
     * @Date 2023/8/22 12:18
     * @return java.lang.Boolean
     **/
    Boolean closeMessageNotice();

    /**
     * 新增系统通知
     * @author wtr
     * @date: 2026-04-10
     * @param dto
     * @return
     */
    BaseResultDTO.AddDTO add(MessageDTO.AddDTO dto);

    /**
     * 修改
     * @author wtr
     * @date: 2026-04-10
     * @param dto
     * @return
     */
    Boolean update(MessageDTO.UpdateDTO dto);

    /**
     * 详情
     * @author wtr
     * @date: 2026-04-10
     * @param id
     * @return
     */
    MessageDTO.ViewDTO view(String id);

    /**
     * 删除系统通知
     * @author wtr
     * @date: 2026-04-10
     * @param id
     * @return
     */
    BatchResultDTO delete(String id);

    /**
     * 删除版本更新
     * @author wtr
     * @date: 2026-04-10
     * @param id
     * @return
     */
    BatchResultDTO deleteVersion(String id);

    /**
     * 新增版本更新
     * @author wtr
     * @date: 2026-04-10
     * @param dto
     * @return
     */
    BaseResultDTO.AddDTO addSysVersion(SysVersionDTO.AddDTO dto);

    /**
     * 版本更新列表查询
     * @author wtr
     * @date: 2026-04-10
     * @param
     * @return
     */
    PagingVO<SysVersionDTO.ListDTO>  pagingSysVersion(PagingDTO<SysVersionDTO.PagingParamDTO> dto);

    /**
     * PC端系统通知历史消息查询
     * @param dto
     * @return
     */
    PagingVO<MessageDTO.ListHistoryMessageDTO>  pagingHistoryMessage(PagingDTO<MessageDTO.HistoryMessagePagingParamDTO> dto);

    /**
     * PC端系统通知历史消息已读
     */
    boolean readHistoryMessage(MessageDTO.ReadHistoryMessageDTO dto);

    /**
     * PDA消息单条已读
     */
    boolean readMessage(MessageDTO.ReadHistoryMessageDTO dto);

    /**
     * PC端版本更新历史消息查询
     * @param dto
     * @return
     */
    PagingVO<SysVersionDTO.ListHistoryVersionDTO> pagingHistoryVersion(PagingDTO<SysVersionDTO.HistoryVersionPagingParamDTO> dto);

    /**
     * PC端版本更新历史消息已读
     */
    boolean readHistoryVersion(SysVersionDTO.ReadHistoryVersionDTO dto);

    /**
     * 获取系统通知未读数量
     */
    int getSysMessageUnreadCount();

    /**
     * 获取版本更新未读数量
     */
    int getSysVersionUnreadCount();

    /**
     * 批量删除消息或版本
     * @author wtr
     * @date: 2026-04-15
     * @param ids
     * @param releaseType
     * @return
     */
    List<BatchResultDTO> batchDelete(List<String> ids, String releaseType);

    /**
     *
     */
    SysVersionDTO.LatestVersionDTO getLatestVersion();
}
