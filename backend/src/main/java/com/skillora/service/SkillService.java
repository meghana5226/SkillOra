package com.skillora.service;

import com.skillora.dto.skill.SkillRequest;
import com.skillora.dto.skill.SkillResponse;
import com.skillora.entity.Skill;
import com.skillora.exception.ConflictException;
import com.skillora.exception.ResourceNotFoundException;
import com.skillora.mapper.UserMapper;
import com.skillora.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    @Transactional(readOnly = true)
    public List<SkillResponse> listAll() {
        return skillRepository.findAll().stream().map(UserMapper::toSkillResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> byCategory(String category) {
        return skillRepository.findByCategoryIgnoreCase(category).stream()
                .map(UserMapper::toSkillResponse).toList();
    }

    @Transactional
    public SkillResponse create(SkillRequest request) {
        if (skillRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("A skill with this name already exists");
        }
        Skill skill = Skill.builder()
                .name(request.getName().trim())
                .category(request.getCategory().trim())
                .description(request.getDescription())
                .build();
        return UserMapper.toSkillResponse(skillRepository.save(skill));
    }

    @Transactional
    public SkillResponse update(Long id, SkillRequest request) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
        skill.setName(request.getName().trim());
        skill.setCategory(request.getCategory().trim());
        skill.setDescription(request.getDescription());
        return UserMapper.toSkillResponse(skillRepository.save(skill));
    }

    @Transactional
    public void delete(Long id) {
        if (!skillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Skill not found");
        }
        skillRepository.deleteById(id);
    }
}
