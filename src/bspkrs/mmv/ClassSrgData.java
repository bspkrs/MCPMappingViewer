package bspkrs.mmv;

public class ClassSrgData
{
    private final String obfName;
    private final String srgName;
    private String       srgPkgName;
    
    public ClassSrgData(String obfName, String srgName, String srgPkgName)
    {
        this.obfName = obfName;
        this.srgName = srgName;
        this.srgPkgName = srgPkgName;
    }
    
    public String getObfName()
    {
        return this.obfName;
    }
    
    public String getSrgName()
    {
        return this.srgName;
    }
    
    public String getSrgPkgName()
    {
        return this.srgPkgName;
    }
    
    public ClassSrgData setSrgPkgName(String pkg)
    {
        this.srgPkgName = pkg;
        return this;
    }
}
