package bspkrs.mmv;

public class CsvData implements Comparable<CsvData>
{
    private final String srgName;
    private String       mcpName;
    private final int    side;
    private String       comment;
    
    public CsvData(String srgName, String mcpName, int side, String comment)
    {
        this.srgName = srgName;
        this.setMcpName(mcpName);
        this.side = side;
        this.setComment(comment);
    }
    
    public String toCsv()
    {
        return srgName + "," + mcpName + "," + side + "," + comment;
    }
    
    public String getSrgName()
    {
        return srgName;
    }
    
    public String getMcpName()
    {
        return mcpName;
    }
    
    public CsvData setMcpName(String mcpName)
    {
        this.mcpName = mcpName;
        return this;
    }
    
    public int getSide()
    {
        return side;
    }
    
    public String getComment()
    {
        return comment;
    }
    
    public CsvData setComment(String comment)
    {
        this.comment = comment;
        return this;
    }
    
    @Override
    public int compareTo(CsvData o)
    {
        return 0;
    }
}
