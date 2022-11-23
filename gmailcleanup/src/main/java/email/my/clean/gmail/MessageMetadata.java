package email.my.clean.gmail;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class MessageMetadata {

    /**
     * The email sender
     */
    @Getter
    private String sender;

    /**
     * All message IDs that have this sender
     */
    @Getter
    private List<String> messageIds;

    /**
     * An associated unsubscribe email if it exists
     */
    @Getter
    @Setter
    private String unsubscribeEmail;

    @Getter
    private List<String> subjects;

    public MessageMetadata(String sender, String messageId, String subject) {
        this.sender = sender;
        messageIds = new ArrayList<>();
        subjects = new ArrayList<>();
        addNewEmail(messageId, subject);
    }

    public void addNewEmail(String messageId, String subject) {
        messageIds.add(messageId);
        if (subject != null) {
            subjects.add(subject);
        }
    }
}
