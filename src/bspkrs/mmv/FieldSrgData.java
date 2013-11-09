package bspkrs.mmv;

public class FieldSrgData
{
    private final String obfOwner;
    private final String obfName;
    private final String srgOwner;
    private final String srgName;
    
    public FieldSrgData(String obfOwner, String obfName, String srgOwner, String srgName)
    {
        this.obfOwner = obfOwner;
        this.obfName = obfName;
        this.srgOwner = srgOwner;
        this.srgName = srgName;
    }

    public String getObfOwner()
    {
        return obfOwner;
    }

    public String getObfName()
    {
        return obfName;
    }

    public String getSrgOwner()
    {
        return srgOwner;
    }

    public String getSrgName()
    {
        return srgName;
    }
}
