package com.erp.server.sys.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.dto.UserInfoDTO;
import com.erp.model.sys.entity.KingdeeOperatorRefPostEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 金蝶业务员表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Mapper
public interface KingdeeOperatorRefPostMapper extends BaseMapper<KingdeeOperatorRefPostEntity> {

    /**
     * 分页查询
     * @description
     * @param
     * @return
     * @date 2024-03-15 11:59
     * @author Lambda
     */
    IPage<KingdeeOperatorRefPostDTO.PagingViewDTO> paging(Page query, @Param("params")KingdeeOperatorRefPostDTO.PagingParamDTO paramDTO);

    /**
     * 获取金蝶业务员信息
     * @param dto
     * @return
     */
    KingdeeOperatorRefPostDTO.OperatorDTO find(@Param("params") KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO dto);

    /**
     * 下拉
     * @description
     * @param
     * @return
     * @date 2024-03-18 14:18
     * @author Lambda
     */
    List<UserInfoDTO.BusinessOperationUserDTO> listInfo(@Param("params") KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO dto);

    List<KingdeeOperatorRefPostDTO.OperatorDTO> listOperatorByUserIdList(@Param("userIdList") List<String> userIdList);
    /**
     * 根据业务员类型和组织id集合查询用户信息
     * @author will
     * @date 2025/8/6 14:29
     * @param dto
     * @return List<BusinessOperationUserDTO>
     */
    List<UserInfoDTO.BusinessOperationUserDTO> listUser(@Param("params")KingdeeBusinessOperatorDTO.ListBusinessOperatorUserDTO dto);
}
