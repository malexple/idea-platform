package com.company.ideaplatform.service;

import com.company.ideaplatform.entity.Division;
import com.company.ideaplatform.entity.Team;
import com.company.ideaplatform.entity.Tribe;
import com.company.ideaplatform.repository.DivisionRepository;
import com.company.ideaplatform.repository.TeamRepository;
import com.company.ideaplatform.repository.TribeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DivisionService {

    private final DivisionRepository divisionRepository;
    private final TribeRepository tribeRepository;
    private final TeamRepository teamRepository;

    public List<Division> getAllDivisions() {
        return divisionRepository.findAll();
    }

    public List<Division> getAllActive() {
        return divisionRepository.findByActiveTrue();
    }

    public List<Tribe> getAllTribes() {
        return tribeRepository.findAll();
    }

    public List<Tribe> getTribesByDivision(Long divisionId) {
        return tribeRepository.findByDivisionIdAndActiveTrue(divisionId);
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public List<Team> getTeamsByTribe(Long tribeId) {
        return teamRepository.findByTribeIdAndActiveTrue(tribeId);
    }

    @Transactional
    public Division createDivision(Division division) {
        return divisionRepository.save(division);
    }

    @Transactional
    public Tribe createTribe(Tribe tribe, Long divisionId) {
        Division division = divisionRepository.findById(divisionId)
                .orElseThrow(() -> new IllegalArgumentException("Дивизион не найден"));
        tribe.setDivision(division);
        return tribeRepository.save(tribe);
    }

    @Transactional
    public Team createTeam(Team team, Long tribeId) {
        Tribe tribe = tribeRepository.findById(tribeId)
                .orElseThrow(() -> new IllegalArgumentException("Трайб не найден"));
        team.setTribe(tribe);
        return teamRepository.save(team);
    }

    @Transactional
    public void toggleActive(Long divisionId) {
        Division division = divisionRepository.findById(divisionId)
                .orElseThrow(() -> new IllegalArgumentException("Дивизион не найден"));
        division.setActive(!division.getActive());
        divisionRepository.save(division);
    }
}
