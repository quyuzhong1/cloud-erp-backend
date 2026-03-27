package com.erp.server.wms.convert;

import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.FileManagementDTO;
import com.erp.model.wms.entity.FileManagementEntity;
import com.erp.model.wms.entity.QcStandardSkuRefEntity;
import com.erp.model.wms.entity.WmsAttachmentEntity;
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
public interface FileManagementConverter {
    FileManagementConverter INSTANCE = Mappers.getMapper(FileManagementConverter.class);

    @Mapping(target = "fileTypeName", expression = "java(com.common.core.constant.EnumMessage.getNameByCode(com.erp.model.wms.enums.WmsFileTypeEnum.class, fileManagementEntity.getFileType()))")
    @Mapping(target = "id", source = "fileManagementEntity.id")
    @Mapping(target = "code", source = "fileManagementEntity.code")
    @Mapping(target = "skuId", source = "fileManagementEntity.skuId")
    @Mapping(target = "skuNo", source = "fileManagementEntity.skuNo")
    @Mapping(target = "fileType", source = "fileManagementEntity.fileType")
    @Mapping(target = "firstCategoryId", source = "fileManagementEntity.firstCategoryId")
    @Mapping(target = "firstCategoryName", source = "fileManagementEntity.firstCategoryName")
    @Mapping(target = "productName", source = "fileManagementEntity.productName")
    @Mapping(target = "attachVersion", source = "attachmentEntity.attachVersion")
    @Mapping(target = "attachUrl", source = "attachmentEntity.attachUrl")
    @Mapping(target = "attachSize", source = "attachmentEntity.attachSize")
    @Mapping(target = "attachName", source = "attachmentEntity.attachName")
    FileManagementDTO.ViewDTO fileManagementToViewDTO(FileManagementEntity fileManagementEntity, WmsAttachmentEntity attachmentEntity);

    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "productName", source = "skuName")
    @Mapping(target = "mainId", ignore = true)
    @Mapping(target = "isUserSystem", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    QcStandardSkuRefEntity skuVOToSkuRefEntity(SkuVO skuVO);
    List<QcStandardSkuRefEntity> skuVOToSkuRefEntity(List<SkuVO> skuVOS);
}
