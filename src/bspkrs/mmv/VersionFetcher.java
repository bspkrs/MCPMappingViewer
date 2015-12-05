package bspkrs.mmv;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import bspkrs.mmv.version.NaturalOrderComparator;

import com.google.gson.Gson;

public class VersionFetcher
{
    private final String jsonUrl = "http://export.mcpbot.bspk.rs/versions.json";
    private final File   baseDir = new File(new File(System.getenv("APPDATA")), "/.mmv/");
    private List<String> versions;

    @SuppressWarnings("unchecked")
    public List<String> getVersions(boolean force) throws IOException
    {
        if ((versions == null) || force)
        {
            final URL url = new URL(jsonUrl);
            final URLConnection connection = url.openConnection();
            connection.addRequestProperty("User-Agent", "MMV/1.0.0");
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            Map<String, Object> json = new Gson().fromJson(br, Map.class);

            versions = new ArrayList<String>();
            for (String mcVer : json.keySet())
                for (String channel : ((Map<String, ArrayList<Double>[]>) json.get(mcVer)).keySet())
                    for (Double ver : ((Map<String, ArrayList<Double>>) json.get(mcVer)).get(channel))
                        versions.add(mcVer + "_" + channel + "_" + String.format("%.0f", ver));
            Collections.sort(versions, Collections.reverseOrder(new NaturalOrderComparator()));
            return versions;
        }
        else
            return versions;
    }
}
