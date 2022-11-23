package email.my.clean.command;

import com.google.api.services.gmail.Gmail;

import java.io.IOException;

public interface Command {
    void execute(Gmail service) throws IOException;
}
