package com.kimdevops.portfolio.service;

import com.kimdevops.portfolio.entity.DevNote;
import com.kimdevops.portfolio.repository.DevNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * DevNoteService — 모놀리스(K-portfolio)의 DevNote CRUD 서비스.
 *
 * <p><b>Strangler Fig 전환 상태:</b> Phase 4에서 blog-service로 분리 예정</p>
 * <p>이 클래스는 MSA 전환 중 잠시 공존하는 레거시 코드.
 * 게이트웨이가 해당 도메인의 라우트를 신규 서비스로 전환하면 이 코드는 사용되지 않는다.
 * 그때까지는 monolith가 fallback 역할을 수행한다.</p>
 *
 * @FROM  K-portfolio 원본 서비스 (Phase 0 흡수)
 * @RISK  이 서비스를 수정하면 신규 서비스와 로직이 달라질 수 있음 — 분리 완료 후 삭제 대상
 */
@Service
public class DevNoteService {
    @Autowired
    private DevNoteRepository devNoteRepository;

    @Transactional(readOnly = true)
    public List<DevNote> getAll() {
        return devNoteRepository.findAllByOrderByDisplayOrderAscIdAsc();
    }

    @Transactional
    public DevNote create(DevNote input) {
        DevNote note = new DevNote();
        apply(note, input);
        return devNoteRepository.save(note);
    }

    @Transactional
    public DevNote update(Long id, DevNote input) {
        DevNote note = devNoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DevNote not found: " + id));
        apply(note, input);
        return devNoteRepository.save(note);
    }

    @Transactional
    public void delete(Long id) {
        devNoteRepository.deleteById(id);
    }

    private void apply(DevNote note, DevNote input) {
        note.setTitle(input.getTitle());
        note.setCategory(input.getCategory());
        note.setSituation(input.getSituation());
        note.setCodeBefore(input.getCodeBefore());
        note.setCodeAfter(input.getCodeAfter());
        note.setSolution(input.getSolution());
        note.setDisplayOrder(input.getDisplayOrder() != null ? input.getDisplayOrder() : 0);
    }
}
