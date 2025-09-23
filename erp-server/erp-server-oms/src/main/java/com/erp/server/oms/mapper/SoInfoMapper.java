package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.ExhibitionOrderDTO;
import com.erp.model.oms.dto.ListingTimeDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 销售订单信息 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface SoInfoMapper extends BaseMapper<SoInfoEntity> {

    IPage<SoInfoDTO.PagingViewDTO> paging(Page query, @Param("params") SoInfoDTO.PagingParamDTO params);

    /**
     * 查询所有总数
     */
    SoInfoDTO.PagingTotalDTO pagingTotal(@Param("params") SoInfoDTO.PagingParamDTO params);

    /**
     * 查询需要汇总的订单详情id
     * @param params
     * @param paramDetailIds
     * @param soIdList
     * @return
     */
    List<String> pagingTotalGetDetailIds(@Param("params") SoInfoDTO.PagingParamDTO params);


    List<SoInfoDTO.PagingViewDTO> listExport(@Param("params") SoInfoDTO.ExportDTO dto);
    Page<SoInfoDTO.PagingViewDTO> listExport(@Param("page") Page<SoInfoDTO.PagingViewDTO> page, @Param("params") SoInfoDTO.ExportDTO dto);
    /**
     * @description: 下推备货申请单数据显示
     * @author Will
     * @date: 2023/5/19 11:14
     * @param ids
     * @return List<ViewGenerateSalesDemandDTO>
     */
    List<SoInfoDTO.ViewGenerateSalesDemandDTO> viewGenerateSalesDemand(@Param("ids") List<String> ids);

    /**
     * 下推发货通知单\销售出库单-列表查询
     * @Author Luo_WG
     * @Date 2023/5/25 12:03
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.GenerateDeliveryView>
     **/
    List<SoInfoDTO.GenerateDeliveryView> generateDeliveryView(@Param("ids")List<String> ids);

    /**
     * 下推销售退货订单-列表查询
     * @Author Luo_WG
     * @Date 2023/5/25 15:19
     * @param detailIds detailIds
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.GenerateSoReturnView>
     **/
    List<SoInfoDTO.GenerateSoReturnView> generateSoReturnView(@Param("detailIds") List<String> detailIds);

    /**
     * 获取折扣额大于0 的
     * @author yl
     * @date 2023-09-28 10:32
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.ListDTO>
     */
    List<SoInfoDTO.ListDTO> listRepairHistoryDb();

    /**
     * 获取首批上市时间
     * @param ids
     * @return java.util.List<com.erp.model.oms.dto.ListingTimeDTO>
     **/
    List<ListingTimeDTO> listFirstListingTime(@Param("ids") List<String> ids);


    List<SoInfoEntity> queryToSdy(@Param("startDate")LocalDate startDate, @Param("endDate")LocalDate endDate, @Param("pageSize")Integer pageSize, @Param("offset")int offset);

    IPage<SoInfoEntity> pagePartitionIsNull(Page query);

    Boolean existsByCustomerAndSku(@Param("customer")String customer,@Param("platformSku") String platformSku);
    /**
     * 查询采购申请数据
     * @author will
     * @date 2025/5/30 14:07
     * @param ids
     * @return List<ViewPushPurchaseApplicationDTO>
     */
    List<SoB2cDTO.ViewPushPurchaseApplicationDTO> viewPushPurchaseApplication(@Param("ids")List<String> ids);

    List<ExhibitionOrderDTO.DownstreamListDTO> listByExhibitionId(@Param("exhibitionId") String exhibitionId);
}
