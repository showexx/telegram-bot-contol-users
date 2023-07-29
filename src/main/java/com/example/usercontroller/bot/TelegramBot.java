package com.example.usercontroller.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${bot.name}")
    private String botName;

    @Value("${bot.channel1}")
    private String channel1;
    @Value("${bot.url1}")
    private String url1;

    @Value("${bot.channel2}")
    private String channel2;
    @Value("${bot.url2}")
    private String utl2;

    public TelegramBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleMessage(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            switch (text) {
                case "/start":
                    sendInstruction(message.getChatId());
                    break;
            }
        }
    }

    private void sendInstruction(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Привет! Для получения доступа, ты должен подписаться на эти два канала: ");
        message.setReplyMarkup(createInlineKeyboard());

        exception(message);
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        if (callbackData.equals("checkSubscription")) {
            checkSubscriptionStatus(callbackQuery.getMessage().getChatId(), callbackQuery.getFrom().getId());
        }
    }

    private void checkSubscriptionStatus(long chatId, long userId) {
        String channel = ""; // TODO

        boolean isUserInChannel1 = isUserInChannel(channel1, userId);
        boolean isUserInChannel2 = isUserInChannel(channel2, userId);

        if (isUserInChannel1 && isUserInChannel2) {
            sendMessage(chatId, "Канал: " + channel);
        } else {
            sendMessage(chatId, "Проверьте подписку!");
        }
    }

    private boolean isUserInChannel(String channel, long userId) {
        List<String> allowedStatuses = Arrays.asList("member", "administrator", "creator");
        GetChatMember getChatMember = new GetChatMember(channel, userId);

        try {
            ChatMember chatMember = execute(getChatMember);
            return allowedStatuses.contains(chatMember.getStatus());
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false;
        }
    }

    private InlineKeyboardMarkup createInlineKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("Канал 1", url1, null));
        row1.add(createButton("Канал 2", utl2, null));
        keyboard.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("Проверить подписку", null, "checkSubscription"));
        keyboard.add(row2);

        markup.setKeyboard(keyboard);

        return markup;
    }

    private InlineKeyboardButton createButton(String message, String url, String callBackData) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(message);
        inlineKeyboardButton.setUrl(url);
        inlineKeyboardButton.setCallbackData(callBackData);
        return inlineKeyboardButton;
    }

    private void sendMessage(long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        exception(message);
    }

    private void exception(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}
