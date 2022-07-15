package ru.robar3.chatgb;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Command {
    AUTH("/auth"){
        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(COMMAND_DELIMiTR);
            return new String[]{split[1],split[2]};
        }
    },
    AUTHOK("/authok"){
        @Override
        public String[] parse(String commandText) {
            return new String[]{commandText.split(COMMAND_DELIMiTR)[1]};
        }
    },
    PRIVATE_MESSAGE("/w"){
        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(COMMAND_DELIMiTR,3);
            return new String[]{split[1],split[2]};
        }
    },
    END("/end"){
        @Override
        public String[] parse(String commandText) {
            return new String[0];
        }
    },
    ERROR("/error"){
        @Override
        public String[] parse(String commandText) {
            String errorMsg = commandText.split(COMMAND_DELIMiTR, 2)[1];
            return new String[]{errorMsg};
        }
    },
    CLIENTS("/clients"){
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(COMMAND_DELIMiTR);
        //    return Stream.of(split).skip(1).toArray(String[]::new);
            String[] nicks = new String[split.length - 1];
            for (int i = 1; i < split.length; i++) {
                nicks[i-1]=split[i];
            }
            return nicks;
        }
    };

    private  static final Map<String,Command>map = Stream.of(Command.values())
            .collect(Collectors.toMap(Command::getCommand, Function.identity()));
    private String command;
    private String[] params = new String[0];
    static final String COMMAND_DELIMiTR="\\s+";
    Command(String command) {

        this.command = command;
    }
    public static boolean isCommand(String message){
        return message.startsWith("/");
    }

    public String[] getParams() {
        return params;
    }

    public String getCommand() {
        return command;
    }

    public static Command getCommand(String message){
        message=message.trim();
        if (!isCommand(message)){
            throw new RuntimeException(" "+message+" is not command");
        }
        final int index = message.indexOf(" ");
        final String cmd = index > 0 ? message.substring(0, index) : message;
        Command command = map.get(cmd);
        if (command==null){
            throw new RuntimeException(" "+cmd+" unknown command");
        }
        return command;
    }
    public abstract String[] parse (String commandText);

    public String collectMessage(String... params) {
        final String command = this.getCommand();
        return command+(params==null? "" :" "+String.join(" ",params));
    }
}
