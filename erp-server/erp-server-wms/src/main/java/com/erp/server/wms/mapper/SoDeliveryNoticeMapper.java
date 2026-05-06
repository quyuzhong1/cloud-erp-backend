package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.dto.inventory.VirtualFlowRefactorDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * <p>
 * 发货通知单主表明细表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-05-10
 */
@Mapper
public interface SoDeliveryNoticeMapper extends BaseMapper<SoDeliveryNoticeEntity> {

    IPage<SoDeliveryNoticeDTO.PagingView> paging(Page query, @Param("params") SoDeliveryNoticeDTO.PagingParam params);

    Integer listCount(@Param("params") SoDeliveryNoticeDTO.PagingParam pagingParam);


    List<SoDeliveryNoticeDTO.PagingView> soDeliveryNoticeExportExcel(@Param("params") SoDeliveryNoticeDTO.PagingParam dto);
    Page<SoDeliveryNoticeDTO.PagingView> soDeliveryNoticeExportExcel(@Param("page") Page<SoDeliveryNoticeDTO.PagingView> page, @Param("params") SoDeliveryNoticeDTO.PagingParam dto);

    /**
     * 获取到销售退货单列表
     * @author yl
     * @date 2023-05-23 14:05
     * @param idList
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDTO.GenerateSoOutstockViewDTO>
     */
    List<SoOutstockDTO.GenerateSoOutstockViewDTO> listGenerateSoOutstockView(@Param("idList") List<String> idList,@Param("sourceType") String sourceType);

    /**
     * 销售单详情-单据关联-发货通知单
     * @Author Luo_WG
     * @Date 2023/5/25 16:37
     * @param sourceId sourceId
     * @return java.util.List<com.erp.model.wms.dto.SoDeliveryNoticeDTO.PagingView>
     **/
    List<SoDeliveryNoticeDTO.PagingView> listSoReturnDetailBySourceId(@Param("sourceId") String sourceId);

    /**
     * PDA:条件查询收货单
     * @Author Luo_WG
     * @Date 2023/8/22 18:55
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.SoDeliveryNoticeDTO.PdaSoDeliveryNotice>
     **/
    List<SoDeliveryNoticeDTO.PdaSoDeliveryNotice> pdaList(SoDeliveryNoticeDTO.PdaSoDeliveryNoticeParam dto);

    /**
     *
     * @param page 分页参数
     * @param id id
     * @param ignoreInventorySkus 忽略得sku
     */
    IPage<SoDeliveryNoticeDTO.PickingViewDTO> pagingPicking(@Param("page") Page<SoDeliveryNoticeDTO.PickingViewDTO> page,@Param("id") String id,@Param("ignoreInventorySkus") List<String> ignoreInventorySkus);
    /**
     * b2b虚拟仓流水
     * @author will
     * @date 2025/3/31 11:31
     * @return java.util.List<com.erp.model.wms.entity.VirtualTransFlowEntity>
     */
    List<VirtualFlowRefactorDTO.OutInStockDTO> rebuildB2bVirtualFlow();

    List<SoDeliveryNoticeDTO.PrintSkuLabelDTO> printSkuLabelView(@Param("detailIds") List<String> detailIds);

    /**
     * 查询未生成报关单的b2b发货通知单明细信息
     * @author will
     * @date 2026/4/22 10:26
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.NotGenerateDetailDTO>
     */
    IPage<TmsDeclareBillDTO.NotGenerateDetailDTO> listNotGenerateB2bDetailPaging(Page query,@Param("params") TmsDeclareBillDTO.NotGenerateParamDTO params);

    /**
     * 查询需要下推的报关数据
     * @author will
     * @date 2026/4/24 16:29
     * @param ids 发货通知单id集合
     * @param detailIds 发货通知单明细id集合
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.SourceDeliveryDetailDTO>
     */
    List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> listBeforePushB2bDeclare(@Param("ids") List<String> ids, @Param("detailIds") List<String> detailIds);

    /**
     * 查询用于报关中间表生成的装箱明细
     *
     * @param ids 发货通知单id集合
     * @return 装箱明细集合
     * @throws RuntimeException 查询异常时抛出
     * @author jack
     * @date 2026-04-29
     */
    List<WmsCartonDetailDTO.ListPackingDetailDTO> listDeclarePackingDetail(@Param("ids") List<String> ids);
}
