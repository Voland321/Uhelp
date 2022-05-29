package com.tehcman.handlers;

import com.tehcman.cahce.Cache;
import com.tehcman.cahce.UserCache;
import com.tehcman.entities.User;
import com.tehcman.informational.portal.IListOfNewsChannels;
import com.tehcman.informational.portal.ListOfNewsChannels;
import com.tehcman.sendmessage.MessageSender;
import com.tehcman.services.BuildButtonsService;
import com.tehcman.services.BuildInlineButtonsService;
import com.tehcman.services.BuildSendMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class TextHandler implements Handler<Message> {
    private final MessageSender messageSender;
    private final BuildSendMessageService buildSendMessageService;
    private final BuildInlineButtonsService buildInlineButtonsService; //testing the inline buttons
    private final BuildButtonsService buildButtonsService;
    private IListOfNewsChannels iListOfNewsChannels;

    private final Cache<User> userCache;

    @Autowired
    public TextHandler(@Lazy MessageSender messageSender, BuildSendMessageService buildSendMessageService, BuildInlineButtonsService buildInlineButtonsService, @Lazy BuildButtonsService buildButtonsService, UserCache userCache) {
        this.messageSender = messageSender;
        this.buildSendMessageService = buildSendMessageService;
        this.buildInlineButtonsService = buildInlineButtonsService;
        this.buildButtonsService = buildButtonsService;
        this.userCache = userCache;
        this.iListOfNewsChannels = new ListOfNewsChannels();
    }

    @Override
    public void handle(Message message) {
        if (message.getText().equals("/start")) {
            buildButtonsService.beforeRegistrationButtons();
            messageSender.messageSend(buildSendMessageService.createHTMLMessage(message.getChatId().toString(), "Yay! You've just launched this bot!", buildButtonsService.getMainMarkup()));
        } else if (message.getText().equals("I want a joke")) {
            var sendMessage = SendMessage.builder()
                    .text("Are you ready for my collection of the most hilarious jokes??\nIf so, press the button below!")
                    .chatId(message.getChatId().toString())
                    .build();
            sendMessage.setReplyMarkup(buildInlineButtonsService.build());
            messageSender.messageSend(sendMessage);
        } else if (message.getText().equals("You're dumb")) {
            messageSender.messageSend(buildSendMessageService.createHTMLMessage(message.getChatId().toString(), "no, you're dumb!", buildButtonsService.getMainMarkup()));
        } else if (message.getText().equals("View my data")) {
            User userFromCache = userCache.findBy(message.getChatId());
            messageSender.messageSend(buildSendMessageService.createHTMLMessage(message.getChatId().toString(), userFromCache.toString(), buildButtonsService.getMainMarkup()));
        } else if (message.getText().equals("Remove my data")) {
            buildButtonsService.beforeRegistrationButtons();
            userCache.remove(message.getChatId());
            messageSender.messageSend(buildSendMessageService.createHTMLMessage(message.getChatId().toString(), "All data about you has been removed", buildButtonsService.getMainMarkup()));
        } else if (message.getText().equals("List of TG news channels on Ukraine (ENG)")) {
//            var sendMsg = new SendMessage(message.getChatId().toString(), listOfNewsChannels.getChannels()/*"List of trustable News resources:\n\n"+ "https://t.me/nytimes\n\n " +
//                    "https://t.me/washingtonpost\n\n" + "https://t.me/financialtimes\n\n" + "https://t.me/KyivIndependent_official\n"*/ );
            messageSender.messageSend(buildSendMessageService.createHTMLMessage(message.getChatId().toString(), iListOfNewsChannels.getListDescription(), buildButtonsService.getMainMarkup()));
            String formattedString = String.valueOf(iListOfNewsChannels.getListOfChannels());
            formattedString = formattedString.substring(1, String.valueOf(iListOfNewsChannels.getListOfChannels()).length() - 1);
            formattedString = formattedString.replaceAll("\\s", "").replaceAll(",", "\n");
            var newMsg = SendMessage.builder()
                    .text(formattedString)
                    .chatId(message.getChatId().toString())
                    .replyMarkup(buildButtonsService.getMainMarkup())
                    .disableWebPagePreview(Boolean.TRUE)
                    .parseMode("HTML")
                    .build();
            messageSender.messageSend(newMsg);
        } else {
            var sendMsg = new SendMessage(message.getChatId().toString(), "I did not understand you. Try to press/text something else");
            messageSender.messageSend(sendMsg);
        }
    }
}