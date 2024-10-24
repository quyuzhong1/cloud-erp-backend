package com.erp.server.mrp.service;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.DeliverySuggestEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 发货计划 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-27
 */
public interface DeliverySuggestService extends SuperService<DeliverySuggestEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DeliverySuggestDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    Boolean update(DeliverySuggestDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @author will
     * @date 2024/10/16 10:36
     * @param pagingDTO
     * @return PagingVO<ListDTO>
     */
    PagingVO<DeliverySuggestDTO.ListDTO> paging(PagingDTO<DeliverySuggestDTO.PagingParamDTO> pagingDTO);

    /**
     * 列表查询
     * @author will
     * @date 2024/9/9 14:12
     * @param params
     * @return List<DeliverySuggestEntity>
     */
    List<DeliverySuggestDTO.ListDTO> list(DeliverySuggestDTO.ListParamDTO params);

    /**
     * 发货
     * @param detailId 明细id
     */
    List<DeliverySuggestEntity> listByReplenishmentId(String detailId);
    /**
     * 查询发货建议导出数据
     * @author will
     * @date 2024/10/12 14:56
     * @param dto
     * @return PagingVO<DeliverySuggestionDTO>
     */
    PagingVO<ReplenishmentSuggestionDTO.DeliverySuggestionDTO> listDeliverySuggestion(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);
    /**
     * 下载模板
     * @author will
     * @date 2024/10/16 10:55
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);
    /**
     * 锁定
     * @author will
     * @date 2024/10/16 11:24
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO locking(String id);
    /**
     * 确认
     * @author will
     * @date 2024/10/16 11:00
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO confirm(String id);
    /**
     * 作废
     * @author will
     * @date 2024/10/16 11:34
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO invalid(String id,String remark);
    /**
     * 导出发货建议
     * @author will
     * @date 2024/10/16 12:12
     * @param pagingParamDTO
     * @return Boolean
     */
    Boolean export(DeliverySuggestDTO.PagingParamDTO pagingParamDTO);
    /**
     * 下推发货计划
     * @author will
     * @date 2024/10/17 18:22
     * @param ids
     * @return ViewPushDeliveryPlanDTO
     */
    DeliverySuggestDTO.ViewPushDeliveryPlanDTO viewPushDeliveryPlan(List<String> ids);
    /**
     * 下推发货计划保存
     * @author will
     * @date 2024/10/18 10:10
     * @param deliveryPlanDTO
     * @return Boolean
     */
    Boolean pushDeliveryPlan(DeliverySuggestDTO.AddPushDeliveryPlanDTO deliveryPlanDTO);
    /**
     * 更新备注
     * @author will
     * @date 2024/10/23 17:44
     * @param id
     * @param remark
     * @return BatchResultDTO
     */
    BatchResultDTO updateRemark(String id, String remark);
    /**
     * 导入补货计划
     * @author will
     * @date 2024/10/24 10:43
     * @param excelFile
     * @param platformType
     * @param response
     */
    void importDeliverySuggest(MultipartFile excelFile, String platformType, HttpServletResponse response);

    /**
     * 导入更新
     * @author will
     * @date 2024/10/24 11:25
     * @param updateDTO
     * @return Boolean
     */
    Boolean importUpdate(DeliverySuggestDTO.ImportUpdateDTO updateDTO);
}
