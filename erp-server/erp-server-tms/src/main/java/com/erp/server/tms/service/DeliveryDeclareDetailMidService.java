package com.erp.server.tms.service;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.DeliveryDeclareDetailMidEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.DeliveryDeclareDetailMidDTO;
import com.common.business.vo.PagingVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 报关明细中间表 服务类
 * </p>
 *
 * @author jack
 * @since 2026-04-27
 */
public interface DeliveryDeclareDetailMidService extends SuperService<DeliveryDeclareDetailMidEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2026-04-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DeliveryDeclareDetailMidDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2026-04-27
    * @param dto
    * @return
    */
    Boolean update(DeliveryDeclareDetailMidDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author jack
    * @date: 2026-04-27
    * @param pagingParamDTO
    * @return PagingVO<DeliveryDeclareDetailMidDTO.ListDTO>>
    */
    PagingVO<DeliveryDeclareDetailMidDTO.ListDTO> paging(PagingDTO<DeliveryDeclareDetailMidDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2026-04-27
    * @param dto
    * @return List<DeliveryDeclareDetailMidDTO.TabListDTO>>
    */
    List<DeliveryDeclareDetailMidDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2026-04-27
    * @param id
    * @return
    */
    DeliveryDeclareDetailMidDTO.ViewDTO view(String id);

    /**
     * 合并前预览
     *
     * @param ids 报关明细中间表id集合
     * @return 合并前预览列表
     * @throws com.common.core.exception.ServiceException 校验失败时抛出
     * @author jack
     * @date 2026-04-29
     */
    List<DeliveryDeclareDetailMidDTO.MergePreviewDTO> mergePreview(List<String> ids);


    /**
     * 合并后预览
     *
     * @param ids 报关明细中间表id集合
     * @return 合并后预览列表
     * @throws com.common.core.exception.ServiceException 校验失败时抛出
     * @author jack
     * @date 2026-05-06
     */
    List<TmsDeclareBillDTO.MergeDeclareBillDTO> mergeAfterPreview(List<String> ids);


    /**
    * 导出Excel
    * @author jack
    * @date: 2026-04-27
    * @param dto
    * @param response
    * @return
    */
    void exportList(DeliveryDeclareDetailMidDTO.ExportDTO dto, HttpServletResponse response);
    /**
     * 根据报关单id查询
     * @author will
     * @date 2026/4/23 15:27
     * @param declareBillIdList
     * @return java.util.List<com.erp.model.tms.entity.DeliveryDeclareDetailMidEntity>
     */
    List<DeliveryDeclareDetailMidEntity> listByDeclareBillIdList(List<String> declareBillIdList);
    /**
     * 根据报关单ids查询来源信息，包含申报要素
     * @author will
     * @date 2026/4/23 18:32
     * @param declareBillIdList
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.SourceDeliveryDetailDTO>
     */
    List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> listSourceByDeclareIdList(List<String> declareBillIdList);
    /**
     * 删除tms发货明细
     * @author will
     * @date 2026/4/24 14:57
     * @param sourceIds
     * @return java.lang.Boolean
     */
    Boolean deleteDeliveryDeclareDetailMid(List<String> sourceIds);

    /**
     * 按报关单id恢复中间表为待生成状态
     * @author will
     * @date 2026/5/13 16:14
     * @param declareBillIds 报关单id集合
     * @return java.lang.Boolean
     */
    Boolean removeByDeclareBillIds(List<String> declareBillIds);
    /**
     * 按报关单表id恢复为待生成状态
     * @author will
     * @date 2026/5/13 16:19
     * @param declareBillIds 中间表报关id集合
     * @return java.lang.Boolean
     */
    Boolean restoreWaitGenerateByDeclareBillIds(List<String> declareBillIds);
    /**
     * 按中间表id恢复为待生成状态
     * @author will
     * @date 2026/5/13 16:19
     * @param ids 中间表id集合
     * @return java.lang.Boolean
     */
    Boolean restoreWaitGenerateByIds(List<String> ids);

    /**
     * 自动生成报关明细中间表
     *
     * @param dto 自动生成参数
     * @return 是否生成成功
     * @throws com.common.core.exception.ServiceException 自动生成失败时抛出
     * @author jack
     * @date 2026-04-29
     */
    Boolean autoGenerateMidData(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> list);

    Boolean batchAddMergeDetail(List<TmsDeclareBillDTO.MergeDeclareBillDTO> list);
    /**
     * 来源id集合
     * @author will
     * @date 2026/5/12 09:26
     * @param sourceIds
     * @return java.util.List<com.erp.model.tms.entity.TmsDeclareBillEntity>
     */
    List<DeliveryDeclareDetailMidEntity> listBySourceIdList(List<String> sourceIds);
}
