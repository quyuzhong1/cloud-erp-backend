package com.erp.server.tms.mapper;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.DeliveryDeclareDetailMidEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.tms.dto.DeliveryDeclareDetailMidDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 报关明细中间表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-04-27
 */
@Mapper
public interface DeliveryDeclareDetailMidMapper extends BaseMapper<DeliveryDeclareDetailMidEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<DeliveryDeclareDetailMidDTO.ListDTO> paging(Page query, @Param("params") DeliveryDeclareDetailMidDTO.PagingParamDTO params);


    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<DeliveryDeclareDetailMidDTO.ListDTO> listExport(@Param("params") DeliveryDeclareDetailMidDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<DeliveryDeclareDetailMidDTO.TabListDTO> tabList(@Param("params") DeliveryDeclareDetailMidDTO.PagingParamDTO searchParam);
    /**
     * 根据报关单id查询
     * @author will
     * @date 2026/4/23 15:28
     * @param declareBillIdList
     * @return java.util.List<com.erp.model.tms.entity.DeliveryDeclareDetailMidEntity>
     */
    List<DeliveryDeclareDetailMidEntity> listByDeclareBillIdList(@Param("declareBillIdList") List<String> declareBillIdList);
    /**
     * 根据报关单ids查询来源信息，包含申报要素
     * @author will
     * @date 2026/4/23 18:33
     * @param declareBillIdList
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.SourceDeliveryDetailDTO>
     */
    List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> listSourceByDeclareIdList(@Param("declareBillIdList") List<String> declareBillIdList);
}
