package com.school.controller;

import com.school.entity.Interaction;
import com.school.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/interaction")
@CrossOrigin
public class InteractionController {
    @Autowired private InteractionService interactionService;

    @GetMapping("/list") public List<Interaction> list() { return interactionService.list(); }
    @PostMapping("/ask") public boolean ask(@RequestBody Interaction interaction) {
        interaction.setAsktime(LocalDateTime.now());
        return interactionService.save(interaction);
    }
    @PostMapping("/reply") public boolean reply(@RequestBody Interaction interaction) {
        Interaction existing = interactionService.getById(interaction.getId());
        if (existing != null) {
            existing.setReplname(interaction.getReplname());
            existing.setComrepl(interaction.getComrepl());
            existing.setRepltime(LocalDateTime.now());
            return interactionService.updateById(existing);
        }
        return false;
    }
    @DeleteMapping("/delete/{id}") public boolean delete(@PathVariable Integer id) { return interactionService.removeById(id); }
}
