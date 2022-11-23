package email.my.clean.gmail;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.util.Preconditions;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This collects and aggregates the unread emails.
 */
public class CollectEmails {
    /**
     * The gmail service that lets you do things
     */
    private final Gmail service;

    public CollectEmails(Gmail service) {
        this.service = service;
    }

    public void getAllUnreadMessages() throws IOException {
        List<String> unreadMessageIds = getMessageIdsWithLabel("is:unread");
        Collection<MessageMetadata> messageInfo = getMessageInfo(unreadMessageIds);

        for (MessageMetadata emailType : messageInfo) {
            System.out.println("----- NEW MESSAGE -----");
            System.out.printf("Sender: %s\n", emailType.getSender());
            System.out.printf("Count: %s\n", emailType.getMessageIds().size());
            System.out.printf("Unsubscribe email: %s\n", emailType.getUnsubscribeEmail());
            System.out.printf("Subject examples: %s\n\n", String.join("; ", emailType.getSubjects()));
        }
    }

    /**
     * Get message IDs that have a given label
     */
    private List<String> getMessageIdsWithLabel(String query) throws IOException {
        List<String> result = new ArrayList<>();
        Gmail.Users.Messages.List request = service.users().messages().list(GmailUtil.USER);
        request.setQ(query);
        request.setMaxResults(10L);

        String nextPageToken;

//        do {
            ListMessagesResponse response = request.execute();
            // Add all the message IDs
            result.addAll(response.getMessages()
                    .stream()
                    .map(Message::getId)
                    .collect(Collectors.toList()));

            // Set the next page token
            nextPageToken = response.getNextPageToken();
            request.setPageToken(nextPageToken);
//        } while (StringUtils.isNotEmpty(nextPageToken));

        return result;
    }

    /**
     * Get message info specific to all the message IDs passed in
     */
    private Collection<MessageMetadata> getMessageInfo(List<String> messageIds) throws IOException {
        BatchRequest batch = service.batch();

        // Maps the sending email to metadata associated with it
        ConcurrentHashMap<String, MessageMetadata> senderToId = new ConcurrentHashMap<>();

        JsonBatchCallback<Message> callback = new JsonBatchCallback<>() {
            @Override
            public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) throws IOException {
                throw new RuntimeException("It didn't work for this specific one: " + googleJsonError);
            }

            @Override
            public void onSuccess(Message message, HttpHeaders httpHeaders) throws IOException {
                String from = null;
                String unsubscribe = null;
                String subject = null;
                int i = 0;
                List<MessagePartHeader> headers = message.getPayload().getHeaders();

                // Iterate through all elements of the list unless we find a from email and unsubscribe email
                while ((from == null || unsubscribe == null || subject == null) && i < headers.size()) {
                    MessagePartHeader header = headers.get(i);
                    String headerName = header.getName();

                    switch (headerName) {
                        case "From":
                            from = header.getValue();
                            break;
                        case "List-Unsubscribe":
                            unsubscribe = header.getValue();
                            break;
                        case "Subject":
                            subject = header.getValue();
                            break;
                    }

                    i++;
                }

                Preconditions.checkNotNull(from, String.format("The message does not have a from email: %s", message.getId()));

                // If the map already has the key, just update the new associated email
                if (senderToId.containsKey(from)) {
                    senderToId.get(from).addNewEmail(message.getId(), subject);

                // Otherwise, add it to the map
                } else {
                    senderToId.put(from, new MessageMetadata(from, message.getId(), subject));

                    // If we have an unsubscribe email, add it
                    if (unsubscribe != null) {
                        senderToId.get(from).setUnsubscribeEmail(unsubscribe);
                    }
                }
            }
        };

        for (String messageId : messageIds) {
            service.users().messages().get(GmailUtil.USER, messageId).queue(batch, callback);
        }

        // Execute the batch
        batch.execute();

        // Ensure we don't have a weird error where nothing came back
        Preconditions.checkArgument(!senderToId.isEmpty(), "There were not any emails that were sent");

        // Return values
        return senderToId.values();
    }
}