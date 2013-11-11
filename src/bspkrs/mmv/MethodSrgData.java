package bspkrs.mmv;

public class MethodSrgData
{
    private final String  obfOwner;
    private final String  obfName;
    private final String  obfDescriptor;
    private final String  srgOwner;
    private final String  srgName;
    private final String  srgDescriptor;
    private final boolean isClientOnly;
    
    public MethodSrgData(String obfOwner, String obfName, String obfDescriptor, String srgOwner, String srgName, String srgDescriptor, boolean isClientOnly)
    {
        this.obfOwner = obfOwner;
        this.obfName = obfName;
        this.obfDescriptor = obfDescriptor;
        this.srgOwner = srgOwner;
        this.srgName = srgName;
        this.srgDescriptor = srgDescriptor;
        this.isClientOnly = isClientOnly;
    }
    
    public String getObfOwner()
    {
        return obfOwner;
    }
    
    public String getObfName()
    {
        return obfName;
    }
    
    public String getObfDescriptor()
    {
        return obfDescriptor;
    }
    
    public String getSrgOwner()
    {
        return srgOwner;
    }
    
    public String getSrgName()
    {
        return srgName;
    }
    
    public String getSrgDescriptor()
    {
        return srgDescriptor;
    }

    public boolean isClientOnly()
    {
        return isClientOnly;
    }
}
