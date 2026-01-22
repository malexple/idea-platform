package com.company.ideaplatform.controller;

import com.company.ideaplatform.config.CurrentUser;
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
import org.springframework.security.core.userdetails.UserDetails;
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

    @GetMapping
    public String listIdeas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) IdeaType type,
            @RequestParam(required = false) IdeaStatus status,
            @CurrentUser UserDetails userDetails,
            Model model) {

        Long currentUserId = getCurrentUserId(userDetails);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<IdeaViewDto> ideas;
        if (status != null) {
            ideas = ideaService.getIdeasByStatus(status, pageRequest, currentUserId);
        } else if (type != null) {
            ideas = ideaService.getIdeasByType(type, pageRequest, currentUserId);
        } else {
            ideas = ideaService.getIdeasForVoting(pageRequest, currentUserId);
        }

        model.addAttribute("ideas", ideas);
        model.addAttribute("types", IdeaType.values());
        model.addAttribute("statuses", IdeaStatus.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStatus", status);

        return "ideas/list";
    }

    @GetMapping("/{number}")
    public String viewIdea(
            @PathVariable String number,
            @CurrentUser UserDetails userDetails,
            Model model) {

        Long currentUserId = getCurrentUserId(userDetails);
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
    public String createIdea(
            @Valid @ModelAttribute("ideaForm") IdeaCreateDto ideaForm,
            BindingResult bindingResult,
            @CurrentUser UserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("types", IdeaType.values());
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("divisions", divisionService.getAllActive());
            return "ideas/form";
        }

        try {
            User author = userService.findByEmail(userDetails.getUsername());
            var idea = ideaService.createIdea(ideaForm, author);

            redirectAttributes.addFlashAttribute("success",
                    "Заявка " + idea.getNumber() + " успешно создана!");

            return "redirect:/ideas/" + idea.getNumber();

        } catch (IllegalArgumentException e) {
            // Ошибка валидации файлов или данных
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ideas/new";
        } catch (Exception e) {
            log.error("Error creating idea", e);
            redirectAttributes.addFlashAttribute("error", "Произошла ошибка при создании заявки. Попробуйте ещё раз.");
            return "redirect:/ideas/new";
        }
    }

    @PostMapping("/{number}/vote")
    public String vote(
            @PathVariable String number,
            @RequestParam VoteType voteType,
            @CurrentUser UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByEmail(userDetails.getUsername());
        ideaService.vote(number, user, voteType);

        redirectAttributes.addFlashAttribute("success", "Ваш голос учтён!");

        return "redirect:/ideas/" + number;
    }

    @PostMapping("/{number}/vote/remove")
    public String removeVote(
            @PathVariable String number,
            @CurrentUser UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByEmail(userDetails.getUsername());
        ideaService.removeVote(number, user);

        redirectAttributes.addFlashAttribute("info", "Голос отменён");

        return "redirect:/ideas/" + number;
    }

    @PostMapping("/{number}/comments")
    public String addComment(
            @PathVariable String number,
            @Valid @ModelAttribute("commentForm") CommentCreateDto commentForm,
            BindingResult bindingResult,
            @CurrentUser UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Комментарий не может быть пустым");
            return "redirect:/ideas/" + number;
        }

        User author = userService.findByEmail(userDetails.getUsername());
        commentService.addComment(number, commentForm, author);

        redirectAttributes.addFlashAttribute("success", "Комментарий добавлен");

        return "redirect:/ideas/" + number;
    }

    @GetMapping("/my")
    public String myIdeas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @CurrentUser UserDetails userDetails,
            Model model) {

        User user = userService.findByEmail(userDetails.getUsername());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<IdeaViewDto> ideas = ideaService.getMyIdeas(user.getId(), pageRequest);
        model.addAttribute("ideas", ideas);

        return "ideas/my";
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userService.findByEmail(userDetails.getUsername()).getId();
    }
}
