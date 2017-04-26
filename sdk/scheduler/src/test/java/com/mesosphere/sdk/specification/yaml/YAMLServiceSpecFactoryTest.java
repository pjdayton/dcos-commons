package com.mesosphere.sdk.specification.yaml;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.io.FileUtils;

import com.mesosphere.sdk.scheduler.SchedulerFlags;
import com.mesosphere.sdk.specification.DefaultServiceSpec;
import com.mesosphere.sdk.testutils.TestConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static com.mesosphere.sdk.specification.yaml.YAMLServiceSpecFactory.*;
import static org.mockito.Mockito.when;

public class YAMLServiceSpecFactoryTest {
    private static final Map<String, String> YAML_ENV_MAP = new HashMap<>();
    static {
        YAML_ENV_MAP.put("PORT_API", String.valueOf(TestConstants.PORT_API_VALUE));
    }

    @Mock private SchedulerFlags mockFlags;
    @Mock private FileReader mockFileReader;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGenerateSpecFromYAML() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("valid-exhaustive.yml").getFile());

        when(mockFileReader.read("config-one.conf.mustache")).thenReturn("hello");
        when(mockFileReader.read("config-two.xml.mustache")).thenReturn("hey");
        when(mockFileReader.read("config-three.conf.mustache")).thenReturn("hi");
        when(mockFlags.getApiServerPort()).thenReturn(123);
        when(mockFlags.getExecutorURI()).thenReturn("test-executor-uri");

        DefaultServiceSpec serviceSpec = generateServiceSpec(generateRawSpecFromYAML(file), mockFlags, mockFileReader);
        Assert.assertNotNull(serviceSpec);
        Assert.assertEquals(Integer.valueOf(123), Integer.valueOf(serviceSpec.getApiPort()));
    }

    @Test
    public void testMustache() throws Exception {
        final Map<String, String> env = new HashMap<String, String>() {{
            put("DISKS", "PATH,/mnt/data_1,data-1,1000;PATH,/mnt/data_2,data-2,1000;PATH,/mnt/data_3,data-3,1000");
            put("PARSE_DISKS_INPUT_VAR", "DISKS");
            put("PARSE_DISKS_OUTPUT_VAR", "outputDisks");
        }};
        Writer w = new StringWriter();
        MustacheFactory mf = new DefaultMustacheFactory();
        String template = "{{#ParseDisks}}{{/ParseDisks}}\n" +
                "{{#outputDisks}}\n" +
                "{{Path}}: \n" +
                "  type: {{Type}}\n" +
                "  path: {{Path}}\n" +
                "  root: {{Root}}\n" +
                "  size: {{Size}}\n" +
                "{{/outputDisks}}";

        Writer w2 = TemplateUtils.executeMustache(w, template, env);
        System.out.println(w2.toString());
    }

    @Test
    public void testGenerateRawSpecFromYAMLFile() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("valid-exhaustive.yml").getFile());
        RawServiceSpec rawServiceSpec = generateRawSpecFromYAML(file, YAML_ENV_MAP);
        Assert.assertNotNull(rawServiceSpec);
        Assert.assertEquals(TestConstants.PORT_API_VALUE, rawServiceSpec.getScheduler().getApiPort());
    }

    @Test
    public void testGenerateRawSpecFromYAMLString() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("valid-exhaustive.yml").getFile());
        String yaml = FileUtils.readFileToString(file);
        RawServiceSpec rawServiceSpec = generateRawSpecFromYAML(yaml, YAML_ENV_MAP);
        Assert.assertNotNull(rawServiceSpec);
        Assert.assertEquals(TestConstants.PORT_API_VALUE, rawServiceSpec.getScheduler().getApiPort());
    }
}
