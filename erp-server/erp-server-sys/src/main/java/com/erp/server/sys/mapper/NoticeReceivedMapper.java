package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.sys.dto.NoticeReceiverDTO;
import com.erp.model.sys.entity.NoticeReceiverEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 通知接收人信息 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-04-20
 */
@Mapper
public interface NoticeReceivedMapper extends BaseMapper<NoticeReceiverEntity> {


    /**
     * 根据节点id获取已开启接收人信息
     * @author yl
     * @date 2023-04-28 11:18
     * @param nodeKey
     * @return java.util.List<com.erp.model.sys.dto.NoticeReceiverDTO.InfoDTO>
     */
    List<NoticeReceiverDTO.InfoDTO> listNoticeReceiver(@Param("nodeKey") String nodeKey);
}
