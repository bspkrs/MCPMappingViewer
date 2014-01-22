package bspkrs.mmv;

import immibis.bon.gui.Side;

public class McpBotCommand
{
    public enum BotCommand
    {
        SCF,
        SCM,
        FSCF,
        FSCM,
        SSF,
        SSM,
        FSSF,
        FSSM;
    }
    
    public enum MemberType
    {
        FIELD,
        METHOD;
    }
    
    public static BotCommand getCommand(MemberType type, Side side, boolean isForced)
    {
        switch (side)
        {
            case Server:
                switch (type)
                {
                    case METHOD:
                        return isForced ? BotCommand.FSSM : BotCommand.SSM;
                    default:
                        return isForced ? BotCommand.FSSF : BotCommand.SSF;
                }
            default:
                switch (type)
                {
                    case METHOD:
                        return isForced ? BotCommand.FSCM : BotCommand.SCM;
                    default:
                        return isForced ? BotCommand.FSCF : BotCommand.SCF;
                }
        }
    }
    
    private final BotCommand command;
    private final String     srgName;
    private final String     newName;
    private final String     comment;
    
    public McpBotCommand(BotCommand command, String srgName, String newName, String comment)
    {
        this.command = command;
        this.srgName = srgName;
        this.newName = newName;
        this.comment = comment;
    }
    
    public McpBotCommand(BotCommand command, String srgName, String newName)
    {
        this(command, srgName, newName, "");
    }
    
    public static McpBotCommand[] getMcpBotCommands(MemberType type, Side side, boolean isForced, boolean isClientOnly, String srgName, String newName, String comment)
    {
        McpBotCommand[] commands = new McpBotCommand[1];
        if (side.equals(Side.Universal) && !isClientOnly)
        {
            commands = new McpBotCommand[2];
            commands[1] = new McpBotCommand(getCommand(type, Side.Server, isForced), srgName, newName, comment);
        }
        
        commands[0] = new McpBotCommand(getCommand(type, side, isForced), srgName, newName, comment);
        
        return commands;
    }
    
    public static McpBotCommand[] updateMcpBotCommands(McpBotCommand[] commands, String srgName, String newName, String comment)
    {
        for (int i = 0; i < commands.length; i++)
        {
            commands[i] = new McpBotCommand(commands[i].getCommand(), srgName, newName, comment);
        }
        
        return commands;
    }
    
    public BotCommand getCommand()
    {
        return command;
    }
    
    public String getNewName()
    {
        return newName;
    }
    
    @Override
    public String toString()
    {
        return String.format("!%s %s %s %s", command.toString().toLowerCase(), srgName, newName, comment);
    }
}
