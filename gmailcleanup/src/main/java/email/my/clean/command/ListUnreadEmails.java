package email.my.clean.command;

import com.google.api.services.gmail.Gmail;
import email.my.clean.gmail.CollectEmails;

import java.io.IOException;

public class ListUnreadEmails implements Command {
    @Override
    public void execute(Gmail service) throws IOException {
        CollectEmails email = new CollectEmails(service);
        email.getAllUnreadMessages();
    }
}
