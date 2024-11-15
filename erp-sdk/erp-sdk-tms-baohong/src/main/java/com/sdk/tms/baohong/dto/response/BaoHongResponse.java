package com.sdk.tms.baohong.dto.response;

import lombok.*;

import java.io.Serializable;

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
