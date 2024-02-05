package com.sdk.tms.baohong.dto.response;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@Builder
@ToString
@AllArgsConstructor
public class BaoHongResponse<T> implements Serializable {

    private String ask;

    private String message;

    private T data;
}
