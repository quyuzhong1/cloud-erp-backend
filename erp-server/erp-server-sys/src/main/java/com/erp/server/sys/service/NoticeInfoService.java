package com.erp.server.sys.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.NoticeDTO;
import com.erp.model.sys.entity.NoticeInfoEntity;

import java.util.List;

/**
 * <p>
 * 通知表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-20
 */
public interface NoticeInfoService extends SuperService<NoticeInfoEntity> {

    
    /**
     * 添加通知
     * @author yl
     * @date 2023-04-20 20:43
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean add(NoticeDTO.AddDTO dto);

    
    /**
     * 编辑通知
     * @author yl
     * @date 2023-04-26 15:47
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean edit(NoticeDTO.UpdateDTO dto);


    /**
     * 通知详情
     * @author yl
     * @date 2023-04-26 15:47
     * @param id
     * @return java.lang.Boolean
     */
    NoticeDTO.ViewDTO view(String id);


    /**
     * 根据节点key 获取对应数据
     * @author yl
     * @date 2023-04-26 18:41
     * @param nodeKeys
     * @return java.util.List<com.erp.model.sys.entity.NoticeInfoEntity>
     */
    List<NoticeInfoEntity> listByNodeKeys(List<String> nodeKeys);


    /**
     * 启用 禁用 通知节点
     *
     * @param dto
     * @return
     */
    Boolean updateStatus(UpdateStateDTO dto);

    PagingVO<NoticeDTO.PagingViewDTO> paging(PagingDTO<NoticeDTO.PagingParamDTO> dto);
}
