package com.erp.server.tms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.TmsDeclareBillEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 报关单 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
 */
public interface TmsDeclareBillService extends SuperService<TmsDeclareBillEntity> {

    /**
    * 修改
    * @author lrp
    * @date: 2024-03-27
    * @param dto
    * @return
    */
    Boolean update(TmsDeclareBillDTO.UpdateDTO dto,SourceTypeEnum sourceTypeEnum);


    List<TmsDeclareBillDTO.TabListDTO> tabList(SourceTypeEnum sourceTypeEnum,PermissionsDTO permissionsDTO);

    PagingVO<TmsDeclareBillDTO.PagingVO> paging(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);

    TmsDeclareBillDTO.StatisticsVO statisticsByFm(PermissionsDTO permissionsDTO);

    List<TmsDeclareBillDTO.DeliveryDTO> getCanGenerateDeliveryOrder(TmsDeclareBillDTO.QuerySourceDTO querySourceDTO);

    /**
     * 根据选中SKU查询报关表头信息
     * @author will
     * @date 2026/5/7 14:47
     * @param dto
     * @param sourceTypeEnum
     * @return com.erp.model.tms.dto.TmsDeclareBillDTO.SelectedSkuHeaderDTO
     */
    TmsDeclareBillDTO.SelectedSkuHeaderDTO querySelectedSkuHeader(TmsDeclareBillDTO.SelectedSkuHeaderParamDTO dto, SourceTypeEnum sourceTypeEnum);

    TmsDeclareBillDTO.ViewDTO view(String id);

    BatchResultDTO confirmDeclareStatus(TmsDeclareBillDTO.ConfirmDeclareStatusDTO dto, SourceTypeEnum sourceTypeEnum);

    List<BatchResultDTO> delete(TmsDeclareBillDTO.DeleteDTO dto);

    Boolean addFmDeclare(TmsDeclareBillDTO.AddDTO dto);

    /**
     * 根据来源id查询报关单
     * @Author Luo_WG
     * @Date 2024/4/1 12:05
     * @param sourceIds
     * @return java.util.List<com.erp.model.tms.entity.TmsDeclareBillEntity>
     **/
    List<TmsDeclareBillEntity> listBySourceIds(List<String> sourceIds);

    List<TmsDeclareBillDTO.SoOutDTO> getCanGenerateSoOut(TmsDeclareBillDTO.QuerySourceDTO querySourceDTO);

    TmsDeclareBillDTO.StatisticsVO statisticsBySoOut(PermissionsDTO permissionsDTO);

    Boolean addB2BDeclare(TmsDeclareBillDTO.AddDTO dto);

    Boolean autoGenerateFirstMileDeclare(AutoGenerateBillDTO autoGenerateBillDTO);

    Boolean autoGenerateB2bDeclare(AutoGenerateBillDTO autoGenerateBillDTO);

    PagingVO<TmsDeclareBillDTO.PagingVO> export(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);

    void exportDeclare(TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) throws IOException;
    /**
     * 更新备注
     * @author will
     * @date 2026/4/21 14:43
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     */
    BatchResultDTO updateRemark(String id,String remark);

    /**
     * 批量更新报关单主表字段
     * @param dto dto
     * @param sourceTypeEnum 报关单类型
     * @return Boolean
     */
    Boolean updateBatchFiled(TmsDeclareBillDTO.BatchUpdateFieldDTO dto, SourceTypeEnum sourceTypeEnum);

    /**
     * 查询报关单批量更新字段下拉配置
     * @return 下拉字段配置
     */
    List<TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO> batchUpdateFieldDropDown();

    /**
     * 删除报关单
     * @author will
     * @date 2026/4/30 10:59
     * @param id
     */
    void deleteDeclareBillById (String id);
    /**
     * 查询拆分报关明细
     * @author will
     * @date 2026/4/23 15:21
     * @param id
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.SplitDeclareDTO>
     */
    List<TmsDeclareBillDTO.SplitDeclareDTO> listSplitB2bDetail(String id);
    /**
     * 批量保存拆分报关明细
     * @author will
     * @date 2026/4/24 12:30
     * @param declareDTO
     * @return java.lang.Boolean
     */
    Boolean batchAddSplitB2bDetail(TmsDeclareBillDTO.AddSplitDeclareDTO declareDTO);
    /**
     * 查询拆分报关明细
     * @author will
     * @date 2026/4/23 15:21
     * @param id
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.SplitDeclareDTO>
     */
    List<TmsDeclareBillDTO.SplitDeclareDTO> listSplitFmDetail(String id);
    /**
     * 批量保存拆分报关明细
     * @author will
     * @date 2026/4/23 16:39
     * @param declareDTO
     */
    Boolean batchAddSplitFmDetail(TmsDeclareBillDTO.AddSplitDeclareDTO declareDTO);
    /**
     * 查询合并前的报关明细
     * @author will
     * @date 2026/4/24 10:21
     * @param ids
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBeforeDTO>
     */
    List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> listBeforeMergeDetail( List<String> ids);
    /**
     * 查询合并后的报关明细
     * @author will
     * @date 2026/4/23 19:09
     * @param ids
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBillDTO>
     */
    List<TmsDeclareBillDTO.MergeDeclareBillDTO> listAfterMergeDetail( List<String> ids);
    /**
     * 批量保存合并后的报关明细
     * @author will
     * @date 2026/4/24 10:22
     * @param list
     * @return Boolean
     */
    Boolean batchAddMergeDetail(String type, List<TmsDeclareBillDTO.MergeDeclareBillDTO> list) ;

    /**
     * 合并报关单数据
     * @author will
     * @date 2026/4/29 15:07
     * @param viewDTO
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBillDTO>
     */
    List<TmsDeclareBillDTO.MergeDeclareBillDTO> autoMergeDeclareBillView(TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO viewDTO) ;

    /**
     * 根据报关明细中间表自动生成报关单
     * @author jack
     * @date 2026/4/30 16:35
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean autoGenerateDeclareBillByMid(AutoGenerateBillDTO dto);
}
