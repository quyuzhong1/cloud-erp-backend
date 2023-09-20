package com.erp.server.bi.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiTargetShopSettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.entity.BiTargetShopSettingEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 店铺目标设置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
public interface BiTargetShopSettingService extends SuperService<BiTargetShopSettingEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    String add(BiTargetShopSettingDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    Boolean update(BiTargetShopSettingDTO.UpdateDTO dto);

    /**
     * 获取详情
     * @author yl
     * @date 2023-09-14 16:16
     * @param id
     * @return com.erp.model.bi.dto.BiTargetShopSettingDTO.ViewDTO
     */
    BiTargetShopSettingDTO.ViewDTO view(String id);

    /**
     * 分页查询
     * @author yl
     * @date 2023-09-14 16:43
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.bi.dto.BiTargetShopSettingDTO.PagingViewDTO>
     */
    PagingVO<BiTargetShopSettingDTO.PagingViewDTO> paging(PagingDTO<BiTargetYearDTO.PagingParamDTO> dto);
    /**
     * @description: 查询目标值
     * @author Will
     * @date: 2023/9/15 11:26
     * @param dto
     * @return List<TargetFinishDTO.ViewDTO>
     */
    List<TargetFinishDTO.ViewDTO> listTargetFinish(TargetFinishDTO.ParamDTO dto);

    /**
     * 下载模板
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 导入
     * @author yl
     * @date 2023-09-15 16:32
     * @param excelFile
     * @param response
     * @return com.erp.model.bi.dto.BiTargetShopSettingDTO.ImportDTO
     */
    BiTargetShopSettingDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response);

    /**
     * 列表删除店铺目标
     * @param dto
     * @return
     */
    Boolean delete(BiTargetShopSettingDTO.RemoveDTO dto);
}
