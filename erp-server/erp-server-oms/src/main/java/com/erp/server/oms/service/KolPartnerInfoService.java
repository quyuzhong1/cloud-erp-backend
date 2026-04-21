package com.erp.server.oms.service;
import com.erp.model.oms.dto.excel.KolPartnerInfoImportExcelDTO;
import com.erp.model.oms.entity.KolPartnerInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.AddressParseDTO;
import com.erp.model.oms.dto.KolPartnerInfoDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 企业达人库 服务类
 * </p>
 *
 * @author jack
 * @since 2025-12-02
 */
public interface KolPartnerInfoService extends SuperService<KolPartnerInfoEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-12-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KolPartnerInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-12-02
    * @param dto
    * @return
    */
    Boolean update(KolPartnerInfoDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author jack
    * @date: 2025-12-02
    * @param pagingParamDTO
    * @return PagingVO<KolPartnerInfoDTO.ListDTO>>
    */
    PagingVO<KolPartnerInfoDTO.ListDTO> paging(PagingDTO<KolPartnerInfoDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2025-12-02
    * @param dto
    * @return List<KolPartnerInfoDTO.TabListDTO>>
    */
    List<KolPartnerInfoDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2025-12-02
    * @param id
    * @return
    */
    KolPartnerInfoDTO.ViewDTO view(String id);

    /**
    * 删除
    * @author jack
    * @date: 2025-12-02
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);


    /**
    * 导出Excel
    * @author jack
    * @date: 2025-12-02
    * @param dto
    * @param response
    * @return
    */
    void exportList(KolPartnerInfoDTO.PagingParamDTO dto, HttpServletResponse response);


    List<KolPartnerInfoDTO.DropDownDTO> dropDown(KolPartnerInfoDTO.SelectDTO dto);

    BatchResultDTO disabled(String id,  Boolean disabled);

    Boolean importFile(BaseDTO.ImportDTO dto);

    void importKolPartnerInfo(BaseDTO.ImportDTO dto);

    void handleImportSuccessList(List<KolPartnerInfoImportExcelDTO> successList, List<String> errorNoList, List<KolPartnerInfoImportExcelDTO> errorList2, String importType);

    List<KolPartnerInfoDTO.PartnerAddressDTO> partnerAddressList(KolPartnerInfoDTO.AddressSelectDTO dto);

    AddressParseDTO.ParseResultDTO addressParse(AddressParseDTO.ParseRequestDTO dto);
}
