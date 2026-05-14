package com.erp.server.tms.mapper;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.DeliveryDeclareDetailMidEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.erp.model.plm.dto.BomChildrenSkuDTO;
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

    /**
     * 根据 BOM 历史 id 查询历史父子件。
     *
     * @param bomHistoryIds BOM 历史 id 集合
     * @return BOM 历史父子件
     */
    List<BomChildrenSkuDTO> listBomHistoryByIds(@Param("bomHistoryIds") List<String> bomHistoryIds);

    /**
     * 根据组合品父 SKU 查询全部历史父子件。
     *
     * @param parentSkuIds 组合品父 SKU id 集合
     * @return BOM 历史父子件
     */
    List<BomChildrenSkuDTO> listBomHistoryByParentSkuIds(@Param("parentSkuIds") List<String> parentSkuIds);

    /**
     * 查询来源单是否仍存在待生成中间表明细。
     *
     * @param sourceType 来源类型
     * @param sourceIds 来源单 id 集合
     * @return 仍存在待生成明细的来源单 id
     */
    List<String> listWaitSourceIds(@Param("sourceType") String sourceType, @Param("sourceIds") List<String> sourceIds);
}
