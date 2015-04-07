package ru.steagle.protocol;

import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import ru.steagle.protocol.request.Command;

public class Request {
    private List<Command> commands = new ArrayList<>();

    List<Command> getCommands() {
        return commands;
    }

    public Request add(Command command) {
        commands.add(command);
        return this;
    }

    public String serialize() {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "root");
            int i=0;
            for (Command command: commands) {
                command.serialize(serializer, commands.size() == 1 ? 0 : ++i);
            }

            serializer.endTag("", "root");
            serializer.endDocument();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String result = writer.toString().replace("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>");
        return result;
    }

    @Override
    public String toString() {
        return serialize();
    }
}
