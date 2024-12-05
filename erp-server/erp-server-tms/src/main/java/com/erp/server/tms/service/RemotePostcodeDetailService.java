package com.erp.server.tms.service;
import com.erp.model.tms.dto.RemotePostcodeDTO;
import com.erp.model.tms.entity.RemotePostcodeDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.RemotePostcodeDetailDTO;
import com.common.business.vo.PagingVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 偏远邮编明细表 服务类
 * </p>
 *
 * @author jack
 * @since 2024-11-29
 */
public interface RemotePostcodeDetailService extends SuperService<RemotePostcodeDetailEntity> {

    /**
     * 新增
     * @author jack
     * @date: 2024-11-29
     * @param addDTO
     * @param mainId
     * @return
     */
    Boolean add(RemotePostcodeDTO.AddDTO addDTO,String mainId);

    /**
     * 修改
     * @author jack
     * @date: 2024-11-29
     * @param dto
     * @return
     */
    Boolean update(RemotePostcodeDTO.UpdateDTO dto,String mainId);

    /**
    * 分页列表查询
    * @author jack
    * @date: 2024-11-29
    * @param pagingParamDTO
    * @return PagingVO<RemotePostcodeDetailDTO.ListDTO>>
    */
    PagingVO<RemotePostcodeDetailDTO.ListDTO> paging(PagingDTO<RemotePostcodeDetailDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return List<RemotePostcodeDetailDTO.TabListDTO>>
    */
    List<RemotePostcodeDetailDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2024-11-29
    * @param id
    * @return
    */
    RemotePostcodeDetailDTO.ViewDTO view(String id);
    /**
    * 删除
    * @author jack
    * @date: 2024-11-29
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 导出Excel
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @param response
    * @return
    */
    void exportList(RemotePostcodeDetailDTO.ExportDTO dto, HttpServletResponse response);
    /**
     * 根据mainid删除明细表
     * @param mainIds
     * @return
     */
    void removeByMainIds(List<String> mainIds);
    /**
     * 根据mainid查询明细表
     * @param mainIds
     * @return
     */
    List<RemotePostcodeDetailDTO.ViewDTO> listByMainIds(List<String> mainIds);
    /**
     * 导入偏远邮编组
     * @author jack
     * @date:  2024-11-29
     * @param excelFile
     * @param response
     * @return ApiResult
     */
    RemotePostcodeDetailDTO.ImportResultDTO importFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * 下载模板
     * @author jack
     * @date:  2024-11-29
     * @param response
     * @return void
     */
    void downloadTemplate(HttpServletResponse response);
}
