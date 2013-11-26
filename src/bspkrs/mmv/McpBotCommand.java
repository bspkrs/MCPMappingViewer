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
        return String.format("%s %s %s %s", command.toString().toLowerCase(), srgName, newName, comment);
    }
}
