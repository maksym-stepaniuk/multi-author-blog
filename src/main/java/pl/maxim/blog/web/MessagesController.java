package pl.maxim.blog.web;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.maxim.blog.message.ConversationService;
import pl.maxim.blog.message.MessageService;
import pl.maxim.blog.message.dto.SendMessageRequest;
import pl.maxim.blog.security.CurrentUserService;
import pl.maxim.blog.user.UserService;
import pl.maxim.blog.web.forms.MessageForm;

@Controller
@RequestMapping("/messages")
public class MessagesController {

    private final MessageService messageService;
    private final ConversationService conversationService;
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public MessagesController(MessageService messageService, ConversationService conversationService, UserService userService, CurrentUserService currentUserService) {
        this.messageService = messageService;
        this.conversationService = conversationService;
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/inbox")
    public String inbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("messages", messageService.inbox(pageable));
        model.addAttribute("size", size);
        model.addAttribute("tab", "inbox");
        return "messages_list";
    }

    @GetMapping("/sent")
    public String sent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("messages", messageService.sent(pageable));
        model.addAttribute("size", size);
        model.addAttribute("tab", "sent");
        return "messages_list";
    }

    @GetMapping("/new")
    public String newMessage(Model model) {
        MessageForm form = new MessageForm();
        model.addAttribute("messageForm", form);
        model.addAttribute("users", userService.search(null, PageRequest.of(0, 200, Sort.by("username"))).getContent());
        return "message_new";
    }

    @PostMapping
    public String send(
            @Valid @ModelAttribute("messageForm") MessageForm form,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) {
            model.addAttribute("users", userService.search(null, PageRequest.of(0, 200, Sort.by("username"))).getContent());
            return "message_new";
        }

        messageService.send(new SendMessageRequest(form.getRecipientId(), form.getContent()));
        return "redirect:/messages/sent";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @RequestParam(defaultValue = "inbox") String back
    ) {
        messageService.delete(id);
        if ("sent".equalsIgnoreCase(back)) return "redirect:/messages/sent";
        return "redirect:/messages/inbox";
    }

    @GetMapping("/with/{userId}")
    public String withUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        Long meId = currentUserService.requireUserId();
        Pageable pageable = PageRequest.of(page, size);

        model.addAttribute("meId", meId);
        model.addAttribute("otherUserId", userId);
        model.addAttribute("otherUser", userService.search(null, PageRequest.of(0, 200, Sort.by("username")))
                .getContent().stream().filter(u -> u.id().equals(userId)).findFirst().orElse(null));

        model.addAttribute("chat", conversationService.conversation(meId, userId, pageable));
        MessageForm form = new MessageForm();
        form.setRecipientId(userId);
        model.addAttribute("messageForm", form);

        return "conversation";
    }

    @PostMapping("/with/{userId}")
    public String sendToUser(
            @PathVariable Long userId,
            @Valid @ModelAttribute("messageForm") MessageForm form,
            BindingResult br,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        if (br.hasErrors()) {
            Long meId = currentUserService.requireUserId();
            Pageable pageable = PageRequest.of(page, size);

            model.addAttribute("meId", meId);
            model.addAttribute("otherUserId", userId);
            model.addAttribute("chat", conversationService.conversation(meId, userId, pageable));
            return "conversation";
        }

        messageService.send(new SendMessageRequest(userId, form.getContent()));
        return "redirect:/messages/with/" + userId;
    }
}
