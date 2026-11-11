package com.all4land.parkinglotnavigator.domain.commoncode.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.all4land.parkinglotnavigator.domain.commoncode.dto.CommonCodeCreateReqDTO;
import com.all4land.parkinglotnavigator.domain.commoncode.dto.CommonCodeResDTO;
import com.all4land.parkinglotnavigator.domain.commoncode.dto.CommonCodeUpdateReqDTO;
import com.all4land.parkinglotnavigator.domain.commoncode.repository.CommonCodeRepository;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.EnCmmErrCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommonCodeServiceTest {

    @Mock
    private CommonCodeRepository repository;

    @InjectMocks
    private CommonCodeService service;

    private static CommonCodeResDTO code(Long id, Long parentId, String code, int level) {
        return new CommonCodeResDTO(
                id, parentId, code, code, level, 1, null, "ACTIVE", LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void getsChildrenForSpecifiedRoot() {
        var root = code(1L, null, "PARKING_CATEGORY", 1);
        var child = code(2L, 1L, "PUBLIC", 2);
        when(repository.findRootByCode("PARKING_CATEGORY")).thenReturn(Optional.of(root));
        when(repository.findChildren(1L, false)).thenReturn(List.of(child));

        assertEquals(List.of(child), service.getChildren("PARKING_CATEGORY", false));
    }

    @Test
    void createsChildWithCalculatedLevel() {
        var root = code(1L, null, "PARKING_CATEGORY", 1);
        var child = code(2L, 1L, "NEW_CODE", 2);
        var req = new CommonCodeCreateReqDTO(1L, "NEW_CODE", "새 코드", 2, null, "ACTIVE");
        when(repository.findById(1L)).thenReturn(Optional.of(root));
        when(repository.insert(req, 2)).thenReturn(2L);
        when(repository.findById(2L)).thenReturn(Optional.of(child));

        assertEquals(2, service.create(req).level());
        verify(repository).insert(req, 2);
    }

    @Test
    void rejectsDuplicateCodeWithinSameParent() {
        var root = code(1L, null, "PARKING_CATEGORY", 1);
        var req = new CommonCodeCreateReqDTO(1L, "PUBLIC", "공영", 1, null, "ACTIVE");
        when(repository.findById(1L)).thenReturn(Optional.of(root));
        when(repository.existsByParentAndCode(1L, "PUBLIC", null)).thenReturn(true);

        BusinessException error = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals(EnCmmErrCode.CONFLICT, error.getErrorCode());
        verify(repository, never()).insert(any(), eq(2));
    }

    @Test
    void rejectsDeletionOfParentWithChildren() {
        when(repository.findById(1L)).thenReturn(Optional.of(code(1L, null, "PARKING_CATEGORY", 1)));
        when(repository.hasChildren(1L)).thenReturn(true);

        BusinessException error = assertThrows(BusinessException.class, () -> service.delete(1L));
        assertEquals(EnCmmErrCode.CONFLICT, error.getErrorCode());
        verify(repository, never()).delete(1L);
    }

    @Test
    void rejectsDeactivatingReferencedChildCode() {
        var child = code(2L, 1L, "PUBLIC", 2);
        var root = code(1L, null, "PARKING_CATEGORY", 1);
        when(repository.findById(2L)).thenReturn(Optional.of(child));
        when(repository.findById(1L)).thenReturn(Optional.of(root));
        when(repository.isReferenced("PARKING_CATEGORY", "PUBLIC")).thenReturn(true);
        var req = new CommonCodeUpdateReqDTO("PUBLIC", "공영", 1, null, "INACTIVE");

        BusinessException error = assertThrows(BusinessException.class, () -> service.update(2L, req));
        assertEquals(EnCmmErrCode.CONFLICT, error.getErrorCode());
        verify(repository, never()).update(eq(2L), any());
    }

    @Test
    void rejectsRenamingReferencedChildCode() {
        var child = code(2L, 1L, "PUBLIC", 2);
        var root = code(1L, null, "PARKING_CATEGORY", 1);
        when(repository.findById(2L)).thenReturn(Optional.of(child));
        when(repository.findById(1L)).thenReturn(Optional.of(root));
        when(repository.isReferenced("PARKING_CATEGORY", "PUBLIC")).thenReturn(true);
        var req = new CommonCodeUpdateReqDTO("PUBLIC_NEW", "공영 주차장", 1, null, "ACTIVE");

        BusinessException error = assertThrows(BusinessException.class, () -> service.update(2L, req));
        assertEquals(EnCmmErrCode.CONFLICT, error.getErrorCode());
        verify(repository, never()).update(eq(2L), any());
    }
}
