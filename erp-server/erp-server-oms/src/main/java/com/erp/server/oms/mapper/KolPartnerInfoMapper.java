package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.KolPartnerInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.oms.dto.KolPartnerInfoDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 企业达人库 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-12-02
 */
@Mapper
public interface KolPartnerInfoMapper extends BaseMapper<KolPartnerInfoEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<KolPartnerInfoDTO.ListDTO> paging(Page query, @Param("params") KolPartnerInfoDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") KolPartnerInfoDTO.PagingParamDTO params);

    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<KolPartnerInfoDTO.TabListDTO> tabList(@Param("params") KolPartnerInfoDTO.PagingParamDTO searchParam);

    List<KolPartnerInfoDTO.PartnerAddressDTO> partnerAddressList(@Param("params") KolPartnerInfoDTO.AddressSelectDTO params);

    List<KolPartnerInfoDTO.DropDownDTO> dropDown(@Param("searchKeyword") String searchKeyword);
}
