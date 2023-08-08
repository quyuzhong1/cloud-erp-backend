package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.common.business.dto.FindUserDTO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.UserInfoDTO;
import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 金蝶业务员 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-07-07
 */
@Mapper
public interface KingdeeBusinessOperatorMapper extends BaseMapper<KingdeeBusinessOperatorEntity> {

    /**
     * 获取到金蝶业务员信息
     * @param dto
     * @return
     */
    List<UserInfoDTO.BusinessOperationUserDTO> listInfo(@Param("params") KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO dto, @Param("orgCode") String orgCode);
}
