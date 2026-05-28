package com.erp.server.wms.convert;

import com.common.business.dto.AttachDTO;
import com.erp.model.wms.dto.FileManagementDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.dto.WmsVirtualDetailMsgDTO;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.model.wms.entity.WmsVirtualDetailMsgEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件管理实体映射工具
 * @Author Luo_WG
 * @Date 2023/10/31 18:55
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface WmsAttachmentConverter {
    WmsAttachmentConverter INSTANCE = Mappers.getMapper(WmsAttachmentConverter.class);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "type", source = "fileType")
    @Mapping(target = "businessId", ignore = true)
    WmsAttachmentEntity addFileManagementToAttachment(FileManagementDTO.AddDTO addDTO);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "type", source = "fileType")
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "businessId", source = "id")
    WmsAttachmentEntity updateFileManagementToAttachment(FileManagementDTO.UpdateDTO addOrUpdateDTO);

    @Mapping(target = "attachSizeStr",source = "attachSize",qualifiedByName = "decimal2ToPlainString")
    FileManagementDTO.VersionDTO entityToVersionDTO(WmsAttachmentEntity attachmentEntity);
    List<FileManagementDTO.VersionDTO> entityToVersionDTO(List<WmsAttachmentEntity> attachmentEntityList);

    AttachDTO entity2DTOList(WmsAttachmentEntity entity);
    List<AttachDTO> entity2DTOList(List<WmsAttachmentEntity> entityList);
}
