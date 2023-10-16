package com.erp.server.sys.mapper;
import com.erp.model.sys.entity.MessageEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.sys.dto.MessageDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 消息通知表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-10
 */
@Mapper
public interface MessageMapper extends BaseMapper<MessageEntity> {

    /**
     * 查询未读消息
     * @Author Luo_WG
     * @Date 2023/8/17 14:15
     * @param param
     * @return java.util.List<com.erp.model.sys.entity.MessageEntity>
     **/
    List<MessageEntity> listByNotReadMessage(@Param("param") MessageDTO.PdaParamDTO param);

    /**
     * 条件查询消息信息
     * @Author Luo_WG
     * @Date 2023/8/22 16:27
     * @param param
     * @return java.util.List<com.erp.model.sys.entity.MessageEntity>
     **/
    List<MessageEntity> list(@Param("param") MessageDTO.PdaParamDTO param);

}
