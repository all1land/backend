package com.all4land.parkinglotnavigator.domain.commoncode.controller;

import com.all4land.parkinglotnavigator.domain.commoncode.dto.CommonCodeCreateReqDTO;
import com.all4land.parkinglotnavigator.domain.commoncode.dto.CommonCodeResDTO;
import com.all4land.parkinglotnavigator.domain.commoncode.dto.CommonCodeUpdateReqDTO;
import com.all4land.parkinglotnavigator.domain.commoncode.service.CommonCodeService;
import com.all4land.parkinglotnavigator.global.response.ApiResDTO;
import com.all4land.parkinglotnavigator.global.response.EnCmmScssCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Common Code", description = "공통코드 CRUD 및 root/하위 코드 목록 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common-codes")
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    @Operation(summary = "최상위(root) 코드 목록", description = "parent_id가 NULL인 코드 목록. 기본적으로 ACTIVE만 조회한다.")
    @GetMapping("/roots")
    public ApiResDTO<List<CommonCodeResDTO>> getRoots(
            @Parameter(description = "비활성 코드를 포함할지 여부") @RequestParam(name = "includeInactive", defaultValue = "false")
                    boolean includeInactive) {
        return ApiResDTO.success(EnCmmScssCode.OK, commonCodeService.getRoots(includeInactive));
    }

    @Operation(
            summary = "root 지정 시 하위 코드 목록",
            description = "예: GET /api/common-codes/PARKING_CATEGORY. 직계 하위 코드를 정렬 순서로 조회한다.")
    @GetMapping("/{rootCode}")
    public ApiResDTO<List<CommonCodeResDTO>> getChildren(
            @Parameter(description = "최상위 코드값 (예: PARKING_TYPE)") @PathVariable("rootCode") String rootCode,
            @Parameter(description = "비활성 코드를 포함할지 여부") @RequestParam(name = "includeInactive", defaultValue = "false")
                    boolean includeInactive) {
        return ApiResDTO.success(EnCmmScssCode.OK, commonCodeService.getChildren(rootCode, includeInactive));
    }

    @Operation(summary = "공통코드 상세 조회")
    @GetMapping("/id/{id}")
    public ApiResDTO<CommonCodeResDTO> getById(@PathVariable("id") @Positive Long id) {
        return ApiResDTO.success(EnCmmScssCode.OK, commonCodeService.getById(id));
    }

    @Operation(summary = "공통코드 등록", description = "parentId 생략 시 root 등록. level은 자동 계산한다.")
    @PostMapping
    public ApiResDTO<CommonCodeResDTO> create(@Valid @RequestBody CommonCodeCreateReqDTO request) {
        return ApiResDTO.success(EnCmmScssCode.OK, commonCodeService.create(request));
    }

    @Operation(summary = "공통코드 수정", description = "parentId 변경은 지원하지 않는다. 사용 중인 코드값 수정은 제한된다.")
    @PutMapping("/id/{id}")
    public ApiResDTO<CommonCodeResDTO> update(
            @PathVariable("id") @Positive Long id, @Valid @RequestBody CommonCodeUpdateReqDTO request) {
        return ApiResDTO.success(EnCmmScssCode.OK, commonCodeService.update(id, request));
    }

    @Operation(summary = "공통코드 삭제", description = "하위 코드 또는 데이터 참조가 있으면 409 응답한다.")
    @DeleteMapping("/id/{id}")
    public ApiResDTO<Void> delete(@PathVariable("id") @Positive Long id) {
        commonCodeService.delete(id);
        return ApiResDTO.success(EnCmmScssCode.OK);
    }
}
