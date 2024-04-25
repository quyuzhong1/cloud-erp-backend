package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.CustomerB2CDTO;
import com.erp.model.oms.entity.CustomerB2cEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface CustomerB2cMapper extends BaseMapper<CustomerB2cEntity> {

    
    /**
     * 分页获取数据
     * @author yl
     * @date 2023-05-12 17:25
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage
     */
    IPage<CustomerB2CDTO.PagingViewDTO> paging(Page query, @Param("params")CustomerB2CDTO.PagingParamDTO params,@Param("approveList") List<String> approveList);

    
    /**
     * 获取到导出的数据
     * @author yl
     * @date 2023-05-15 14:56
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.CustomerB2CDTO.PagingViewDTO>
     */
    List<CustomerB2CDTO.PagingViewDTO> listExport(@Param("params") CustomerB2CDTO.ExportDTO dto,@Param("approveList") List<String> approveList);

    List<CustomerB2CDTO.ApproveCountDTO> listApproveCount(@Param("permissionSql") String permissionSql);

    /**
     * 根据名称模糊搜索
     * @param query
     * @param params
     * @return
     */
    IPage<CustomerB2CDTO.DropListDTO> customerDropDown(Page<CustomerB2CDTO.DropListDTO> query, @Param("params") CustomerB2CDTO.DropSearchDTO params);
}
