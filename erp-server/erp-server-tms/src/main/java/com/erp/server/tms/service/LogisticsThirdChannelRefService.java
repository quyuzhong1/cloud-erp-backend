package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.LogisticsThirdChannelRefEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 物流-第三方渠道关系表 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-05-29
 */
public interface LogisticsThirdChannelRefService extends SuperService<LogisticsThirdChannelRefEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-05-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsThirdChannelRefDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-05-29
    * @param dto
    * @return
    */
    Boolean update(LogisticsThirdChannelRefDTO.UpdateDTO dto);


    List<LogisticsThirdChannelRefEntity> listByChannelIds(List<String> channelIds);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PagingVO<LogisticsThirdChannelRefDTO.PagingVO> paging(PagingDTO<LogisticsThirdChannelRefDTO.PagingParamDTO> dto);
    /**
     * 查看详情
     *
     * @param id
     * @return com.erp.model.tms.dto.LogisticsThirdChannelRefDTO.ViewDTO
     * @author zdy
     * @Time ${YEAR}年${MONTH}月${DAY}日
     **/
    LogisticsThirdChannelRefDTO.ViewDTO view(String id);

    /**
     * 导出
     * @param dto
     * @return
     */
    Boolean export(LogisticsThirdChannelRefDTO.PagingParamDTO dto);

    /**
     * 删除
     * @param id
     * @return
     */
    BatchResultDTO delete(LogisticsThirdChannelRefEntity id);

    /**
     * 启用/停用
     * @param id
     * @param disabled
     * @return
     */
    BatchResultDTO updateStatus(String id, Boolean disabled);

    /**
     * 导入
     * @param excelFile
     * @param response
     */
    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response);

    void downloadTemplate(HttpServletResponse response);

    /**
     * 根据平台类型获取配置
     * @param platformType
     * @return
     */
    List<LogisticsThirdChannelRefDTO.PagingVO> listByPlatform(String platformType);
}
