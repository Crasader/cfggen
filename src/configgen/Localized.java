package configgen;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by HuangQiang on 2017/1/18.
 */
public class Localized {
    public final static Localized Ins = new Localized();
    private final Map<String, String> mapper = new HashMap<>();
    private final Set<String> unmappers = new HashSet<>();

    private boolean hasLocalized = false;

    public boolean isHasLocalized() {
        return hasLocalized;
    }

    public void load(String file) throws Exception {
        String fullPath = Utils.combine(Main.csvDir, file);
        List<List<String>> lines = (List<List<String>>)Utils.parseAsXmlOrLuaOrFlatStream(fullPath);
        for(List<String> line : lines) {
            line = line.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
            if(line.isEmpty()) continue;
            if(line.get(0).startsWith("##")) continue;
            if(line.size() != 2) {
                Utils.error("localized file. invalid line:" + line);
            }
            if(mapper.put(line.get(0), line.get(1)) != null) {
                Utils.error("localized file. duplicate line:" + line);
            }
        }
        hasLocalized = true;
    }

    public String getLocalizedStr(String src) {
        return mapper.get(src);
    }

    public void addUnlocalizedStr(String src) {
        unmappers.add(src);
    }

    public void saveUnLocalizedAs(String file) {
        final String text = unmappers.stream().map(s -> s.replace("\"", "\"\""))
                .map(s -> s.contains("\n") ? "\"" + s + "\"" : s).collect(Collectors.joining("\n"));
        Utils.save(Utils.combine(Main.csvDir, file), text);
    }
}
