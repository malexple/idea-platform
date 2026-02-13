package com.company.ideaplatform.controller;

import com.company.ideaplatform.config.AuthHelper;
import com.company.ideaplatform.dto.*;
import com.company.ideaplatform.entity.User;
import com.company.ideaplatform.entity.enums.IdeaStatus;
import com.company.ideaplatform.entity.enums.IdeaType;
import com.company.ideaplatform.entity.enums.Priority;
import com.company.ideaplatform.entity.enums.VoteType;
import com.company.ideaplatform.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/ideas")
@RequiredArgsConstructor
public class IdeaController {

    private final IdeaService ideaService;
    private final UserService userService;
    private final CommentService commentService;
    private final DivisionService divisionService;
    private final AuthHelper authHelper;

    @GetMapping
    public String listIdeas(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) IdeaType type,
                            @RequestParam(required = false) IdeaStatus status,
                            Model model) {
        Long currentUserId = authHelper.getCurrentUserId();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<IdeaViewDto> ideas;
        if (status != null) {
            ideas = ideaService.getIdeasByStatus(status, pageRequest, currentUserId);
        } else if (type != null) {
            ideas = ideaService.getIdeasByType(type, pageRequest, currentUserId);
        } else {
            ideas = ideaService.getAllIdeas(pageRequest, currentUserId);
        }

        model.addAttribute("ideas", ideas);
        model.addAttribute("types", IdeaType.values());
        model.addAttribute("statuses", IdeaStatus.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStatus", status);
        return "ideas/list";
    }


    @GetMapping("/{number}")
    public String viewIdea(@PathVariable String number, Model model) {
        Long currentUserId = authHelper.getCurrentUserId();
        IdeaViewDto idea = ideaService.getIdeaByNumber(number, currentUserId);
        model.addAttribute("idea", idea);
        model.addAttribute("voteTypes", VoteType.values());
        model.addAttribute("commentForm", new CommentCreateDto());
        return "ideas/view";
    }

    @GetMapping("/new")
    public String newIdeaForm(Model model) {
        model.addAttribute("ideaForm", new IdeaCreateDto());
        model.addAttribute("types", IdeaType.values());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("divisions", divisionService.getAllActive());
        return "ideas/form";
    }

    @PostMapping("/new")
    public String createIdea(@Valid @ModelAttribute("ideaForm") IdeaCreateDto ideaForm,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        log.info("=== createIdea called, hasErrors={} ===", bindingResult.hasErrors());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("types", IdeaType.values());
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("divisions", divisionService.getAllActive());
            return "ideas/form";
        }

        User author = authHelper.getCurrentUser();
        log.info("=== author={} ===", author != null ? author.getEmail() : "NULL");

        if (author == null) {
            return "redirect:/login";
        }

        ideaService.createIdea(ideaForm, author);
        redirectAttributes.addFlashAttribute("success", "Идея подана!");
        return "redirect:/ideas";
    }

    @PostMapping("/{number}/vote")
    public String vote(@PathVariable String number,
                       @RequestParam VoteType type,
                       RedirectAttributes redirectAttributes) {
        User user = authHelper.getCurrentUser();
        if (user == null) return "redirect:/login";
        ideaService.vote(number, user, type);
        redirectAttributes.addFlashAttribute("success", "Голос учтён!");
        return "redirect:/ideas/" + number;
    }

    @PostMapping("/{number}/unvote")
    public String unvote(@PathVariable String number,
                         RedirectAttributes redirectAttributes) {
        User user = authHelper.getCurrentUser();
        if (user == null) return "redirect:/login";
        ideaService.removeVote(number, user);
        redirectAttributes.addFlashAttribute("info", "Голос отменён");
        return "redirect:/ideas/" + number;
    }

    @PostMapping("/{number}/comments")
    public String addComment(@PathVariable String number,
                             @Valid @ModelAttribute("commentForm") CommentCreateDto commentForm,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Комментарий не может быть пустым");
            return "redirect:/ideas/" + number;
        }
        User author = authHelper.getCurrentUser();
        if (author == null) return "redirect:/login";
        commentService.addComment(number, commentForm, author);
        redirectAttributes.addFlashAttribute("success", "Комментарий добавлен");
        return "redirect:/ideas/" + number;
    }

    @GetMapping("/my")
    public String myIdeas(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {
        User user = authHelper.getCurrentUser();
        if (user == null) return "redirect:/login";
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<IdeaViewDto> ideas = ideaService.getMyIdeas(user.getId(), pageRequest);
        model.addAttribute("ideas", ideas);
        return "ideas/my";
    }
}
