package ua.kiev.prog.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.kiev.prog.bot.ChatBot;
import ua.kiev.prog.model.User;
import ua.kiev.prog.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class MyController {

    private static final Logger LOGGER = LogManager.getLogger(ChatBot.class);

    private final UserService userService;
    private final ChatBot chatBot;

    public MyController (UserService userService, ChatBot chatBot) {
        this.userService = userService;
        this.chatBot = chatBot;
    }

    static final Map<String, String> accounts = new ConcurrentHashMap<>();

    static {
        accounts.put("admin", "admin");
        accounts.put("qqq", "qqq");
    }

    @GetMapping("/")
    public String onIndex() {
        return "index";
    }

    @PostMapping("/login")
    public String onLogin(Model model,
                          @RequestParam String login,
                          @RequestParam String password) {
        String pass = accounts.get(login);

        model.addAttribute("login", login);
        model.addAttribute("message",
                password.equals(pass) ? "Login success" : "User not found");
        
        return password.equals(pass) ? "result" : "index";
    }

    @PostMapping("/sendMessage")
    public String onSend(Model model,@RequestParam("photo") MultipartFile file, @RequestParam String textMessage) {
        LOGGER.info("Admin command received: " + "/sendMessage");
        broadcastMessage (textMessage);
        broadcastFile (file);
        model.addAttribute("message", "message sent");
        return "result";
    }

    private void broadcastFile(MultipartFile file) {
        List<User> users = userService.findAllUsers();
        users.forEach(user -> {
            try {
                sendFile(user.getChatId(), file);
            } catch (IOException e) {
                e.printStackTrace ();
            }
        });
    }

    private void sendFile(long chatId, MultipartFile file) throws IOException {

        SendPhoto photo = new SendPhoto ()
                .setChatId(chatId)
                .setPhoto ("photo",file.getInputStream ());
        try {
            chatBot.execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void broadcastMessage(String text) {
        List<User> users = userService.findAllUsers();
        users.forEach(user -> sendMessage(user.getChatId(), text));
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage()
                .setChatId(chatId)
                .setText(text);
        try {
            chatBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }



}
