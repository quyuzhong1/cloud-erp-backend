package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.DictHsCodeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.DictHsCodeDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 出口申报要素表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-07-11
 */
public interface DictHsCodeService extends SuperService<DictHsCodeEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-07-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DictHsCodeDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-07-11
    * @param dto
    * @return
    */
    Boolean update(DictHsCodeDTO.UpdateDTO dto);

    /**
     * 分页查询
     *
     * @return
     */
    PagingVO<DictHsCodeDTO.ListDTO> paging(PagingDTO<DictHsCodeDTO.PagingParamDTO> dto);
    /**
     * 删除
     * @author jack
     * @date:  2025-06-21
     * @param id
     * @return ApiResult<List<BatchResultDTO>>
     */
    BatchResultDTO delete(String id);
    /**
     * 详情
     * @author jack
     * @date:  2025-06-21
     * @return ApiResult<DictHsCodeDTO.ViewDTO>>
     */
    DictHsCodeDTO.ViewDTO view(String id);
    /**
     * 导出Excel数据
     * @author jack
     * @date:  2025-06-21
     * @param dto
     * @param response
     * @return
     */
    void exportList(DictHsCodeDTO.PagingParamDTO dto, HttpServletResponse response);
    /**
     * 导入
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * 下载模板
     *
     * @return
     */
    void downloadTemplate(HttpServletResponse response);
}
