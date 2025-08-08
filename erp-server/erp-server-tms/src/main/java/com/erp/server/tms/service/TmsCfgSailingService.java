package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsCfgSailingDTO;
import com.erp.model.tms.entity.TmsCfgSailingEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 截单开船配置 服务类
 * </p>
 *
 * @author will
 * @since 2024-03-15
 */
public interface TmsCfgSailingService extends SuperService<TmsCfgSailingEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-03-15
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsCfgSailingDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-03-15
    * @param dto
    * @return
    */
    Boolean update(TmsCfgSailingDTO.UpdateDTO dto);

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/3/18 9:08
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<TmsCfgSailingDTO.ListDTO> paging(PagingDTO<TmsCfgSailingDTO.PagingParamDTO> dto);

    /**
     * @description: 查看详情
     * @author Will
     * @date: 2024/3/18 9:14
     * @param id
     * @return ViewDTO
     */
    TmsCfgSailingDTO.ViewDTO view(String id);

    /**
     * @description: 删除
     * @author Will
     * @date: 2024/3/18 9:14
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO delete(String id);
    /**
     * @description: 根据物流供应商和物流渠道查询
     * @author Will
     * @date: 2024/3/18 17:06
     * @param logisticsChannelIdList
     * @return List<TmsCfgSailingEntity>
     */
    List<TmsCfgSailingEntity>  getByLogisticsChannelIdList (List<String> logisticsChannelIdList);

    /**
     * 通过渠道id和下单时间计算出开船时间
     */
    LocalDateTime calculateShipTime(String logisticsChannelId, LocalDateTime orderTime);
    /**
     * 导出Excel数据
     * @author jack
     * @date:  2025-07-17
     * @param dto
     * @param response
     * @return
     */
    void exportList(TmsCfgSailingDTO.PagingParamDTO dto, HttpServletResponse response);
    /**
     * 导入
     * @author jack
     * @date:  2025-07-17
     * @return
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * 下载模板
     * @author jack
     * @date:  2025-07-17
     * @return
     */
    void downloadTemplate(HttpServletResponse response);
}
