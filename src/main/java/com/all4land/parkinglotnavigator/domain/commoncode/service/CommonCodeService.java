package com.all4land.parkinglotnavigator.domain.commoncode.service;

import com.all4land.parkinglotnavigator.domain.commoncode.dto.CommonCodeCreateReqDTO;
import com.all4land.parkinglotnavigator.domain.commoncode.dto.CommonCodeResDTO;
import com.all4land.parkinglotnavigator.domain.commoncode.dto.CommonCodeUpdateReqDTO;
import com.all4land.parkinglotnavigator.domain.commoncode.repository.CommonCodeRepository;
import com.all4land.parkinglotnavigator.global.exception.BusinessException;
import com.all4land.parkinglotnavigator.global.response.EnCmmErrCode;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommonCodeService {

    private static final Set<String> RESERVED_ROOT_CODES =
            Set.of("PLACE_CATEGORY", "PARKING_CATEGORY", "PARKING_TYPE", "USER_ROLE");

    private final CommonCodeRepository commonCodeRepository;

    @Transactional(readOnly = true)
    public List<CommonCodeResDTO> getRoots(boolean includeInactive) {
        return commonCodeRepository.findRoots(includeInactive);
    }

    @Transactional(readOnly = true)
    public List<CommonCodeResDTO> getChildren(String rootCode, boolean includeInactive) {
        CommonCodeResDTO root = commonCodeRepository
                .findRootByCode(rootCode)
                .orElseThrow(() -> new BusinessException(EnCmmErrCode.NOT_FOUND, "최상위 공통코드를 찾을 수 없습니다."));
        if (!includeInactive && !"ACTIVE".equals(root.state())) {
            throw new BusinessException(EnCmmErrCode.NOT_FOUND, "사용 가능한 최상위 공통코드가 아닙니다.");
        }
        return commonCodeRepository.findChildren(root.id(), includeInactive);
    }

    @Transactional(readOnly = true)
    public CommonCodeResDTO getById(Long id) {
        return commonCodeRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(EnCmmErrCode.NOT_FOUND, "공통코드를 찾을 수 없습니다."));
    }

    @Transactional
    public CommonCodeResDTO create(CommonCodeCreateReqDTO request) {
        int level = 1;
        if (request.parentId() != null) {
            CommonCodeResDTO parent = getById(request.parentId());
            level = parent.level() + 1;
        }
        if (commonCodeRepository.existsByParentAndCode(request.parentId(), request.code(), null)) {
            throw new BusinessException(EnCmmErrCode.CONFLICT, "같은 부모 아래 동일한 코드가 존재합니다.");
        }
        try {
            Long id = commonCodeRepository.insert(request, level);
            return getById(id);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(EnCmmErrCode.CONFLICT, "공통코드 등록 중 제약조건 위반이 발생했습니다.");
        }
    }

    @Transactional
    public CommonCodeResDTO update(Long id, CommonCodeUpdateReqDTO request) {
        CommonCodeResDTO current = getById(id);
        if (!Objects.equals(current.code(), request.code())) {
            ensureCodeChangeAllowed(current);
        }
        if ("ACTIVE".equals(current.state())
                && "INACTIVE".equals(request.state())
                && (isReservedRoot(current) || isReferenced(current))) {
            throw new BusinessException(EnCmmErrCode.CONFLICT, "현재 사용 중인 공통코드는 비활성화할 수 없습니다.");
        }
        if (commonCodeRepository.existsByParentAndCode(current.parentId(), request.code(), id)) {
            throw new BusinessException(EnCmmErrCode.CONFLICT, "같은 부모 아래 동일한 코드가 존재합니다.");
        }
        try {
            commonCodeRepository.update(id, request);
            return getById(id);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(EnCmmErrCode.CONFLICT, "공통코드 수정 중 제약조건 위반이 발생했습니다.");
        }
    }

    @Transactional
    public void delete(Long id) {
        CommonCodeResDTO current = getById(id);
        if (commonCodeRepository.hasChildren(id)) {
            throw new BusinessException(EnCmmErrCode.CONFLICT, "하위 코드가 있으므로 삭제할 수 없습니다.");
        }
        if (isReservedRoot(current) || isReferenced(current)) {
            throw new BusinessException(EnCmmErrCode.CONFLICT, "서비스에서 사용 중인 공통코드는 삭제할 수 없습니다.");
        }
        try {
            commonCodeRepository.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(EnCmmErrCode.CONFLICT, "참조 중인 공통코드는 삭제할 수 없습니다.");
        }
    }

    private void ensureCodeChangeAllowed(CommonCodeResDTO code) {
        if (isReservedRoot(code) || commonCodeRepository.hasChildren(code.id()) || isReferenced(code)) {
            throw new BusinessException(EnCmmErrCode.CONFLICT, "서비스 또는 하위 코드에서 사용하는 코드값은 변경할 수 없습니다.");
        }
    }

    private boolean isReservedRoot(CommonCodeResDTO code) {
        return code.parentId() == null && RESERVED_ROOT_CODES.contains(code.code());
    }

    private boolean isReferenced(CommonCodeResDTO code) {
        if (code.parentId() == null) {
            return false;
        }
        CommonCodeResDTO parent = getById(code.parentId());
        if (parent.parentId() != null) {
            return false;
        }
        return commonCodeRepository.isReferenced(parent.code(), code.code());
    }
}
