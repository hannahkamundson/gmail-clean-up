package email.my.clean;

import com.google.api.services.gmail.Gmail;
import email.my.clean.command.Command;
import email.my.clean.gmail.GmailUtil;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class App {
    public static void main(String[] args) throws GeneralSecurityException, IOException, ArgumentParserException {
        // Create a basic CLI parser
        ArgumentParser parser = ArgumentParsers.newFor("Clean My Email").build()
                .description("Clean up my email")
                .defaultHelp(true);

        // The user has to pass in a command
        parser.addArgument("--command")
                .required(true)
                .choices("ListUnreadEmails")
                .nargs("+")
                .type(Command.class);

        Namespace namespace = parser.parseArgs(args);

        List<Command> commands = namespace.get("command");

        Gmail service = GmailUtil.create();

        for (Command command: commands) {
            command.execute(service);
        }
    }
}
