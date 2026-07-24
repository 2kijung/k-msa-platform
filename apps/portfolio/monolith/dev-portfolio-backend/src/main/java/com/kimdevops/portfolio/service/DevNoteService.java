package com.kimdevops.portfolio.service;

import com.kimdevops.portfolio.entity.DevNote;
import com.kimdevops.portfolio.repository.DevNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
