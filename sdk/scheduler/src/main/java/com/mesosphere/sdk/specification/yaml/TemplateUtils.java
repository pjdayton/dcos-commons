package com.mesosphere.sdk.specification.yaml;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * Utility methods relating to rendering mustache templates.
 */
public class TemplateUtils {

    /**
     * Pattern for envvar names surrounded by "{{" "}}".
     * {@link Pattern.DOTALL} is needed to ensure that we scan beyond the first line...
     */
    private static final Pattern MUSTACHE_PATTERN = Pattern.compile(".*\\{\\{[a-zA-Z0-9_]+\\}\\}.*", Pattern.DOTALL);

    private TemplateUtils() {
        // do not instantiate
    }

    /**
     * Renders a given Mustache template using the provided environment map.
     *
     * @param templateContent String representation of template.
     * @param environment     Map of environment variables.
     * @return Rendered Mustache template String.
     */
    public static String applyEnvToMustache(String templateContent, Map<String, String> environment) {
        StringWriter writer = new StringWriter();
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader(templateContent), "configTemplate");
        executeMustache(writer, templateContent, environment);
        return writer.toString();
    }

    /**
     * Returns whether the provided content still contains any "{{ }}" mustache templating.
     *
     * @param templateContent the content to be evaluated
     */
    public static boolean isMustacheFullyRendered(String templateContent) {
        return StringUtils.isEmpty(templateContent) || !MUSTACHE_PATTERN.matcher(templateContent).matches();
    }

    public static final class DiskConfig {
        public String Type;
        public String Root;
        public String Path;
        public int Size;
        public boolean Last;
        public DiskConfig(String tp, String root, String path, int size, boolean last) {
            this.Type = tp;
            this.Root = root;
            this.Path = path;
            this.Size = size;
            this.Last = last;
        }
    }

    public static List<DiskConfig> parseDisksString(String disks) {
        List<DiskConfig> output = new ArrayList<>();
        String[] tmp = disks.split(";");
        for (String disk : tmp) {
            String[] spec = disk.split(",");
            String type = spec[0];
            String root = spec[1];
            String path = spec[2];
            int size = Integer.parseInt(spec[3]);
            output.add(new DiskConfig(type, root, path, size, false));
        }
        output.get(output.size() - 1).Last = true;
        return output;
    }

    /**
     * Executes a Mustache template, using some extra functions to parse things like disk specs.
     *
     * @param templateContent String representation of template.
     * @param environment     Map of environment variables.
     * @return Rendered Mustache template String.
     */
    public static Writer executeMustache(Writer writer, String templateContent, Map<String, String> environment) {
        Map<String, Object> objectEnvironment = new HashMap<>();
        objectEnvironment.putAll(environment);

        Function<String, String> parseDisks = input -> {
            try {
                String inputVarName = environment.getOrDefault("PARSE_DISKS_INPUT_VAR", "DATA_DISKS");
                String outputVarName = environment.getOrDefault("PARSE_DISKS_OUTPUT_VAR", "data_disks");

                String inputVar = environment.get(inputVarName);
                List<DiskConfig> outputDisks = parseDisksString(inputVar);
                objectEnvironment.put(outputVarName, outputDisks);
            } catch (Exception e) {
                return "";
                //date jos
            }
            return "";
        };

        objectEnvironment.put("ParseDisks", parseDisks);

        DefaultMustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader(templateContent), "configTemplate");
        return mustache.execute(writer, objectEnvironment);
    }

}
