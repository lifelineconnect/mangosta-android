package inaka.com.mangosta.realm;

import java.util.ArrayList;
import java.util.List;

import inaka.com.mangosta.models.BlogPost;
import inaka.com.mangosta.models.BlogPostComment;
import inaka.com.mangosta.models.Chat;
import inaka.com.mangosta.models.ChatMessage;
import inaka.com.mangosta.utils.MangostaApplication;
import inaka.com.mangosta.utils.Preferences;
import inaka.com.mangosta.xmpp.XMPPUtils;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class RealmManager {

    public static void saveChatMessage(ChatMessage chatMessage) {
        Realm realm = getRealm();

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(chatMessage);
        realm.commitTransaction();

        realm.close();
    }

    public static void saveChat(Chat chat) {
        Realm realm = getRealm();

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(chat);
        realm.commitTransaction();

        realm.close();
    }

    public static List<Chat> getMUCs(Realm realm) {
        List<Chat> chatList = new ArrayList<>();

        RealmResults<Chat> chats = realm.where(Chat.class)
                .equalTo("show", true)
                .equalTo("type", Chat.TYPE_MUC)
                .findAll();

        for (Chat chat : chats) {
            chatList.add(chat);
        }

        return chatList;
    }

    public static List<Chat> getMUCLights(Realm realm) {
        List<Chat> chatList = new ArrayList<>();

        RealmResults<Chat> chats = realm.where(Chat.class)
                .equalTo("show", true)
                .equalTo("type", Chat.TYPE_MUC_LIGHT)
                .findAll();

        for (Chat chat : chats) {
            chatList.add(chat);
        }

        return chatList;
    }

    public static List<Chat> get1to1Chats(Realm realm) {
        List<Chat> chatList = new ArrayList<>();

        RealmResults<Chat> chats = realm.where(Chat.class)
                .equalTo("show", true)
                .equalTo("type", Chat.TYPE_1_T0_1)
                .notEqualTo("jid", Preferences.getInstance().getUserXMPPJid())
                .findAll();

        for (Chat chat : chats) {
            chatList.add(chat);
        }

        return chatList;
    }

    public static boolean chatExists(String chatFromJID) {
        Realm realm = getRealm();

        boolean hasChat = realm.where(Chat.class)
                .equalTo("jid", chatFromJID)
                .count() > 0;

        realm.close();

        return hasChat;
    }

    public static boolean chatMessageExists(String messageId) {
        Realm realm = getRealm();

        boolean hasChat = realm.where(ChatMessage.class)
                .equalTo("messageId", messageId)
                .count() > 0;

        realm.close();

        return hasChat;
    }

    public static ChatMessage getChatMessage(String messageId) {
        Realm realm = getRealm();
        ChatMessage chatMessage =
                realm.where(ChatMessage.class)
                        .equalTo("messageId", messageId)
                        .findFirst();
        realm.close();

        return chatMessage;
    }

    public static Chat getChat(String chatJid) {
        Realm realm = getRealm();
        Chat chat = realm.where(Chat.class)
                .equalTo("jid", chatJid)
                .findFirst();
        realm.close();

        return chat;
    }

    public static Realm getRealm() {
        Realm.init(MangostaApplication.getInstance());
        return Realm.getDefaultInstance();
    }

    public static RealmResults<ChatMessage> getMessagesForChat(Realm realm, String jid) {
        return realm.where(ChatMessage.class)
                .equalTo("roomJid", jid)
                .isNotEmpty("content")
                .findAllSorted("date", Sort.ASCENDING);
    }

    public static ChatMessage getLastMessageSentByMeForChat(String jid) {
        Realm realm = getRealm();
        RealmResults<ChatMessage> chatMessages =
                realm.where(ChatMessage.class)
                        .equalTo("roomJid", jid)
                        .isNotEmpty("content")
                        .equalTo("userSender", XMPPUtils.fromJIDToUserName(Preferences.getInstance().getUserXMPPJid()))
                        .findAllSorted("date", Sort.ASCENDING);
        realm.close();

        return chatMessages.last();
    }

    public static ChatMessage getLastMessageForChat(String jid) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        for (ChatMessage chatMessage : getMessagesForChat(getRealm(), jid)) {
            chatMessages.add(chatMessage);
        }
        if (chatMessages.size() == 0) {
            return null;
        } else {
            return chatMessages.get(chatMessages.size() - 1);
        }
    }

    public static void saveBlogPost(BlogPost blogPost) {
        Realm realm = getRealm();

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(blogPost);
        realm.commitTransaction();

        realm.close();
    }

    public static List<BlogPost> getBlogPosts() {
        List<BlogPost> blogPosts = new ArrayList<>();

        Realm realm = getRealm();

        RealmResults<BlogPost> blogPostRealmResults = realm.where(BlogPost.class).findAllSorted("updated", Sort.DESCENDING);

        for (BlogPost blogPost : blogPostRealmResults) {
            blogPosts.add(blogPost);
        }

        return blogPosts;
    }

    public static void saveBlogPostComment(BlogPostComment comment) {
        Realm realm = getRealm();

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(comment);
        realm.commitTransaction();

        realm.close();
    }

    public static List<BlogPostComment> getBlogPostComments(String blogPostId) {
        List<BlogPostComment> comments = new ArrayList<>();

        Realm realm = getRealm();

        RealmResults<BlogPostComment> blogPostComments = realm.where(BlogPostComment.class)
                .equalTo("blogPostId", blogPostId)
                .findAll();

        for (BlogPostComment comment : blogPostComments) {
            comments.add(comment);
        }

        return comments;
    }

    public static void deleteMessage(String messageId) {
        Realm realm = getRealm();

        ChatMessage chatMessage = realm.where(ChatMessage.class)
                .equalTo("messageId", messageId)
                .findFirst();

        if (chatMessage != null) {
            realm.beginTransaction();
            chatMessage.deleteFromRealm();
            realm.commitTransaction();
        }

        realm.close();
    }

}