package com.all4land.parkinglotnavigator.domain.commoncode.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.all4land.parkinglotnavigator.domain.commoncode.dto.CommonCodeResDTO;
import com.all4land.parkinglotnavigator.domain.commoncode.service.CommonCodeService;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.EnCmmErrCode;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommonCodeController.class)
class CommonCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommonCodeService service;

    private static CommonCodeResDTO code() {
        return new CommonCodeResDTO(
                2L, 1L, "PUBLIC", "공영", 2, 1, null, "ACTIVE", LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void listsRootsAndDirectChildren() throws Exception {
        CommonCodeResDTO root = new CommonCodeResDTO(
                1L, null, "PARKING_CATEGORY", "주차장 분류", 1, 2, null, "ACTIVE", LocalDateTime.now(), LocalDateTime.now());
        when(service.getRoots(false)).thenReturn(List.of(root));
        when(service.getChildren("PARKING_CATEGORY", false)).thenReturn(List.of(code()));

        mockMvc.perform(get("/api/common-codes/roots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("PARKING_CATEGORY"));
        mockMvc.perform(get("/api/common-codes/PARKING_CATEGORY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("공영"));
        verify(service).getChildren("PARKING_CATEGORY", false);
    }

    @Test
    void supportsCrudEndpoints() throws Exception {
        when(service.create(any())).thenReturn(code());
        when(service.update(eq(2L), any())).thenReturn(code());
        when(service.getById(2L)).thenReturn(code());

        mockMvc.perform(get("/api/common-codes/id/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("PUBLIC"));
        mockMvc.perform(post("/api/common-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parentId":1,"code":"PUBLIC","name":"공영","sortOrder":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("PUBLIC"));
        mockMvc.perform(put("/api/common-codes/id/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"PUBLIC","name":"공영","sortOrder":1,"state":"ACTIVE"}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/common-codes/id/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        verify(service).delete(2L);
    }

    @Test
    void validatesInvalidCreateBody() throws Exception {
        mockMvc.perform(post("/api/common-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parentId":0,"code":"lowercase","name":"","sortOrder":-1,"state":"DELETED"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_002"));
    }

    @Test
    void mapsNotFoundTo404() throws Exception {
        when(service.getChildren("UNKNOWN", false)).thenThrow(new BusinessException(EnCmmErrCode.NOT_FOUND));
        mockMvc.perform(get("/api/common-codes/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMON_404_001"));
    }
}
